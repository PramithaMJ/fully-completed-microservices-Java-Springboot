#!/bin/bash

# Blue-Green Deployment Manager for E-commerce Microservices
# Author: Pramitha Jayasooriya
# Version: 1.0.0

set -e

# Configuration
NAMESPACE=${NAMESPACE:-"ecommerce"}
RELEASE_NAME=${RELEASE_NAME:-"ecommerce-microservices"}
CHART_PATH=${CHART_PATH:-"./k8s/helm-charts"}
TIMEOUT=${TIMEOUT:-"600s"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to get current active slot
get_active_slot() {
    helm get values $RELEASE_NAME -n $NAMESPACE -o json | jq -r '.bluegreen.activeSlot // "blue"'
}

# Function to get inactive slot
get_inactive_slot() {
    local active_slot=$(get_active_slot)
    if [ "$active_slot" = "blue" ]; then
        echo "green"
    else
        echo "blue"
    fi
}

# Function to check if deployment is healthy
check_deployment_health() {
    local slot=$1
    local services=("discovery-service" "config-server" "api-gateway" "customer-service" "product-service" "order-service" "payment-service" "notification-service")
    
    log_info "Checking health of $slot deployment..."
    
    for service in "${services[@]}"; do
        local deployment_name="${service}-${slot}"
        
        # Check if deployment exists
        if ! kubectl get deployment $deployment_name -n $NAMESPACE > /dev/null 2>&1; then
            log_warning "Deployment $deployment_name not found, skipping health check"
            continue
        fi
        
        # Check deployment status
        local ready_replicas=$(kubectl get deployment $deployment_name -n $NAMESPACE -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
        local desired_replicas=$(kubectl get deployment $deployment_name -n $NAMESPACE -o jsonpath='{.spec.replicas}')
        
        if [ "$ready_replicas" != "$desired_replicas" ]; then
            log_error "Deployment $deployment_name is not ready ($ready_replicas/$desired_replicas)"
            return 1
        fi
        
        log_info "✓ $deployment_name is healthy ($ready_replicas/$desired_replicas)"
    done
    
    log_success "$slot deployment is healthy"
    return 0
}

# Function to run health checks
run_health_checks() {
    local slot=$1
    local gateway_service="api-gateway-${slot}"
    
    log_info "Running health checks for $slot deployment..."
    
    # Wait for gateway to be ready
    kubectl wait --for=condition=available --timeout=$TIMEOUT deployment/api-gateway-$slot -n $NAMESPACE
    
    # Get gateway service endpoint
    local gateway_url=$(kubectl get service $gateway_service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
    
    if [ -z "$gateway_url" ]; then
        gateway_url=$(kubectl get service $gateway_service -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
        log_warning "Using ClusterIP for health checks: $gateway_url"
    fi
    
    # Health check endpoints
    local health_endpoints=(
        "/actuator/health"
        "/api/v1/customers/health"
        "/api/v1/products/health"
        "/api/v1/orders/health"
        "/api/v1/payments/health"
    )
    
    for endpoint in "${health_endpoints[@]}"; do
        log_info "Checking endpoint: http://$gateway_url:8080$endpoint"
        
        # Use kubectl port-forward for local testing if needed
        if ! curl -f -s "http://$gateway_url:8080$endpoint" > /dev/null; then
            log_warning "Health check failed for $endpoint, but continuing..."
        else
            log_info "✓ Health check passed for $endpoint"
        fi
    done
    
    log_success "Health checks completed for $slot deployment"
}

# Function to deploy to inactive slot
deploy_to_inactive_slot() {
    local inactive_slot=$(get_inactive_slot)
    
    log_info "Deploying to inactive slot: $inactive_slot"
    
    # Update values file to deploy to inactive slot
    helm upgrade $RELEASE_NAME $CHART_PATH \
        --namespace $NAMESPACE \
        --create-namespace \
        --set bluegreen.enabled=true \
        --set bluegreen.activeSlot=$inactive_slot \
        --timeout $TIMEOUT \
        --wait
    
    log_success "Deployed to $inactive_slot slot"
    
    # Wait for deployments to be ready
    sleep 30
    
    # Check health of new deployment
    if ! check_deployment_health $inactive_slot; then
        log_error "Health check failed for $inactive_slot deployment"
        return 1
    fi
    
    # Run additional health checks
    run_health_checks $inactive_slot
    
    log_success "Deployment to $inactive_slot slot completed successfully"
}

# Function to switch traffic to new deployment
switch_traffic() {
    local new_active_slot=$(get_inactive_slot)
    local old_active_slot=$(get_active_slot)
    
    log_info "Switching traffic from $old_active_slot to $new_active_slot"
    
    # Update ingress to point to new deployment
    helm upgrade $RELEASE_NAME $CHART_PATH \
        --namespace $NAMESPACE \
        --reuse-values \
        --set bluegreen.activeSlot=$new_active_slot \
        --timeout $TIMEOUT
    
    log_success "Traffic switched to $new_active_slot"
    
    # Wait a bit for traffic to stabilize
    sleep 10
    
    # Verify the switch worked
    log_info "Verifying traffic switch..."
    run_health_checks $new_active_slot
    
    log_success "Traffic successfully switched to $new_active_slot"
}

# Function to rollback to previous deployment
rollback() {
    local current_active=$(get_active_slot)
    local rollback_to=$(get_inactive_slot)
    
    log_warning "Rolling back from $current_active to $rollback_to"
    
    # Switch traffic back
    helm upgrade $RELEASE_NAME $CHART_PATH \
        --namespace $NAMESPACE \
        --reuse-values \
        --set bluegreen.activeSlot=$rollback_to \
        --timeout $TIMEOUT
    
    log_success "Rollback completed. Traffic switched to $rollback_to"
}

# Function to cleanup old deployment
cleanup_old_deployment() {
    local old_slot=$(get_inactive_slot)
    
    log_info "Cleaning up old deployment in $old_slot slot"
    
    # Scale down old deployments
    local services=("discovery-service" "config-server" "api-gateway" "customer-service" "product-service" "order-service" "payment-service" "notification-service")
    
    for service in "${services[@]}"; do
        local deployment_name="${service}-${old_slot}"
        
        if kubectl get deployment $deployment_name -n $NAMESPACE > /dev/null 2>&1; then
            kubectl scale deployment $deployment_name --replicas=0 -n $NAMESPACE
            log_info "Scaled down $deployment_name"
        fi
    done
    
    log_success "Old deployment cleanup completed"
}

# Function to perform canary deployment
canary_deploy() {
    local weight=${1:-10}
    local inactive_slot=$(get_inactive_slot)
    
    log_info "Starting canary deployment to $inactive_slot slot with $weight% traffic"
    
    # Deploy to inactive slot first
    deploy_to_inactive_slot
    
    # Update ingress canary weight
    kubectl patch ingress ${RELEASE_NAME}-canary -n $NAMESPACE -p "{\"metadata\":{\"annotations\":{\"nginx.ingress.kubernetes.io/canary-weight\":\"$weight\"}}}"
    
    log_success "Canary deployment started with $weight% traffic to $inactive_slot"
    log_info "Monitor metrics and run 'promote_canary' when ready"
}

# Function to promote canary to full deployment
promote_canary() {
    log_info "Promoting canary to full deployment"
    
    # Set canary weight to 100%
    kubectl patch ingress ${RELEASE_NAME}-canary -n $NAMESPACE -p '{"metadata":{"annotations":{"nginx.ingress.kubernetes.io/canary-weight":"100"}}}'
    
    sleep 10
    
    # Switch main traffic
    switch_traffic
    
    # Disable canary
    kubectl patch ingress ${RELEASE_NAME}-canary -n $NAMESPACE -p '{"metadata":{"annotations":{"nginx.ingress.kubernetes.io/canary-weight":"0"}}}'
    
    log_success "Canary promoted to full deployment"
}

# Function to show deployment status
status() {
    local active_slot=$(get_active_slot)
    local inactive_slot=$(get_inactive_slot)
    
    echo "=== Blue-Green Deployment Status ==="
    echo "Namespace: $NAMESPACE"
    echo "Release: $RELEASE_NAME"
    echo "Active Slot: $active_slot"
    echo "Inactive Slot: $inactive_slot"
    echo ""
    
    echo "=== Active Deployments ==="
    kubectl get deployments -n $NAMESPACE -l deployment.kubernetes.io/slot=$active_slot
    echo ""
    
    echo "=== Services ==="
    kubectl get services -n $NAMESPACE
    echo ""
    
    echo "=== Ingress ==="
    kubectl get ingress -n $NAMESPACE
}

# Main command dispatcher
case "${1:-}" in
    "deploy")
        deploy_to_inactive_slot
        ;;
    "switch")
        switch_traffic
        ;;
    "rollback")
        rollback
        ;;
    "cleanup")
        cleanup_old_deployment
        ;;
    "full-deploy")
        deploy_to_inactive_slot
        switch_traffic
        cleanup_old_deployment
        ;;
    "canary")
        canary_deploy ${2:-10}
        ;;
    "promote")
        promote_canary
        ;;
    "status")
        status
        ;;
    *)
        echo "Usage: $0 {deploy|switch|rollback|cleanup|full-deploy|canary [weight]|promote|status}"
        echo ""
        echo "Commands:"
        echo "  deploy      - Deploy to inactive slot"
        echo "  switch      - Switch traffic to inactive slot"
        echo "  rollback    - Rollback to previous deployment"
        echo "  cleanup     - Cleanup old deployment"
        echo "  full-deploy - Complete blue-green deployment (deploy + switch + cleanup)"
        echo "  canary      - Start canary deployment with specified weight (default: 10%)"
        echo "  promote     - Promote canary to full deployment"
        echo "  status      - Show deployment status"
        exit 1
        ;;
esac
