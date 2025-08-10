package com.pramithamj.ecommerce.saga.handlers;

import com.pramithamj.ecommerce.saga.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @KafkaListener(topics = "payment-processing-response", groupId = "order-saga-group")
    public void handlePaymentProcessingResponse(PaymentProcessingResponse response) {
        log.info("Received payment processing response for saga: {}", response.getSagaId());
        
        if (response.isSuccess()) {
            sagaOrchestrator.handlePaymentProcessingSuccess(response.getSagaId(), response.getPaymentId());
        } else {
            sagaOrchestrator.handlePaymentProcessingFailure(response.getSagaId(), response.getErrorMessage());
        }
    }

    @KafkaListener(topics = "payment-compensation-response", groupId = "order-saga-group")
    public void handlePaymentCompensationResponse(PaymentCompensationResponse response) {
        log.info("Received payment compensation response for saga: {}", response.getSagaId());
        
        if (response.isSuccess()) {
            log.info("Payment compensation completed for saga: {}", response.getSagaId());
        } else {
            log.error("Payment compensation failed for saga: {}: {}", response.getSagaId(), response.getErrorMessage());
        }
    }

    public static class PaymentProcessingResponse {
        private String sagaId;
        private Integer paymentId;
        private boolean success;
        private String errorMessage;
        private Double amount;

        // Getters and setters
        public String getSagaId() { return sagaId; }
        public void setSagaId(String sagaId) { this.sagaId = sagaId; }
        
        public Integer getPaymentId() { return paymentId; }
        public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class PaymentCompensationResponse {
        private String sagaId;
        private Integer paymentId;
        private boolean success;
        private String errorMessage;

        // Getters and setters
        public String getSagaId() { return sagaId; }
        public void setSagaId(String sagaId) { this.sagaId = sagaId; }
        
        public Integer getPaymentId() { return paymentId; }
        public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
