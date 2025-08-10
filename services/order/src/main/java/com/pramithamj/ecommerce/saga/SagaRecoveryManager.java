package com.pramithamj.ecommerce.saga;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Saga Recovery Manager handles timeout scenarios and saga cleanup
 * Implements compensation logic for failed or timed-out sagas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaRecoveryManager {

    private final OrderSagaRepository sagaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CircuitBreaker sagaRecoveryCircuitBreaker;
    
    private static final int SAGA_TIMEOUT_MINUTES = 30;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Runs every 5 minutes to check for timed-out sagas
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void recoverTimedOutSagas() {
        log.info("Starting saga recovery process...");
        
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(SAGA_TIMEOUT_MINUTES);
        
        // Find sagas that are stuck in pending states
        List<OrderSaga> timedOutSagas = sagaRepository.findByStatusInAndStartTimeBefore(
            List.of(SagaStatus.STARTED, SagaStatus.CUSTOMER_VALIDATED, 
                   SagaStatus.INVENTORY_RESERVED, SagaStatus.PAYMENT_PROCESSED),
            timeoutThreshold
        );
        
        for (OrderSaga saga : timedOutSagas) {
            handleTimedOutSaga(saga);
        }
        
        log.info("Saga recovery process completed. Processed {} timed-out sagas", timedOutSagas.size());
    }

    /**
     * Handles a timed-out saga by either retrying or starting compensation
     */
    private void handleTimedOutSaga(OrderSaga saga) {
        sagaRecoveryCircuitBreaker.executeSupplier(() -> {
            log.warn("Handling timed-out saga: {} in status: {}", saga.getSagaId(), saga.getStatus());
            
            if (saga.getRetryCount() < MAX_RETRY_ATTEMPTS) {
                // Retry the saga
                retrySaga(saga);
            } else {
                // Start compensation process
                startCompensationForTimedOutSaga(saga);
            }
            return true;
        });
    }

    /**
     * Retries a saga based on its current status
     */
    private void retrySaga(OrderSaga saga) {
        saga.setRetryCount(saga.getRetryCount() + 1);
        
        log.info("Retrying saga: {} (attempt {}/{})", saga.getSagaId(), saga.getRetryCount(), MAX_RETRY_ATTEMPTS);
        
        switch (saga.getStatus()) {
            case STARTED:
                // Retry customer validation
                retryCustomerValidation(saga);
                break;
            case CUSTOMER_VALIDATED:
                // Retry inventory reservation
                retryInventoryReservation(saga);
                break;
            case INVENTORY_RESERVED:
                // Retry payment processing
                retryPaymentProcessing(saga);
                break;
            case PAYMENT_PROCESSED:
                // Check if saga should be completed
                checkSagaCompletion(saga);
                break;
            default:
                log.warn("Cannot retry saga in status: {}", saga.getStatus());
                break;
        }
        
        sagaRepository.save(saga);
    }

    /**
     * Starts compensation for a timed-out saga
     */
    private void startCompensationForTimedOutSaga(OrderSaga saga) {
        sagaRecoveryCircuitBreaker.executeSupplier(() -> {
            log.warn("Starting compensation for timed-out saga: {}", saga.getSagaId());
            
            saga.setStatus(SagaStatus.COMPENSATING);
            saga.setErrorMessage("Saga timed out after " + SAGA_TIMEOUT_MINUTES + " minutes");
            saga.setEndTime(LocalDateTime.now());
            
            // Start compensation based on how far the saga progressed
            switch (saga.getStatus()) {
                case PAYMENT_PROCESSED:
                    compensatePayment(saga);
                    break;
                case INVENTORY_RESERVED:
                    compensateInventory(saga);
                    break;
                default:
                    // No compensation needed for earlier stages
                    saga.setStatus(SagaStatus.CANCELLED);
                    break;
            }
            
            sagaRepository.save(saga);
            return true;
        });
    }

    /**
     * Retry customer validation step
     */
    private void retryCustomerValidation(OrderSaga saga) {
        log.info("Retrying customer validation for saga: {}", saga.getSagaId());
        
        // Publish customer validation event
        kafkaTemplate.send("customer-validation-topic", new CustomerValidationRetryEvent(
            saga.getSagaId(),
            saga.getCustomerId(),
            saga.getRetryCount()
        ));
    }

    /**
     * Retry inventory reservation step
     */
    private void retryInventoryReservation(OrderSaga saga) {
        log.info("Retrying inventory reservation for saga: {}", saga.getSagaId());
        
        // Publish inventory reservation event
        kafkaTemplate.send("inventory-reservation-topic", new InventoryReservationRetryEvent(
            saga.getSagaId(),
            saga.getProducts(),
            saga.getRetryCount()
        ));
    }

    /**
     * Retry payment processing step
     */
    private void retryPaymentProcessing(OrderSaga saga) {
        log.info("Retrying payment processing for saga: {}", saga.getSagaId());
        
        // Calculate total amount
        Double totalAmount = saga.getProducts().stream()
            .mapToDouble(product -> product.getQuantity() * getProductPrice(product.getProductId()))
            .sum();
        
        // Publish payment processing event
        kafkaTemplate.send("payment-processing-topic", new PaymentProcessingRetryEvent(
            saga.getSagaId(),
            saga.getCustomerId(),
            totalAmount,
            saga.getPaymentMethod(),
            saga.getRetryCount()
        ));
    }

    /**
     * Check if saga should be completed
     */
    private void checkSagaCompletion(OrderSaga saga) {
        log.info("Checking completion status for saga: {}", saga.getSagaId());
        
        // Check if all steps are completed
        boolean allStepsCompleted = saga.getSteps().stream()
            .allMatch(step -> step.getStatus() == OrderSaga.SagaStepStatus.COMPLETED);
        
        if (allStepsCompleted) {
            saga.setStatus(SagaStatus.COMPLETED);
            saga.setEndTime(LocalDateTime.now());
            log.info("Saga completed during recovery: {}", saga.getSagaId());
        } else {
            log.warn("Saga steps not all completed during recovery check: {}", saga.getSagaId());
        }
    }

    /**
     * Compensate payment for failed saga
     */
    private void compensatePayment(OrderSaga saga) {
        log.info("Compensating payment for saga: {}", saga.getSagaId());
        
        Integer paymentId = (Integer) saga.getSagaData().get("paymentId");
        if (paymentId != null) {
            kafkaTemplate.send("payment-compensation-topic", new PaymentCompensationEvent(
                saga.getSagaId(),
                paymentId
            ));
        }
    }

    /**
     * Compensate inventory reservation for failed saga
     */
    private void compensateInventory(OrderSaga saga) {
        log.info("Compensating inventory for saga: {}", saga.getSagaId());
        
        kafkaTemplate.send("inventory-compensation-topic", new InventoryCompensationEvent(
            saga.getSagaId(),
            saga.getProducts()
        ));
    }

    /**
     * Get product price (this would typically call Product Service)
     */
    private Double getProductPrice(Integer productId) {
        // In a real implementation, this would call the Product Service
        // For now, return a default price
        return 100.0;
    }

    /**
     * Clean up old completed sagas (runs daily)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldSagas() {
        log.info("Starting cleanup of old completed sagas...");
        
        LocalDateTime cleanupThreshold = LocalDateTime.now().minusDays(30);
        
        List<OrderSaga> oldSagas = sagaRepository.findByStatusInAndEndTimeBefore(
            List.of(SagaStatus.COMPLETED, SagaStatus.CANCELLED, SagaStatus.FAILED),
            cleanupThreshold
        );
        
        if (!oldSagas.isEmpty()) {
            sagaRepository.deleteAll(oldSagas);
            log.info("Cleaned up {} old saga records", oldSagas.size());
        }
    }

    // Event classes for retry scenarios
    private static class CustomerValidationRetryEvent {
        private String sagaId;
        private String customerId;
        private int retryAttempt;

        public CustomerValidationRetryEvent(String sagaId, String customerId, int retryAttempt) {
            this.sagaId = sagaId;
            this.customerId = customerId;
            this.retryAttempt = retryAttempt;
        }

        // Getters
        public String getSagaId() { return sagaId; }
        public String getCustomerId() { return customerId; }
        public int getRetryAttempt() { return retryAttempt; }
    }

    private static class InventoryReservationRetryEvent {
        private String sagaId;
        private List<OrderSaga.OrderLineRequest> products;
        private int retryAttempt;

        public InventoryReservationRetryEvent(String sagaId, List<OrderSaga.OrderLineRequest> products, int retryAttempt) {
            this.sagaId = sagaId;
            this.products = products;
            this.retryAttempt = retryAttempt;
        }

        // Getters
        public String getSagaId() { return sagaId; }
        public List<OrderSaga.OrderLineRequest> getProducts() { return products; }
        public int getRetryAttempt() { return retryAttempt; }
    }

    private static class PaymentProcessingRetryEvent {
        private String sagaId;
        private String customerId;
        private Double amount;
        private String paymentMethod;
        private int retryAttempt;

        public PaymentProcessingRetryEvent(String sagaId, String customerId, Double amount, String paymentMethod, int retryAttempt) {
            this.sagaId = sagaId;
            this.customerId = customerId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.retryAttempt = retryAttempt;
        }

        // Getters
        public String getSagaId() { return sagaId; }
        public String getCustomerId() { return customerId; }
        public Double getAmount() { return amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public int getRetryAttempt() { return retryAttempt; }
    }

    private static class PaymentCompensationEvent {
        private String sagaId;
        private Integer paymentId;

        public PaymentCompensationEvent(String sagaId, Integer paymentId) {
            this.sagaId = sagaId;
            this.paymentId = paymentId;
        }

        // Getters
        public String getSagaId() { return sagaId; }
        public Integer getPaymentId() { return paymentId; }
    }

    private static class InventoryCompensationEvent {
        private String sagaId;
        private List<OrderSaga.OrderLineRequest> products;

        public InventoryCompensationEvent(String sagaId, List<OrderSaga.OrderLineRequest> products) {
            this.sagaId = sagaId;
            this.products = products;
        }

        // Getters
        public String getSagaId() { return sagaId; }
        public List<OrderSaga.OrderLineRequest> getProducts() { return products; }
    }
}
