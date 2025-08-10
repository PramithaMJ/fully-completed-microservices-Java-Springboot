package com.pramithamj.ecommerce.kafka;

import com.pramithamj.ecommerce.customer.CustomerResponse;
import com.pramithamj.ecommerce.order.PaymentMethod;
import com.pramithamj.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation (
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products

) {
}
