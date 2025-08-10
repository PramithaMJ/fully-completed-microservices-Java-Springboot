# Kubernetes Deployment Guide for E-commerce Microservices

## Overview

This guide provides comprehensive instructions for deploying the e-commerce microservices platform on Kubernetes using Helm charts with advanced patterns including:

- **Circuit Breaker Pattern** with Resilience4j
- **Saga Pattern** for distributed transactions
- **Blue-Green Deployment** for zero-downtime deployments
- **Auto-scaling** and **Health Monitoring**
- **Security** with Network Policies

## Architecture

### Microservices Components

- **Discovery Service** (Eureka) - Service registration and discovery
- **Config Server** - Centralized configuration management
- **API Gateway** - Single entry point with routing and load balancing
- **Customer Service** - Customer management with circuit breakers
- **Product Service** - Product catalog with inventory management
- **Order Service** - Order processing with Saga orchestration
- **Payment Service** - Payment processing with circuit breakers
- **Notification Service** - Email and messaging notifications

### Infrastructure Components

- **MongoDB** - Document database for orders and saga state
- **PostgreSQL** - Relational database for customers and products
- **Apache Kafka** - Event streaming for saga pattern
- **Prometheus** - Metrics collection and monitoring
- **Grafana** - Metrics visualization

## Prerequisites

### Required Tools

```bash
# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Install Helm
brew install helm

# Install jq for JSON parsing
brew install jq
```

### Kubernetes Cluster

You need a Kubernetes cluster with:
- **Minimum 4 CPU cores and 8GB RAM**
- **LoadBalancer support** (for cloud providers)
- **Storage Class** for persistent volumes
- **Ingress Controller** (nginx recommended)

#### Local Development with Kind

```bash
# Install Kind
brew install kind

# Create cluster with ingress support
cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
EOF

# Install NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

# Wait for ingress controller to be ready
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

## Deployment Instructions

### 1. Clone and Prepare

```bash
git clone https://github.com/PramithaMJ/fully-completed-microservices.git
cd fully-completed-microservices/k8s
```

### 2. Build Docker Images

```bash
# Build all service images
./build-images.sh

# Or build individually
docker build -t ecommerce/discovery-service:latest ../services/discovery
docker build -t ecommerce/config-server:latest ../services/config-server
docker build -t ecommerce/api-gateway:latest ../services/gateway
docker build -t ecommerce/customer-service:latest ../services/customer
docker build -t ecommerce/product-service:latest ../services/product
docker build -t ecommerce/order-service:latest ../services/order
docker build -t ecommerce/payment-service:latest ../services/payment
docker build -t ecommerce/notification-service:latest ../services/notification
```

### 3. Deploy Infrastructure Dependencies

```bash
# Add Bitnami repository
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Create namespace
kubectl create namespace ecommerce

# Install dependencies
helm dependency update ./helm-charts
```

### 4. Deploy with Blue-Green Strategy

#### Option 1: Full Automated Deployment

```bash
# Deploy everything with blue-green strategy
./blue-green-deploy.sh full-deploy
```

#### Option 2: Step-by-Step Deployment

```bash
# Initial deployment (Blue slot)
helm install ecommerce-microservices ./helm-charts \
  --namespace ecommerce \
  --set bluegreen.enabled=true \
  --set bluegreen.activeSlot=blue \
  --timeout 10m \
  --wait

# Verify deployment
kubectl get pods -n ecommerce
kubectl get services -n ecommerce

# Deploy to Green slot
./blue-green-deploy.sh deploy

# Switch traffic to Green
./blue-green-deploy.sh switch

# Cleanup old Blue deployment
./blue-green-deploy.sh cleanup
```

### 5. Canary Deployment (Alternative)

```bash
# Start canary deployment with 10% traffic
./blue-green-deploy.sh canary 10

# Monitor metrics and logs
kubectl logs -f deployment/order-service-green -n ecommerce

# Increase traffic gradually
kubectl patch ingress ecommerce-microservices-canary -n ecommerce \
  -p '{"metadata":{"annotations":{"nginx.ingress.kubernetes.io/canary-weight":"50"}}}'

# Promote canary to full deployment
./blue-green-deploy.sh promote
```

## Configuration

### Circuit Breaker Settings

Circuit breakers are configured per service in `values.yaml`:

```yaml
order:
  circuitBreaker:
    enabled: true
    failureRateThreshold: 30    # 30% failure rate threshold
    waitDurationInOpenState: 60000  # 60 seconds wait in open state
    slidingWindowSize: 20       # 20 calls sliding window
```

### Saga Pattern Settings

Saga orchestration settings:

```yaml
order:
  saga:
    enabled: true
    retryAttempts: 3           # Maximum retry attempts
    compensationTimeout: 30000  # Compensation timeout in ms
```

### Scaling Configuration

Horizontal Pod Autoscaler settings:

```yaml
order:
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10
    targetCPUUtilizationPercentage: 70
```

## Monitoring and Observability

### Access Monitoring Dashboards

```bash
# Port forward to Grafana
kubectl port-forward service/grafana 3000:3000 -n ecommerce

# Access Grafana at http://localhost:3000
# Default credentials: admin/admin

# Port forward to Prometheus
kubectl port-forward service/prometheus 9090:9090 -n ecommerce

# Access Prometheus at http://localhost:9090
```

### Key Metrics to Monitor

#### Circuit Breaker Metrics
- `resilience4j_circuitbreaker_state` - Circuit breaker state (open/closed/half-open)
- `resilience4j_circuitbreaker_calls_total` - Total circuit breaker calls
- `resilience4j_circuitbreaker_failure_rate` - Failure rate percentage

#### Saga Pattern Metrics
- `saga_started_total` - Total saga transactions started
- `saga_completed_total` - Total saga transactions completed
- `saga_failed_total` - Total saga transactions failed
- `saga_compensated_total` - Total saga transactions compensated
- `saga_duration_seconds` - Saga transaction duration histogram

#### Application Metrics
- `http_server_requests_seconds` - HTTP request duration
- `jvm_memory_used_bytes` - JVM memory usage
- `kafka_producer_io_wait_time_ns_avg` - Kafka producer metrics

### Alerts Configuration

Key alerts are configured in Prometheus rules:

1. **CircuitBreakerOpen** - Triggers when circuit breaker opens
2. **SagaHighFailureRate** - High saga failure rate (>10%)
3. **ServiceDown** - Service unavailability
4. **HighMemoryUsage** - Memory usage >85%
5. **HighCPUUsage** - CPU usage >80%

## Testing the Deployment

### Health Checks

```bash
# Check all services health
kubectl get pods -n ecommerce

# Check specific service
kubectl describe pod -l app.kubernetes.io/component=order-service -n ecommerce

# Check service logs
kubectl logs -f deployment/order-service-blue -n ecommerce
```

### API Testing

```bash
# Get external IP
export GATEWAY_IP=$(kubectl get service api-gateway-blue -n ecommerce -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Test API endpoints
curl -X GET "http://$GATEWAY_IP:8080/api/v1/customers"
curl -X GET "http://$GATEWAY_IP:8080/api/v1/products"

# Test order creation (Saga Pattern)
curl -X POST "http://$GATEWAY_IP:8080/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "1",
    "products": [
      {"productId": 1, "quantity": 2}
    ],
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Circuit Breaker Testing

```bash
# Generate load to trigger circuit breaker
for i in {1..100}; do
  curl -X GET "http://$GATEWAY_IP:8080/api/v1/customers/999" & 
done

# Check circuit breaker metrics
curl -s "http://$GATEWAY_IP:8080/actuator/prometheus" | grep circuit
```

## Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check pod events
kubectl describe pod <pod-name> -n ecommerce

# Check service logs
kubectl logs <pod-name> -n ecommerce --previous

# Common causes:
# - Database connection issues
# - Configuration server unavailable
# - Resource constraints
```

#### 2. Circuit Breaker Not Working
```bash
# Verify circuit breaker configuration
kubectl get configmap order-service-config -n ecommerce -o yaml

# Check metrics endpoint
curl "http://<service-ip>:8070/actuator/prometheus" | grep resilience4j
```

#### 3. Saga Pattern Issues
```bash
# Check Kafka connectivity
kubectl exec -it <kafka-pod> -n ecommerce -- kafka-topics.sh --list --bootstrap-server localhost:9092

# Check saga state in MongoDB
kubectl exec -it <mongodb-pod> -n ecommerce -- mongo --eval "db.order_saga.find().pretty()"

# Check order service logs for saga execution
kubectl logs -f deployment/order-service-blue -n ecommerce | grep -i saga
```

#### 4. Blue-Green Deployment Issues
```bash
# Check deployment status
./blue-green-deploy.sh status

# Rollback if needed
./blue-green-deploy.sh rollback

# Check ingress configuration
kubectl get ingress -n ecommerce -o yaml
```

### Performance Tuning

#### 1. Resource Optimization
```yaml
# Adjust resource requests/limits in values.yaml
order:
  resources:
    requests:
      cpu: 500m
      memory: 512Mi
    limits:
      cpu: 1000m
      memory: 1Gi
```

#### 2. JVM Tuning
```yaml
# Add JVM options as environment variables
order:
  env:
    - name: JAVA_OPTS
      value: "-Xmx768m -XX:+UseG1GC -XX:G1HeapRegionSize=16m"
```

#### 3. Database Connection Pooling
```yaml
# Configure database connection pools
order:
  env:
    - name: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
      value: "20"
    - name: SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE
      value: "5"
```

## Security Best Practices

### 1. Network Policies
Network policies are enabled by default and restrict inter-service communication.

### 2. Secrets Management
All sensitive data is stored in Kubernetes secrets:
```bash
# View secrets (base64 encoded)
kubectl get secret ecommerce-microservices-secrets -n ecommerce -o yaml
```

### 3. RBAC Configuration
```yaml
# Create service account with minimal permissions
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ecommerce-service-account
  namespace: ecommerce
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: ecommerce
  name: ecommerce-role
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
```

## Backup and Disaster Recovery

### Database Backups
```bash
# MongoDB backup
kubectl exec <mongodb-pod> -n ecommerce -- mongodump --out /tmp/backup
kubectl cp ecommerce/<mongodb-pod>:/tmp/backup ./mongodb-backup

# PostgreSQL backup
kubectl exec <postgresql-pod> -n ecommerce -- pg_dump -U ecommerce ecommerce > postgresql-backup.sql
```

### Configuration Backups
```bash
# Backup Helm values
helm get values ecommerce-microservices -n ecommerce > values-backup.yaml

# Backup all Kubernetes resources
kubectl get all -n ecommerce -o yaml > kubernetes-backup.yaml
```

## Upgrading

### Application Updates
```bash
# Update application version
helm upgrade ecommerce-microservices ./helm-charts \
  --namespace ecommerce \
  --set order.image.tag=v1.1.0 \
  --reuse-values

# Blue-green upgrade
./blue-green-deploy.sh full-deploy
```

### Infrastructure Updates
```bash
# Update Helm dependencies
helm dependency update ./helm-charts

# Upgrade with new dependencies
helm upgrade ecommerce-microservices ./helm-charts \
  --namespace ecommerce \
  --reuse-values
```

## Production Checklist

- [ ] Resource limits configured appropriately
- [ ] Health checks configured and working
- [ ] Monitoring and alerting setup
- [ ] Network policies enabled
- [ ] Secrets properly managed
- [ ] Backup strategy implemented
- [ ] Disaster recovery plan documented
- [ ] Load testing completed
- [ ] Security scanning performed
- [ ] Documentation updated

## Support and Troubleshooting

For issues and support:
1. Check the troubleshooting section above
2. Review application logs: `kubectl logs -f deployment/<service-name> -n ecommerce`
3. Check cluster events: `kubectl get events -n ecommerce --sort-by='.lastTimestamp'`
4. Contact: **Pramitha Jayasooriya** - pramithajayasooriya@example.com

---

**Author**: Pramitha Jayasooriya  
**Version**: 1.0.0  
**Last Updated**: August 2025
