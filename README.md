# 🏗️ Complete Microservices Architecture with Advanced Patterns

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)](https://spring.io/projects/spring-cloud)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.28+-blue)](https://kubernetes.io/)
[![Docker](https://img.shields.io/badge/Docker-24.0+-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> A comprehensive, production-ready microservices implementation featuring **Circuit Breaker Pattern**, **Saga Pattern**, and **Blue-Green Deployment** with Kubernetes orchestration.

## 🎯 Project Overview

This project demonstrates enterprise-grade microservices architecture patterns through a complete e-commerce platform implementation. It showcases advanced distributed systems patterns essential for building resilient, scalable applications.

### 🏆 Key Features

- ✅ **6 Production-Ready Microservices**
- ✅ **Circuit Breaker Pattern** with Resilience4j
- ✅ **Saga Pattern** for distributed transactions
- ✅ **Blue-Green Deployment** with Kubernetes
- ✅ **Event-Driven Architecture** with Kafka
- ✅ **Distributed Tracing** with Zipkin
- ✅ **Interactive Learning Platform**
- ✅ **Zero-Downtime Deployments**

## 🏗️ Advanced Patterns Implemented

### 🛡️ Circuit Breaker Pattern
- **Technology**: Resilience4j
- **Implementation**: All inter-service communications
- **Features**: Configurable thresholds, automatic recovery, fallback strategies
- **Monitoring**: Prometheus metrics integration

### 🔄 Saga Pattern (Orchestration)
- **Coordinator**: OrderSagaOrchestrator
- **State Storage**: MongoDB
- **Event Bus**: Apache Kafka
- **Features**: Automatic compensation, timeout handling, retry mechanisms

### 🔵🟢 Blue-Green Deployment
- **Platform**: Kubernetes with Helm
- **Features**: Zero-downtime deployment, instant rollback, canary support
- **Automation**: Custom deployment scripts with health checks

## Overview

This repository contains a collection of fully completed microservices built with Spring Boot version 3.2.5 and Java 17. The project utilizes Spring Cloud version 2023.0.1 for implementing various distributed system patterns and features.

## Microservices Description

1. **Config Server**
   - Provides centralized configuration for all microservices.
   - Uses Spring Cloud Config Server.

2. **Customer Service**
   - Manages customer data and operations.
   - Integrated with Eureka Discovery.

3. **Discovery Service**
   - Service registry using Netflix Eureka.
   - Enables service discovery for other microservices.

4. **Gateway Service**
   - API Gateway for routing requests to appropriate microservices.
   - Uses Spring Cloud Gateway.
   - Includes distributed tracing and circuit breaker.

5. **Notification Service**
   - Handles notifications and alerts.
   - Uses Kafka for messaging.

6. **Order Service**
   - Manages orders and their statuses.
   - Integrated with Eureka Discovery.

7. **Payment Service**
   - Processes payments.
   - Uses Eureka Discovery and Zipkin for tracing.

8. **Product Service**
   - Manages product information.
   - Integrated with Eureka Discovery.

## Features

- **Service Discovery**: All microservices register with the Eureka server for easy discovery.
- **Centralized Configuration**: Configurations are managed centrally using the Spring Cloud Config Server.
- **API Gateway**: Spring Cloud Gateway is used for routing and handling cross-cutting concerns like security, monitoring, and resilience.
- **Distributed Tracing**: Zipkin is used for tracing requests across microservices.
- **Circuit Breaker**: Circuit breaking capabilities provided by Spring Cloud Circuit Breaker.
- **Messaging**: Kafka is used for asynchronous communication between microservices.

## Prerequisites

- Java 17 or later
- Maven or Gradle
- Docker (optional, for containerized deployment)

## Running the Microservices

1. **Clone the repository**
   ```sh
   git clone https://github.com/PramithaMJ/fully-completed-microservices.git
   cd fully-completed-microservices
   ```

2. **Start Config Server**
   ```sh
   cd config-server
   mvn spring-boot:run
   ```

3. **Start Discovery Service**
   ```sh
   cd discovery
   mvn spring-boot:run
   ```

4. **Start Other Microservices**
   Start the remaining microservices in any order. Ensure they are configured to register with the Discovery Service.

   ```sh
   cd <microservice-name>
   mvn spring-boot:run
   ```

## Configuration

Each microservice has its configuration properties defined in the `application.yml` or `application.properties` file. The Config Server properties should be specified in a central configuration repository.

## Deployment

To deploy the microservices using Docker, use the Dockerfile available in each microservice directory. You can build and run the Docker images as follows:

```sh
cd <microservice-name>
docker build -t <microservice-name>:latest .
docker run -d -p <port>:<container-port> <microservice-name>:latest
```

## Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License.

## Contact

For any questions or feedback, please open an issue in the repository.

Thank you,
Pramitha Jayasooriya
https://pramithamj.live

<!--
buy me a coffee
-->
## Donation

***If you like what I do, maybe consider buying me a coffee***

<a href="https://buymeacoffee.com/lpramithamm"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-red.png" alt="Buy Me A Coffee" style="height: 35px !important; width: 120px !important;"></a>

***
