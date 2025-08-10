package com.pramithamj.ecommerce.payment;

import com.pramithamj.ecommerce.customer.CustomerResponse;
import com.pramithamj.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    Integer orderId,
    String orderReference,
    CustomerResponse customer
) {
}
