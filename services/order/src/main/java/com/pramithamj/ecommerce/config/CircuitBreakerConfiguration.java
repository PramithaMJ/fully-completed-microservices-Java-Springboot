package com.pramithamj.ecommerce.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public CircuitBreaker customerServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(30000))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .build();
        
        return registry.circuitBreaker("customer-service", config);
    }

    @Bean
    public CircuitBreaker productServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(30000))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .build();
        
        return registry.circuitBreaker("product-service", config);
    }

    @Bean
    public CircuitBreaker paymentServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofMillis(60000))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .slowCallRateThreshold(40)
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .build();
        
        return registry.circuitBreaker("payment-service", config);
    }

    // Saga-specific Circuit Breakers
    @Bean
    public CircuitBreaker sagaOrchestrationCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofMillis(45000))
                .slidingWindowSize(15)
                .minimumNumberOfCalls(8)
                .slowCallRateThreshold(45)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
        
        return registry.circuitBreaker("saga-orchestration", config);
    }

    @Bean
    public CircuitBreaker sagaCompensationCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(25)
                .waitDurationInOpenState(Duration.ofMillis(30000))
                .slidingWindowSize(12)
                .minimumNumberOfCalls(6)
                .slowCallRateThreshold(35)
                .slowCallDurationThreshold(Duration.ofSeconds(4))
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        
        return registry.circuitBreaker("saga-compensation", config);
    }

    @Bean
    public CircuitBreaker sagaRecoveryCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofMillis(90000))
                .slidingWindowSize(8)
                .minimumNumberOfCalls(4)
                .slowCallRateThreshold(55)
                .slowCallDurationThreshold(Duration.ofSeconds(6))
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        
        return registry.circuitBreaker("saga-recovery", config);
    }

    @Bean
    public CircuitBreaker inventoryReservationCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(45)
                .waitDurationInOpenState(Duration.ofMillis(35000))
                .slidingWindowSize(12)
                .minimumNumberOfCalls(7)
                .slowCallRateThreshold(40)
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .permittedNumberOfCallsInHalfOpenState(4)
                .build();
        
        return registry.circuitBreaker("inventory-reservation", config);
    }

    @Bean
    public CircuitBreaker customerValidationCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(35)
                .waitDurationInOpenState(Duration.ofMillis(25000))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .slowCallRateThreshold(30)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        
        return registry.circuitBreaker("customer-validation", config);
    }
}
