#!/bin/bash

# Docker Image Build Script for E-commerce Microservices
# Author: Pramitha Jayasooriya
# Version: 1.0.0

set -e

# Configuration
DOCKER_REGISTRY=${DOCKER_REGISTRY:-""}
IMAGE_TAG=${IMAGE_TAG:-"latest"}
SERVICES_DIR="../services"

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

# Function to build Docker image
build_image() {
    local service_name=$1
    local service_dir=$2
    local image_name="ecommerce/${service_name}"
    
    if [ ! -z "$DOCKER_REGISTRY" ]; then
        image_name="${DOCKER_REGISTRY}/${image_name}"
    fi
    
    log_info "Building image for ${service_name}..."
    
    # Check if Dockerfile exists
    if [ ! -f "${service_dir}/Dockerfile" ]; then
        log_warning "Dockerfile not found for ${service_name}, creating one..."
        create_dockerfile "$service_dir" "$service_name"
    fi
    
    # Build the Docker image
    docker build -t "${image_name}:${IMAGE_TAG}" "$service_dir"
    
    if [ $? -eq 0 ]; then
        log_success "Successfully built ${image_name}:${IMAGE_TAG}"
        
        # Tag as latest
        docker tag "${image_name}:${IMAGE_TAG}" "${image_name}:latest"
        
        return 0
    else
        log_error "Failed to build ${image_name}:${IMAGE_TAG}"
        return 1
    fi
}

# Function to create Dockerfile if not exists
create_dockerfile() {
    local service_dir=$1
    local service_name=$2
    
    cat > "${service_dir}/Dockerfile" << EOF
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy the application jar
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \\
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EOF
    
    log_info "Created Dockerfile for ${service_name}"
}

# Function to push image to registry
push_image() {
    local service_name=$1
    local image_name="ecommerce/${service_name}"
    
    if [ ! -z "$DOCKER_REGISTRY" ]; then
        image_name="${DOCKER_REGISTRY}/${image_name}"
        
        log_info "Pushing ${image_name}:${IMAGE_TAG} to registry..."
        docker push "${image_name}:${IMAGE_TAG}"
        docker push "${image_name}:latest"
        
        if [ $? -eq 0 ]; then
            log_success "Successfully pushed ${image_name}"
        else
            log_error "Failed to push ${image_name}"
            return 1
        fi
    else
        log_warning "No registry specified, skipping push for ${service_name}"
    fi
}

# Function to build all services
build_all_services() {
    local services=(
        "discovery-service:discovery"
        "config-server:config-server"
        "api-gateway:gateway"
        "customer-service:customer"
        "product-service:product"
        "order-service:order"
        "payment-service:payment"
        "notification-service:notification"
    )
    
    local failed_builds=()
    
    log_info "Building all microservices Docker images..."
    log_info "Registry: ${DOCKER_REGISTRY:-'local'}"
    log_info "Tag: ${IMAGE_TAG}"
    
    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name service_dir <<< "$service_info"
        local full_service_dir="${SERVICES_DIR}/${service_dir}"
        
        if [ -d "$full_service_dir" ]; then
            # Build Maven project first
            log_info "Building Maven project for ${service_name}..."
            (cd "$full_service_dir" && ./mvnw clean package -DskipTests)
            
            if [ $? -eq 0 ]; then
                # Build Docker image
                if build_image "$service_name" "$full_service_dir"; then
                    # Push to registry if specified
                    push_image "$service_name"
                else
                    failed_builds+=("$service_name")
                fi
            else
                log_error "Maven build failed for ${service_name}"
                failed_builds+=("$service_name")
            fi
        else
            log_error "Service directory not found: ${full_service_dir}"
            failed_builds+=("$service_name")
        fi
    done
    
    # Summary
    echo ""
    echo "=== Build Summary ==="
    if [ ${#failed_builds[@]} -eq 0 ]; then
        log_success "All services built successfully!"
    else
        log_error "Failed to build the following services:"
        for failed_service in "${failed_builds[@]}"; do
            echo "  - $failed_service"
        done
        return 1
    fi
}

# Function to build single service
build_single_service() {
    local service_name=$1
    
    case $service_name in
        "discovery-service"|"discovery")
            build_service_by_name "discovery-service" "discovery"
            ;;
        "config-server"|"config")
            build_service_by_name "config-server" "config-server"
            ;;
        "api-gateway"|"gateway")
            build_service_by_name "api-gateway" "gateway"
            ;;
        "customer-service"|"customer")
            build_service_by_name "customer-service" "customer"
            ;;
        "product-service"|"product")
            build_service_by_name "product-service" "product"
            ;;
        "order-service"|"order")
            build_service_by_name "order-service" "order"
            ;;
        "payment-service"|"payment")
            build_service_by_name "payment-service" "payment"
            ;;
        "notification-service"|"notification")
            build_service_by_name "notification-service" "notification"
            ;;
        *)
            log_error "Unknown service: $service_name"
            echo "Available services: discovery, config, gateway, customer, product, order, payment, notification"
            return 1
            ;;
    esac
}

# Helper function to build service by name
build_service_by_name() {
    local service_name=$1
    local service_dir=$2
    local full_service_dir="${SERVICES_DIR}/${service_dir}"
    
    if [ -d "$full_service_dir" ]; then
        log_info "Building Maven project for ${service_name}..."
        (cd "$full_service_dir" && ./mvnw clean package -DskipTests)
        
        if [ $? -eq 0 ]; then
            if build_image "$service_name" "$full_service_dir"; then
                push_image "$service_name"
            fi
        else
            log_error "Maven build failed for ${service_name}"
            return 1
        fi
    else
        log_error "Service directory not found: ${full_service_dir}"
        return 1
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS] [SERVICE]"
    echo ""
    echo "Build Docker images for E-commerce microservices"
    echo ""
    echo "Options:"
    echo "  -r, --registry REGISTRY    Docker registry to push images to"
    echo "  -t, --tag TAG             Image tag (default: latest)"
    echo "  -h, --help                Show this help message"
    echo ""
    echo "Services:"
    echo "  all                       Build all services (default)"
    echo "  discovery                 Discovery Service (Eureka)"
    echo "  config                    Config Server"
    echo "  gateway                   API Gateway"
    echo "  customer                  Customer Service"
    echo "  product                   Product Service"
    echo "  order                     Order Service"
    echo "  payment                   Payment Service"
    echo "  notification              Notification Service"
    echo ""
    echo "Examples:"
    echo "  $0                        Build all services with default settings"
    echo "  $0 order                  Build only Order Service"
    echo "  $0 -r docker.io/myuser -t v1.0.0 all"
    echo "  $0 --registry=harbor.company.com --tag=prod customer"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        -t|--tag)
            IMAGE_TAG="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        -*)
            log_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
        *)
            SERVICE_NAME="$1"
            shift
            ;;
    esac
done

# Main execution
log_info "E-commerce Microservices Docker Build Script"
log_info "============================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Build services
if [ -z "${SERVICE_NAME:-}" ] || [ "${SERVICE_NAME}" = "all" ]; then
    build_all_services
else
    build_single_service "$SERVICE_NAME"
fi

log_success "Build process completed!"

# Show built images
echo ""
echo "=== Built Images ==="
docker images | grep "ecommerce/"
