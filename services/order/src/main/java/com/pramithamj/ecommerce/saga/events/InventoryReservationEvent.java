package com.pramithamj.ecommerce.saga.events;

import com.pramithamj.ecommerce.saga.OrderSaga;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationEvent {
    private String sagaId;
    private List<OrderSaga.OrderLineRequest> products;
}
