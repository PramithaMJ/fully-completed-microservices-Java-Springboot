package com.pramithamj.ecommerce.saga.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessingEvent {
    private String sagaId;
    private String customerId;
    private Double amount;
    private String paymentMethod;
}
