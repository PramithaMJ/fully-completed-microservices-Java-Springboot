package com.pramithamj.ecommerce.saga;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderSagaRepository extends MongoRepository<OrderSaga, String> {
    OrderSaga findBySagaId(String sagaId);
    List<OrderSaga> findByStatus(SagaStatus status);
    List<OrderSaga> findByCustomerId(String customerId);
    List<OrderSaga> findByOrderId(Integer orderId);
    
    // For saga recovery
    List<OrderSaga> findByStatusInAndStartTimeBefore(List<SagaStatus> statuses, LocalDateTime threshold);
    List<OrderSaga> findByStatusInAndEndTimeBefore(List<SagaStatus> statuses, LocalDateTime threshold);
    
    // For monitoring and metrics
    long countByStatus(SagaStatus status);
    long countByStatusAndStartTimeAfter(SagaStatus status, LocalDateTime since);
}
