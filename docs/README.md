# Microservices Architecture Learning Platform

An interactive web-based learning platform that explains microservices architecture through a complete e-commerce implementation using Spring Boot and Java, featuring advanced patterns like Saga orchestration, Circuit Breakers, and Kubernetes deployment strategies.

## 🚀 Features

### Interactive Learning Experience
- **Visual Architecture Diagram**: Click on any service to see detailed information
- **Step-by-Step Explanations**: Understand how each component works
- **Hands-On Testing**: Real API examples and testing scenarios
- **Progress Tracking**: Track your learning progress through sections
- **GitHub Integration**: Direct access to complete source code
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Circuit Breaker Demonstrations**: Interactive circuit breaker pattern visualization
- **Saga Pattern Examples**: Learn distributed transaction management
- **Deployment Scenarios**: Kubernetes and Helm chart explanations

### Complete Source Code Access
- **GitHub Repository**: Direct links to the complete implementation
- **Author Information**: Learn about the creator and get support
- **Quick Actions**: Fork, download, or report issues directly from the platform
- **Live Repository Stats**: See the latest information about the codebase

### Comprehensive Coverage
- **8 Microservices**: Customer, Product, Order, Payment, Notification, Config Server, Discovery, and Gateway
- **Advanced Patterns**: Saga orchestration, Circuit Breakers, Event-Driven Architecture, Service Registry, API Gateway
- **12+ Technologies**: Spring Boot, Spring Cloud, PostgreSQL, MongoDB, Kafka, Docker, Zipkin, Kubernetes, Helm, Resilience4j, and more
- **Deployment Strategies**: Blue-green deployment, rolling updates, canary deployments

### Real-World Implementation
- **Business Problem**: Based on actual e-commerce requirements
- **Complete Solution**: From customer management to payment processing
- **Production-Ready**: Includes monitoring, tracing, resilience patterns, and Kubernetes deployment
- **Enterprise-Grade**: Saga patterns for distributed transactions, circuit breakers for fault tolerance

## 📁 File Structure

```
learning-platform/
├── index.html          # Main HTML file with all content
├── styles.css          # Complete CSS styling
├── script.js           # Interactive JavaScript functionality
└── README.md           # This documentation
```

## 🛠️ How to Use

### 1. Open the Learning Platform
Simply open `index.html` in your web browser:
```bash
open index.html
```
Or use a local server:
```bash
# Using Python
python -m http.server 8000

# Using Node.js
npx serve .

# Then visit http://localhost:8000
```

### 2. Navigate Through Sections
- **Overview**: Understand the business problem and solution
- **Architecture**: Interactive diagram of all services
- **Repository**: Access complete source code and GitHub integration
- **Services**: Deep dive into each microservice
- **Patterns**: Learn microservices design patterns including Saga and Circuit Breaker
- **Deployment**: Kubernetes, Helm charts, and blue-green deployment strategies
- **Hands-On**: Step-by-step implementation guide

### 3. Interactive Features
- **Click service boxes** to see detailed information
- **Copy code examples** with one click
- **Follow hands-on tutorials** for practical learning
- **Track your progress** with the built-in progress bar

## 🎯 Learning Objectives

After completing this platform, you will understand:

1. **Microservices Architecture Principles**
   - Service decomposition strategies
   - Inter-service communication
   - Data management patterns

2. **Spring Cloud Ecosystem**
   - Config Server for centralized configuration
   - Eureka for service discovery
   - Gateway for API routing

3. **Event-Driven Architecture**
   - Kafka for asynchronous messaging
   - Event sourcing patterns
   - Notification systems
   - Saga orchestration patterns

4. **Resilience Patterns**
   - Circuit breakers with Resilience4j
   - Saga pattern for distributed transactions
   - Compensation and recovery strategies
   - Fault tolerance mechanisms

5. **Production Concerns**
   - Distributed tracing with Zipkin
   - Database per service pattern
   - Monitoring and observability

6. **Kubernetes Deployment**
   - Container orchestration strategies
   - Helm charts for package management
   - Blue-green deployment processes
   - Service mesh considerations

7. **Hands-On Implementation**
   - Setting up the complete system
   - Testing API endpoints
   - Viewing distributed traces
   - Deploying to Kubernetes

## 🔧 Technical Implementation

### Technologies Explained

#### Core Services
- **Config Server (Port 8888)**: Centralized configuration management
- **Discovery Service (Port 8761)**: Service registry using Netflix Eureka
- **API Gateway (Port 8222)**: Single entry point with Spring Cloud Gateway

#### Business Services
- **Customer Service**: MongoDB for flexible customer profiles with validation circuit breakers
- **Product Service**: PostgreSQL for structured product data
- **Order Service**: Orchestrates order workflow with Saga patterns and multiple circuit breakers
- **Payment Service**: Handles secure payment processing with compensation logic
- **Notification Service**: Event-driven email notifications

#### Infrastructure
- **PostgreSQL**: Relational database for transactional data
- **MongoDB**: Document database for flexible schemas
- **Apache Kafka**: Event streaming platform
- **Zipkin**: Distributed tracing system
- **Docker Compose**: Container orchestration
- **Kubernetes**: Production container orchestration
- **Helm**: Kubernetes package manager
- **Resilience4j**: Circuit breakers and resilience patterns

### Design Patterns Demonstrated

1. **Service Registry & Discovery**
   - Implementation: Netflix Eureka
   - Benefits: Dynamic service location, load balancing, health monitoring

2. **API Gateway**
   - Implementation: Spring Cloud Gateway
   - Benefits: Single entry point, cross-cutting concerns, request routing

3. **Database per Service**
   - Implementation: PostgreSQL + MongoDB
   - Benefits: Data isolation, technology diversity, independent scaling

4. **Event-Driven Architecture**
   - Implementation: Apache Kafka
   - Benefits: Loose coupling, asynchronous processing, scalability

5. **Externalized Configuration**
   - Implementation: Spring Cloud Config
   - Benefits: Environment-specific configs, runtime updates, security

6. **Distributed Tracing**
   - Implementation: Zipkin
   - Benefits: Performance monitoring, debugging, dependency visualization

7. **Saga Orchestration Pattern**
   - Implementation: OrderSagaOrchestrator with compensation logic
   - Benefits: Distributed transaction management, data consistency, failure recovery

8. **Circuit Breaker Pattern**
   - Implementation: Resilience4j with specialized saga circuit breakers
   - Benefits: Fault tolerance, graceful degradation, system stability

9. **Blue-Green Deployment**
   - Implementation: Kubernetes deployment strategies
   - Benefits: Zero-downtime deployments, quick rollback, risk mitigation

10. **Package Management**
    - Implementation: Helm charts for Kubernetes
    - Benefits: Templated deployments, version management, configuration management

## 🧪 Testing Scenarios

The platform includes practical testing scenarios:

### 1. Create Customer
```bash
curl -X POST http://localhost:8222/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "John",
    "lastname": "Doe",
    "email": "john.doe@example.com",
    "address": {
      "street": "123 Main St",
      "houseNumber": "123",
      "zipCode": "12345"
    }
  }'
```

### 2. Add Product
```bash
curl -X POST http://localhost:8222/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro",
    "description": "Apple MacBook Pro 16-inch",
    "availableQuantity": 10,
    "price": 2499.99
  }'
```

### 3. Simulate Saga Compensation
```bash
# Trigger a failed order to see saga compensation in action
curl -X POST http://localhost:8222/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "invalid_customer_id",
    "products": [
      {
        "productId": "product_id_here",
        "quantity": 999
      }
    ],
    "paymentMethod": "PAYPAL"
  }'
```

### 4. Circuit Breaker Testing
```bash
# Test circuit breaker resilience by making multiple rapid requests
for i in {1..10}; do
  curl -X GET http://localhost:8222/api/customers/invalid_id
  sleep 0.1
done
```

## 📊 Monitoring Dashboards

Access these URLs once your microservices are running:

- **Eureka Discovery**: http://localhost:8761 - Service registry dashboard
- **PostgreSQL Admin**: http://localhost:5050 - Database administration
- **MongoDB Admin**: http://localhost:8081 - MongoDB management
- **Zipkin Tracing**: http://localhost:9411 - Distributed tracing visualization
- **Email Testing**: http://localhost:1080 - Email notification testing
- **Circuit Breaker Metrics**: Available through Spring Boot Actuator endpoints

## 🐳 Kubernetes Deployment

### Prerequisites for Kubernetes
- Kubernetes cluster (local with minikube or cloud provider)
- kubectl CLI tool
- Helm 3.x
- Docker registry access

### Deploy with Helm

1. **Add Helm Repository** (if using external charts):
   ```bash
   helm repo add microservices-chart ./helm-charts
   helm repo update
   ```

2. **Deploy Infrastructure**:
   ```bash
   helm install postgres stable/postgresql
   helm install mongodb stable/mongodb
   helm install kafka strimzi/strimzi-kafka-operator
   ```

3. **Deploy Microservices**:
   ```bash
   # Deploy each service with Helm
   helm install config-server ./helm-charts/config-server
   helm install discovery-service ./helm-charts/discovery
   helm install api-gateway ./helm-charts/gateway
   
   # Deploy business services
   helm install customer-service ./helm-charts/customer
   helm install product-service ./helm-charts/product
   helm install order-service ./helm-charts/order
   helm install payment-service ./helm-charts/payment
   helm install notification-service ./helm-charts/notification
   ```

### Blue-Green Deployment

1. **Prepare Green Environment**:
   ```bash
   kubectl apply -f k8s/green-deployment.yaml
   ```

2. **Switch Traffic**:
   ```bash
   kubectl patch service api-gateway -p '{"spec":{"selector":{"version":"green"}}}'
   ```

3. **Rollback if Needed**:
   ```bash
   kubectl patch service api-gateway -p '{"spec":{"selector":{"version":"blue"}}}'
   ```

## 🚀 Getting Started with the Actual System

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- IDE (IntelliJ IDEA or VS Code)
- kubectl (for Kubernetes deployment)
- Helm 3.x (for package management)

### Local Development Setup

1. **Start Infrastructure**:
   ```bash
   docker-compose up -d
   ```

2. **Start Core Services** (in order):
   ```bash
   # Config Server first
   cd services/config-server && mvn spring-boot:run
   
   # Discovery Service
   cd services/discovery && mvn spring-boot:run
   
   # Gateway
   cd services/gateway && mvn spring-boot:run
   ```

3. **Start Business Services**:
   ```bash
   # Customer Service
   cd services/customer && mvn spring-boot:run
   
   # Product Service
   cd services/product && mvn spring-boot:run
   
   # Order Service (with Saga orchestration)
   cd services/order && mvn spring-boot:run
   
   # Payment Service
   cd services/payment && mvn spring-boot:run
   
   # Notification Service
   cd services/notification && mvn spring-boot:run
   ```

### Production Kubernetes Setup

1. **Build Container Images**:
   ```bash
   # Build all service images
   docker build -t pramithamj/config-server:latest services/config-server/
   docker build -t pramithamj/discovery:latest services/discovery/
   docker build -t pramithamj/gateway:latest services/gateway/
   docker build -t pramithamj/customer:latest services/customer/
   docker build -t pramithamj/product:latest services/product/
   docker build -t pramithamj/order:latest services/order/
   docker build -t pramithamj/payment:latest services/payment/
   docker build -t pramithamj/notification:latest services/notification/
   ```

2. **Deploy to Kubernetes**:
   ```bash
   # Apply namespace
   kubectl create namespace microservices
   
   # Deploy infrastructure
   kubectl apply -f k8s/infrastructure/ -n microservices
   
   # Deploy services
   kubectl apply -f k8s/services/ -n microservices
   
   # Or use Helm
   helm install microservices-stack ./helm-charts/ -n microservices
   ```

## 💡 Learning Tips

1. **Start with the Overview** to understand the business context
2. **Explore the Architecture** interactively before diving into code
3. **Read each Service section** to understand responsibilities
4. **Study the Design Patterns** to grasp architectural concepts including Saga and Circuit Breaker patterns
5. **Learn the Deployment section** to understand Kubernetes and Helm deployment strategies
6. **Follow the Hands-On guide** to see everything in action
7. **Use the monitoring tools** to observe system behavior
8. **Test circuit breakers** by simulating failures
9. **Observe saga compensation** during failed transactions
10. **Practice blue-green deployment** in a safe environment

## 🎨 Customization

### Adding New Content
- Edit `index.html` to add new sections
- Update `serviceDetails` in `script.js` for new services
- Modify `styles.css` for visual customizations

### Extending Functionality
- Add new interactive features in `script.js`
- Create additional testing scenarios
- Integrate with actual running services for live data

## 📱 Mobile Experience

The platform is fully responsive and works on:
- Desktop computers
- Tablets
- Mobile phones

Navigation is optimized for touch interfaces with collapsible menus and touch-friendly buttons.

## 🎓 Educational Value

This platform serves as:
- **University coursework** for distributed systems and microservices architecture
- **Professional training** for enterprise microservices adoption
- **Self-study resource** for developers learning modern patterns
- **Architecture reference** for teams implementing saga patterns and circuit breakers
- **Interview preparation** for system design and microservices questions
- **DevOps training** for Kubernetes and Helm deployment strategies
- **Resilience engineering** education for fault-tolerant system design

## 📚 Advanced Topics Covered

### Saga Pattern Implementation
- **Orchestration vs Choreography**: Learn when to use each approach
- **Compensation Logic**: Understand rollback mechanisms for distributed transactions
- **Saga Recovery**: Handle partial failures and system resilience

### Circuit Breaker Patterns
- **Failure Thresholds**: Configure appropriate failure rates for different scenarios
- **Fallback Strategies**: Implement graceful degradation when services fail
- **Recovery Mechanisms**: Automatic and manual circuit breaker recovery

### Kubernetes Deployment Strategies
- **Blue-Green Deployments**: Zero-downtime deployment strategies
- **Rolling Updates**: Gradual service updates with minimal disruption
- **Canary Releases**: Risk-mitigated feature rollouts

### Observability and Monitoring
- **Distributed Tracing**: Track requests across multiple services
- **Circuit Breaker Metrics**: Monitor system resilience patterns
- **Saga Transaction Monitoring**: Observe distributed transaction flows

## 📞 Support

For questions about the microservices implementation:
1. Review the business needs in `resources/business needs.txt`
2. Check the curriculum in `resources/curriculum.txt`
3. Study the patterns in `resources/distributed patterns.txt`
4. Explore the source code in the `services/` directory

## 🔄 Updates

This learning platform includes the latest features:

### Recently Added
- **Saga Orchestration Patterns**: Complete implementation with compensation logic
- **Circuit Breaker Integration**: Five specialized circuit breakers for different scenarios
- **Kubernetes Deployment Guide**: Step-by-step production deployment instructions
- **Helm Charts Documentation**: Package management for Kubernetes deployments
- **Blue-Green Deployment**: Zero-downtime deployment strategies
- **Interactive Circuit Breaker Demo**: Visual learning tool for resilience patterns

### Upcoming Features
- Additional microservices patterns (CQRS, Event Sourcing)
- Advanced monitoring examples with Prometheus and Grafana
- Performance optimization techniques and load testing
- Security implementation details (OAuth2, JWT, API Security)
- Service mesh implementation with Istio
- Cloud-native deployment strategies (AWS EKS, Google GKE, Azure AKS)
- GitOps workflows with ArgoCD

---

**Happy Learning! 🚀**

Master modern microservices architecture with advanced patterns, resilience strategies, and production-ready Kubernetes deployment through this comprehensive, interactive learning experience.
