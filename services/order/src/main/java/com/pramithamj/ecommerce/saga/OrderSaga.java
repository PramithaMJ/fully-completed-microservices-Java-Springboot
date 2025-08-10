package com.pramithamj.ecommerce.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "order_saga")
public class OrderSaga {
    
    @Id
    private String sagaId;
    private Integer orderId;
    private String customerId;
    private List<OrderLineRequest> products;
    private String paymentMethod;
    private SagaStatus status;
    private Map<String, Object> sagaData;
    private List<SagaStep> steps;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private Integer retryCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineRequest {
        private Integer productId;
        private Integer quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SagaStep {
        private String stepName;
        private SagaStepStatus status;
        private LocalDateTime executedAt;
        private String errorMessage;
        private Object stepData;
    }
    
    public enum SagaStepStatus {
        PENDING,
        COMPLETED,
        COMPENSATED,
        FAILED
    }
}
