package com.pramithamj.ecommerce.saga;

public enum SagaStatus {
    STARTED,
    CUSTOMER_VALIDATED,
    INVENTORY_RESERVED,
    PAYMENT_PROCESSED,
    ORDER_CONFIRMED,
    COMPLETED,
    COMPENSATING,
    CANCELLED,
    FAILED
}
