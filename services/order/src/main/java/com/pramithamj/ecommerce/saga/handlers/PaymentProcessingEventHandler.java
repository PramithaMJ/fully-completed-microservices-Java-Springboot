package com.pramithamj.ecommerce.saga.handlers;

import com.pramithamj.ecommerce.saga.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingEventHandler {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @KafkaListener(topics = "payment-processing-response", groupId = "order-saga-group")
    public void handlePaymentProcessingResponse(PaymentProcessingResponse response) {
        log.info("Received payment processing response for saga: {}", response.getSagaId());
        
        if (response.isProcessed()) {
            sagaOrchestrator.handlePaymentProcessingSuccess(response.getSagaId(), response.getPaymentId());
        } else {
            sagaOrchestrator.handlePaymentProcessingFailure(response.getSagaId(), response.getErrorMessage());
        }
    }

    public static class PaymentProcessingResponse {
        private String sagaId;
        private Integer paymentId;
        private boolean processed;
        private String errorMessage;

        // Getters and setters
        public String getSagaId() { return sagaId; }
        public void setSagaId(String sagaId) { this.sagaId = sagaId; }
        
        public Integer getPaymentId() { return paymentId; }
        public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
        
        public boolean isProcessed() { return processed; }
        public void setProcessed(boolean processed) { this.processed = processed; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
