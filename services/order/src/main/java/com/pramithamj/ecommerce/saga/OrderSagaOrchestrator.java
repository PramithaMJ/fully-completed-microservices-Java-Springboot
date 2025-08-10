package com.pramithamj.ecommerce.saga;

import com.pramithamj.ecommerce.saga.events.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final OrderSagaRepository sagaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CircuitBreaker paymentServiceCircuitBreaker;
    
    // Saga-specific circuit breakers
    private final CircuitBreaker sagaOrchestrationCircuitBreaker;
    private final CircuitBreaker sagaCompensationCircuitBreaker;
    private final CircuitBreaker inventoryReservationCircuitBreaker;
    private final CircuitBreaker customerValidationCircuitBreaker;

    public String startOrderSaga(List<OrderSaga.OrderLineRequest> products, String customerId, String paymentMethod) {
        return sagaOrchestrationCircuitBreaker.executeSupplier(() -> {
            String sagaId = UUID.randomUUID().toString();
            
            OrderSaga saga = OrderSaga.builder()
                    .sagaId(sagaId)
                    .customerId(customerId)
                    .products(products)
                    .paymentMethod(paymentMethod)
                    .status(SagaStatus.STARTED)
                    .sagaData(new HashMap<>())
                    .steps(new ArrayList<>())
                    .startTime(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            saga = sagaRepository.save(saga);
            log.info("Started Order Saga: {}", sagaId);

            // Start the saga by validating customer
            executeCustomerValidationStep(saga);
            
            return sagaId;
        });
    }

    private void executeCustomerValidationStep(OrderSaga saga) {
        try {
            // Execute with saga-specific circuit breaker for customer validation
            customerValidationCircuitBreaker.executeSupplier(() -> {
                log.info("Validating customer: {} for saga: {}", saga.getCustomerId(), saga.getSagaId());
                
                // Send customer validation event
                CustomerValidationEvent event = CustomerValidationEvent.builder()
                        .sagaId(saga.getSagaId())
                        .customerId(saga.getCustomerId())
                        .build();
                
                kafkaTemplate.send("customer-validation-topic", event);
                return true;
            });

            addSagaStep(saga, "CUSTOMER_VALIDATION", OrderSaga.SagaStepStatus.PENDING);
            updateSagaStatus(saga, SagaStatus.CUSTOMER_VALIDATED);
            
        } catch (Exception e) {
            log.error("Customer validation failed for saga: {}", saga.getSagaId(), e);
            handleSagaFailure(saga, "Customer validation failed: " + e.getMessage());
        }
    }

    public void handleCustomerValidationSuccess(String sagaId, String customerId) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId);
        if (saga == null) return;

        log.info("Customer validation successful for saga: {}", sagaId);
        updateSagaStep(saga, "CUSTOMER_VALIDATION", OrderSaga.SagaStepStatus.COMPLETED);
        
        // Proceed to inventory reservation
        executeInventoryReservationStep(saga);
    }

    public void handleCustomerValidationFailure(String sagaId, String errorMessage) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId);
        if (saga == null) return;

        log.error("Customer validation failed for saga: {}: {}", sagaId, errorMessage);
        updateSagaStep(saga, "CUSTOMER_VALIDATION", OrderSaga.SagaStepStatus.FAILED);
        handleSagaFailure(saga, errorMessage);
    }

    private void executeInventoryReservationStep(OrderSaga saga) {
        try {
            inventoryReservationCircuitBreaker.executeSupplier(() -> {
                log.info("Reserving inventory for saga: {}", saga.getSagaId());
                
                InventoryReservationEvent event = InventoryReservationEvent.builder()
                        .sagaId(saga.getSagaId())
                        .products(saga.getProducts())
                        .build();
                
                kafkaTemplate.send("inventory-reservation-topic", event);
                return true;
            });

            addSagaStep(saga, "INVENTORY_RESERVATION", OrderSaga.SagaStepStatus.PENDING);
            updateSagaStatus(saga, SagaStatus.INVENTORY_RESERVED);
            
        } catch (Exception e) {
            log.error("Inventory reservation failed for saga: {}", saga.getSagaId(), e);
            startCompensation(saga, "Inventory reservation failed: " + e.getMessage());
        }
    }

    public void handleInventoryReservationSuccess(String sagaId) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId);
        if (saga == null) return;

        log.info("Inventory reservation successful for saga: {}", sagaId);
        updateSagaStep(saga, "INVENTORY_RESERVATION", OrderSaga.SagaStepStatus.COMPLETED);
        
        // Proceed to payment processing
        executePaymentProcessingStep(saga);
    }

    public void handleInventoryReservationFailure(String sagaId, String errorMessage) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId);
        if (saga == null) return;

        log.error("Inventory reservation failed for saga: {}: {}", sagaId, errorMessage);
        updateSagaStep(saga, "INVENTORY_RESERVATION", OrderSaga.SagaStepStatus.FAILED);
        startCompensation(saga, errorMessage);
    }

    private void executePaymentProcessingStep(OrderSaga saga) {
        try {
            paymentServiceCircuitBreaker.executeSupplier(() -> {
                log.info("Processing payment for saga: {}", saga.getSagaId());
                
                PaymentProcessingEvent event = PaymentProcessingEvent.builder()
                        .sagaId(saga.getSagaId())
                        .customerId(saga.getCustomerId())
                        .amount(calculateTotalAmount(saga.getProducts()))
                        .paymentMethod(saga.getPaymentMethod())
                        .build();
                
                kafkaTemplate.send("payment-processing-topic", event);
                return true;
            });

            addSagaStep(saga, "PAYMENT_PROCESSING", OrderSaga.SagaStepStatus.PENDING);
            updateSagaStatus(saga, SagaStatus.PAYMENT_PROCESSED);
            
        } catch (Exception e) {
            log.error("Payment processing failed for saga: {}", saga.getSagaId(), e);
            startCompensation(saga, "Payment processing failed: " + e.getMessage());
        }
    }

    public void handlePaymentProcessingSuccess(String sagaId, Integer paymentId) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId);
        if (saga == null) return;

        log.info("Payment processing successful for saga: {}", sagaId);
        updateSagaStep(saga, "PAYMENT_PROCESSING", OrderSaga.SagaStepStatus.COMPLETED);
        saga.getSagaData().put("paymentId", paymentId);
        
        // Complete the saga
        completeSaga(saga);
    }

    public void handlePaymentProcessingFailure(String sagaId, String errorMessage) {
        OrderSaga saga = sagaRepository.findBySagaId(sagaId);
        if (saga == null) return;

        log.error("Payment processing failed for saga: {}: {}", sagaId, errorMessage);
        updateSagaStep(saga, "PAYMENT_PROCESSING", OrderSaga.SagaStepStatus.FAILED);
        startCompensation(saga, errorMessage);
    }

    private void completeSaga(OrderSaga saga) {
        log.info("Completing saga: {}", saga.getSagaId());
        
        OrderCompletionEvent event = OrderCompletionEvent.builder()
                .sagaId(saga.getSagaId())
                .orderId(saga.getOrderId())
                .customerId(saga.getCustomerId())
                .products(saga.getProducts())
                .paymentId((Integer) saga.getSagaData().get("paymentId"))
                .build();
        
        kafkaTemplate.send("order-completion-topic", event);
        
        updateSagaStatus(saga, SagaStatus.COMPLETED);
        saga.setEndTime(LocalDateTime.now());
        sagaRepository.save(saga);
        
        log.info("Saga completed successfully: {}", saga.getSagaId());
    }

    private void startCompensation(OrderSaga saga, String errorMessage) {
        sagaCompensationCircuitBreaker.executeSupplier(() -> {
            log.warn("Starting compensation for saga: {} due to: {}", saga.getSagaId(), errorMessage);
            
            saga.setStatus(SagaStatus.COMPENSATING);
            saga.setErrorMessage(errorMessage);
            sagaRepository.save(saga);

            // Compensate in reverse order
            if (isStepCompleted(saga, "PAYMENT_PROCESSING")) {
                compensatePayment(saga);
            } else if (isStepCompleted(saga, "INVENTORY_RESERVATION")) {
                compensateInventoryReservation(saga);
            } else {
                // No compensation needed, just mark as cancelled
                updateSagaStatus(saga, SagaStatus.CANCELLED);
                saga.setEndTime(LocalDateTime.now());
                sagaRepository.save(saga);
            }
            return true;
        });
    }

    private void compensatePayment(OrderSaga saga) {
        sagaCompensationCircuitBreaker.executeSupplier(() -> {
            log.info("Compensating payment for saga: {}", saga.getSagaId());
            
            PaymentCompensationEvent event = PaymentCompensationEvent.builder()
                    .sagaId(saga.getSagaId())
                    .paymentId((Integer) saga.getSagaData().get("paymentId"))
                    .build();
            
            kafkaTemplate.send("payment-compensation-topic", event);
            return true;
        });
    }

    private void compensateInventoryReservation(OrderSaga saga) {
        sagaCompensationCircuitBreaker.executeSupplier(() -> {
            log.info("Compensating inventory reservation for saga: {}", saga.getSagaId());
            
            InventoryCompensationEvent event = InventoryCompensationEvent.builder()
                    .sagaId(saga.getSagaId())
                    .products(saga.getProducts())
                    .build();
            
            kafkaTemplate.send("inventory-compensation-topic", event);
            return true;
        });
    }

    private void handleSagaFailure(OrderSaga saga, String errorMessage) {
        log.error("Saga failed: {} - {}", saga.getSagaId(), errorMessage);
        
        saga.setStatus(SagaStatus.FAILED);
        saga.setErrorMessage(errorMessage);
        saga.setEndTime(LocalDateTime.now());
        sagaRepository.save(saga);
    }

    private void addSagaStep(OrderSaga saga, String stepName, OrderSaga.SagaStepStatus status) {
        OrderSaga.SagaStep step = OrderSaga.SagaStep.builder()
                .stepName(stepName)
                .status(status)
                .executedAt(LocalDateTime.now())
                .build();
        
        saga.getSteps().add(step);
        sagaRepository.save(saga);
    }

    private void updateSagaStep(OrderSaga saga, String stepName, OrderSaga.SagaStepStatus status) {
        saga.getSteps().stream()
                .filter(step -> step.getStepName().equals(stepName))
                .findFirst()
                .ifPresent(step -> step.setStatus(status));
        
        sagaRepository.save(saga);
    }

    private void updateSagaStatus(OrderSaga saga, SagaStatus status) {
        saga.setStatus(status);
        sagaRepository.save(saga);
    }

    private boolean isStepCompleted(OrderSaga saga, String stepName) {
        return saga.getSteps().stream()
                .anyMatch(step -> step.getStepName().equals(stepName) && 
                         step.getStatus() == OrderSaga.SagaStepStatus.COMPLETED);
    }

    private Double calculateTotalAmount(List<OrderSaga.OrderLineRequest> products) {
        // This would typically calculate from product prices
        return products.stream()
                .mapToDouble(p -> p.getQuantity() * 100.0) // Placeholder calculation
                .sum();
    }
}
