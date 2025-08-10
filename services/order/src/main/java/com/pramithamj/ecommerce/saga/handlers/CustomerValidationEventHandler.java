package com.pramithamj.ecommerce.saga.handlers;

import com.pramithamj.ecommerce.saga.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerValidationEventHandler {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @KafkaListener(topics = "customer-validation-response", groupId = "order-saga-group")
    public void handleCustomerValidationResponse(CustomerValidationResponse response) {
        log.info("Received customer validation response for saga: {}", response.getSagaId());
        
        if (response.isValid()) {
            sagaOrchestrator.handleCustomerValidationSuccess(response.getSagaId(), response.getCustomerId());
        } else {
            sagaOrchestrator.handleCustomerValidationFailure(response.getSagaId(), response.getErrorMessage());
        }
    }

    public static class CustomerValidationResponse {
        private String sagaId;
        private String customerId;
        private boolean valid;
        private String errorMessage;

        // Getters and setters
        public String getSagaId() { return sagaId; }
        public void setSagaId(String sagaId) { this.sagaId = sagaId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
