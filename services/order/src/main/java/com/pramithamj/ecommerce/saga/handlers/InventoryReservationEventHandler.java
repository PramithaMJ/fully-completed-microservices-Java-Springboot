package com.pramithamj.ecommerce.saga.handlers;

import com.pramithamj.ecommerce.saga.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationEventHandler {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @KafkaListener(topics = "inventory-reservation-response", groupId = "order-saga-group")
    public void handleInventoryReservationResponse(InventoryReservationResponse response) {
        log.info("Received inventory reservation response for saga: {}", response.getSagaId());
        
        if (response.isReserved()) {
            sagaOrchestrator.handleInventoryReservationSuccess(response.getSagaId());
        } else {
            sagaOrchestrator.handleInventoryReservationFailure(response.getSagaId(), response.getErrorMessage());
        }
    }

    public static class InventoryReservationResponse {
        private String sagaId;
        private boolean reserved;
        private String errorMessage;

        // Getters and setters
        public String getSagaId() { return sagaId; }
        public void setSagaId(String sagaId) { this.sagaId = sagaId; }
        
        public boolean isReserved() { return reserved; }
        public void setReserved(boolean reserved) { this.reserved = reserved; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
