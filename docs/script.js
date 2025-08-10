// Learning Platform Interactive JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Initialize the application
    initializeApp();
});

function initializeApp() {
    // Setup navigation
    setupSmoothScrolling();
    
    // Setup interactive architecture diagram
    setupArchitectureDiagram();
    
    // Setup service modals
    setupServiceModals();
    
    // Setup copy code functionality
    setupCodeCopying();
    
    // Setup progress tracking
    setupProgressTracking();
    
    // Setup dynamic content loading
    setupDynamicContent();
    
    console.log('🚀 Microservices Learning Platform Initialized');
}

// Smooth scrolling navigation
function setupSmoothScrolling() {
    const navLinks = document.querySelectorAll('a[href^="#"]');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                const headerOffset = 80;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
                
                // Update active navigation
                updateActiveNavigation(targetId);
            }
        });
    });
}

// Update active navigation item
function updateActiveNavigation(activeId) {
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === `#${activeId}`) {
            link.classList.add('active');
        }
    });
}

// Interactive Architecture Diagram
function setupArchitectureDiagram() {
    const serviceBoxes = document.querySelectorAll('.service-box, .infra-box');
    
    serviceBoxes.forEach(box => {
        box.addEventListener('click', function() {
            const serviceName = this.getAttribute('data-service');
            showServiceDetails(serviceName);
        });
        
        // Add hover effects
        box.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.05)';
            showServiceTooltip(this);
        });
        
        box.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            hideServiceTooltip();
        });
    });
}

// Service details data
const serviceDetails = {
    config: {
        title: 'Config Server',
        icon: 'fas fa-cog',
        port: '8888',
        technology: 'Spring Cloud Config',
        description: 'Centralized configuration management for all microservices',
        features: [
            'Environment-specific configurations',
            'Dynamic configuration refresh',
            'Git integration for version control',
            'Encrypted sensitive properties',
            'Configuration validation'
        ],
        endpoints: [
            { method: 'GET', path: '/{application}/{profile}', description: 'Get configuration' },
            { method: 'POST', path: '/refresh', description: 'Refresh configuration' }
        ]
    },
    discovery: {
        title: 'Discovery Service',
        icon: 'fas fa-search',
        port: '8761',
        technology: 'Netflix Eureka',
        description: 'Service registry and discovery for dynamic service location',
        features: [
            'Automatic service registration',
            'Health check monitoring',
            'Load balancing support',
            'Failover capabilities',
            'Service instance management'
        ],
        endpoints: [
            { method: 'GET', path: '/eureka/apps', description: 'List all services' },
            { method: 'GET', path: '/eureka/apps/{service}', description: 'Get service instances' }
        ]
    },
    gateway: {
        title: 'API Gateway',
        icon: 'fas fa-door-open',
        port: '8222',
        technology: 'Spring Cloud Gateway',
        description: 'Single entry point for all client requests with routing and filtering',
        features: [
            'Dynamic request routing',
            'Load balancing',
            'Authentication & authorization',
            'Rate limiting',
            'Request/response transformation',
            'Distributed tracing integration'
        ],
        routes: [
            { path: '/api/customers/**', target: 'Customer Service' },
            { path: '/api/products/**', target: 'Product Service' },
            { path: '/api/orders/**', target: 'Order Service' },
            { path: '/api/payments/**', target: 'Payment Service' }
        ]
    },
    customer: {
        title: 'Customer Service',
        icon: 'fas fa-users',
        database: 'MongoDB',
        technology: 'Spring Data MongoDB',
        description: 'Manages customer profiles and related operations',
        dataModel: {
            fields: ['firstname', 'lastname', 'email', 'address'],
            collections: ['customers']
        },
        apis: [
            { method: 'POST', path: '/api/customers', description: 'Create customer' },
            { method: 'GET', path: '/api/customers/{id}', description: 'Get customer details' },
            { method: 'PUT', path: '/api/customers/{id}', description: 'Update customer' },
            { method: 'GET', path: '/api/customers/exists/{id}', description: 'Check existence' }
        ]
    },
    product: {
        title: 'Product Service',
        icon: 'fas fa-box',
        database: 'PostgreSQL',
        technology: 'Spring Data JPA',
        description: 'Manages product catalog and inventory',
        features: [
            'Product catalog management',
            'Inventory tracking',
            'Price management',
            'Product categorization'
        ]
    },
    order: {
        title: 'Order Service',
        icon: 'fas fa-shopping-cart',
        database: 'PostgreSQL',
        technology: 'Spring Data JPA + OpenFeign + Kafka',
        description: 'Orchestrates order processing workflow',
        workflow: [
            'Validate customer via Customer Service',
            'Check product availability via Product Service',
            'Create order with PENDING status',
            'Process payment via Payment Service',
            'Publish order event to Kafka',
            'Update order status based on payment result'
        ]
    },
    payment: {
        title: 'Payment Service',
        icon: 'fas fa-credit-card',
        database: 'PostgreSQL',
        technology: 'Spring Data JPA',
        description: 'Handles payment processing and transactions'
    },
    notification: {
        title: 'Notification Service',
        icon: 'fas fa-envelope',
        database: 'MongoDB',
        technology: 'Kafka Consumer + Java Mail + Thymeleaf',
        description: 'Event-driven email notification system',
        events: [
            'Order confirmation',
            'Payment success',
            'Payment failure',
            'Order status updates'
        ]
    },
    postgres: {
        title: 'PostgreSQL',
        icon: 'fas fa-database',
        port: '5432',
        description: 'Relational database for Order, Payment, and Product services',
        features: ['ACID compliance', 'Complex queries', 'Transactions', 'Relationships']
    },
    mongodb: {
        title: 'MongoDB',
        icon: 'fas fa-leaf',
        port: '27017',
        description: 'Document database for Customer and Notification services',
        features: ['Schema flexibility', 'Horizontal scaling', 'JSON documents', 'Rich queries']
    },
    kafka: {
        title: 'Apache Kafka',
        icon: 'fas fa-stream',
        port: '9092',
        description: 'Distributed streaming platform for event-driven communication',
        topics: ['order-topic', 'payment-topic', 'notification-topic']
    },
    zipkin: {
        title: 'Zipkin',
        icon: 'fas fa-route',
        port: '9411',
        description: 'Distributed tracing system for monitoring microservices',
        features: ['Request tracing', 'Performance monitoring', 'Service dependencies', 'Error tracking']
    }
};

// Show service details modal
function showServiceDetails(serviceName) {
    const service = serviceDetails[serviceName];
    if (!service) return;
    
    const modal = document.getElementById('serviceModal');
    const title = document.getElementById('serviceModalTitle');
    const body = document.getElementById('serviceModalBody');
    
    title.innerHTML = `<i class="${service.icon}"></i> ${service.title}`;
    body.innerHTML = generateServiceModalContent(service);
    
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
}

// Generate service modal content
function generateServiceModalContent(service) {
    let content = `
        <div class="service-modal-content">
            <div class="row mb-3">
                <div class="col-md-6">
                    <h6><i class="fas fa-info-circle"></i> Overview</h6>
                    <p>${service.description}</p>
                </div>
                <div class="col-md-6">
                    <div class="service-specs">
    `;
    
    if (service.port) content += `<p><strong>Port:</strong> ${service.port}</p>`;
    if (service.database) content += `<p><strong>Database:</strong> ${service.database}</p>`;
    if (service.technology) content += `<p><strong>Technology:</strong> ${service.technology}</p>`;
    
    content += `
                    </div>
                </div>
            </div>
    `;
    
    if (service.features) {
        content += `
            <div class="mb-3">
                <h6><i class="fas fa-star"></i> Key Features</h6>
                <ul class="feature-list">
                    ${service.features.map(feature => `<li>${feature}</li>`).join('')}
                </ul>
            </div>
        `;
    }
    
    if (service.workflow) {
        content += `
            <div class="mb-3">
                <h6><i class="fas fa-cogs"></i> Workflow</h6>
                <ol class="workflow-list">
                    ${service.workflow.map(step => `<li>${step}</li>`).join('')}
                </ol>
            </div>
        `;
    }
    
    if (service.apis) {
        content += `
            <div class="mb-3">
                <h6><i class="fas fa-code"></i> API Endpoints</h6>
                <div class="api-list">
                    ${service.apis.map(api => `
                        <div class="api-item">
                            <span class="method ${api.method.toLowerCase()}">${api.method}</span>
                            <span class="path">${api.path}</span>
                            <span class="description">${api.description}</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    if (service.routes) {
        content += `
            <div class="mb-3">
                <h6><i class="fas fa-route"></i> Routes</h6>
                <div class="route-list">
                    ${service.routes.map(route => `
                        <div class="route-item">
                            <code>${route.path}</code> → ${route.target}
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    if (service.topics) {
        content += `
            <div class="mb-3">
                <h6><i class="fas fa-stream"></i> Kafka Topics</h6>
                <div class="topics">
                    ${service.topics.map(topic => `<span class="badge bg-info">${topic}</span>`).join(' ')}
                </div>
            </div>
        `;
    }
    
    content += `</div>`;
    return content;
}

// Tooltip functionality
let tooltip = null;

function showServiceTooltip(element) {
    const serviceName = element.getAttribute('data-service');
    const service = serviceDetails[serviceName];
    if (!service) return;
    
    // Remove existing tooltip
    hideServiceTooltip();
    
    // Create tooltip
    tooltip = document.createElement('div');
    tooltip.className = 'service-tooltip';
    tooltip.innerHTML = `
        <div class="tooltip-title">${service.title}</div>
        <div class="tooltip-description">${service.description}</div>
        ${service.port ? `<div class="tooltip-port">Port: ${service.port}</div>` : ''}
    `;
    
    document.body.appendChild(tooltip);
    
    // Position tooltip
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + rect.width / 2 - tooltip.offsetWidth / 2 + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 10 + 'px';
    
    // Show tooltip
    setTimeout(() => tooltip.classList.add('show'), 10);
}

function hideServiceTooltip() {
    if (tooltip) {
        tooltip.remove();
        tooltip = null;
    }
}

// Setup service modals
function setupServiceModals() {
    // Modal styles
    const modalStyles = `
        <style>
            .service-modal-content {
                font-size: 0.95rem;
            }
            
            .service-specs {
                background: #f8f9fa;
                padding: 1rem;
                border-radius: 8px;
            }
            
            .feature-list, .workflow-list {
                padding-left: 1.5rem;
            }
            
            .feature-list li, .workflow-list li {
                margin-bottom: 0.5rem;
            }
            
            .api-list {
                background: #f8f9fa;
                padding: 1rem;
                border-radius: 8px;
            }
            
            .api-item {
                display: flex;
                align-items: center;
                margin-bottom: 0.5rem;
                padding: 0.5rem;
                background: white;
                border-radius: 4px;
            }
            
            .api-item .method {
                margin-right: 1rem;
                min-width: 60px;
            }
            
            .api-item .path {
                font-family: monospace;
                background: #e9ecef;
                padding: 0.25rem 0.5rem;
                border-radius: 3px;
                margin-right: 1rem;
                flex-shrink: 0;
            }
            
            .api-item .description {
                color: #6c757d;
                font-size: 0.9rem;
            }
            
            .route-list {
                background: #f8f9fa;
                padding: 1rem;
                border-radius: 8px;
            }
            
            .route-item {
                margin-bottom: 0.5rem;
                padding: 0.5rem;
                background: white;
                border-radius: 4px;
            }
            
            .route-item code {
                background: #e9ecef;
                padding: 0.25rem 0.5rem;
                border-radius: 3px;
                margin-right: 1rem;
            }
            
            .service-tooltip {
                position: fixed;
                background: rgba(0, 0, 0, 0.9);
                color: white;
                padding: 0.75rem;
                border-radius: 8px;
                font-size: 0.9rem;
                max-width: 250px;
                z-index: 9999;
                opacity: 0;
                transform: translateY(10px);
                transition: all 0.3s ease;
                pointer-events: none;
            }
            
            .service-tooltip.show {
                opacity: 1;
                transform: translateY(0);
            }
            
            .tooltip-title {
                font-weight: 600;
                margin-bottom: 0.5rem;
                color: #ffd700;
            }
            
            .tooltip-description {
                font-size: 0.85rem;
                margin-bottom: 0.5rem;
                line-height: 1.4;
            }
            
            .tooltip-port {
                font-size: 0.8rem;
                color: #ccc;
            }
        </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', modalStyles);
}

// Code copying functionality
function setupCodeCopying() {
    const codeBlocks = document.querySelectorAll('pre code, .code-block code');
    
    codeBlocks.forEach(block => {
        const container = block.closest('pre, .code-block');
        if (container && !container.querySelector('.copy-btn')) {
            const copyBtn = document.createElement('button');
            copyBtn.className = 'copy-btn';
            copyBtn.innerHTML = '<i class="fas fa-copy"></i>';
            copyBtn.title = 'Copy to clipboard';
            
            copyBtn.addEventListener('click', () => {
                navigator.clipboard.writeText(block.textContent).then(() => {
                    copyBtn.innerHTML = '<i class="fas fa-check"></i>';
                    copyBtn.style.background = '#28a745';
                    
                    setTimeout(() => {
                        copyBtn.innerHTML = '<i class="fas fa-copy"></i>';
                        copyBtn.style.background = '#667eea';
                    }, 2000);
                });
            });
            
            container.style.position = 'relative';
            container.appendChild(copyBtn);
        }
    });
    
    // Copy button styles
    const copyStyles = `
        <style>
            .copy-btn {
                position: absolute;
                top: 0.5rem;
                right: 0.5rem;
                background: #667eea;
                color: white;
                border: none;
                padding: 0.5rem;
                border-radius: 4px;
                cursor: pointer;
                font-size: 0.8rem;
                transition: all 0.3s ease;
                z-index: 1;
            }
            
            .copy-btn:hover {
                background: #5a67d8;
                transform: scale(1.05);
            }
        </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', copyStyles);
}

// Progress tracking
function setupProgressTracking() {
    const sections = document.querySelectorAll('section[id]');
    let currentSection = '';
    
    window.addEventListener('scroll', () => {
        const scrollPosition = window.pageYOffset + 100;
        
        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionBottom = sectionTop + section.offsetHeight;
            
            if (scrollPosition >= sectionTop && scrollPosition < sectionBottom) {
                if (currentSection !== section.id) {
                    currentSection = section.id;
                    updateActiveNavigation(currentSection);
                    updateProgressBar(section.id);
                }
            }
        });
    });
    
    // Create progress bar
    const progressBar = document.createElement('div');
    progressBar.className = 'learning-progress';
    progressBar.innerHTML = `
        <div class="progress-bar">
            <div class="progress-fill"></div>
        </div>
        <div class="progress-text">Learning Progress</div>
    `;
    document.body.appendChild(progressBar);
    
    // Progress bar styles
    const progressStyles = `
        <style>
            .learning-progress {
                position: fixed;
                top: 80px;
                right: 20px;
                background: white;
                padding: 1rem;
                border-radius: 8px;
                box-shadow: 0 5px 15px rgba(0,0,0,0.1);
                z-index: 1000;
                min-width: 150px;
            }
            
            .progress-bar {
                width: 100%;
                height: 8px;
                background: #e9ecef;
                border-radius: 4px;
                overflow: hidden;
                margin-bottom: 0.5rem;
            }
            
            .progress-fill {
                height: 100%;
                background: linear-gradient(90deg, #667eea, #764ba2);
                transition: width 0.5s ease;
                width: 0%;
            }
            
            .progress-text {
                font-size: 0.8rem;
                text-align: center;
                color: #666;
            }
            
            @media (max-width: 768px) {
                .learning-progress {
                    display: none;
                }
            }
        </style>
    `;
    document.head.insertAdjacentHTML('beforeend', progressStyles);
}

function updateProgressBar(sectionId) {
    const sections = ['home', 'overview', 'architecture', 'repository', 'services', 'patterns', 'hands-on'];
    const currentIndex = sections.indexOf(sectionId);
    const progress = currentIndex >= 0 ? ((currentIndex + 1) / sections.length) * 100 : 0;
    
    const progressFill = document.querySelector('.progress-fill');
    if (progressFill) {
        progressFill.style.width = progress + '%';
    }
}

// Dynamic content loading
function setupDynamicContent() {
    // Service status checking
    const statusIndicators = document.querySelectorAll('.service-status');
    statusIndicators.forEach(indicator => {
        checkServiceStatus(indicator);
    });
    
    // Auto-refresh service statuses every 30 seconds
    setInterval(() => {
        statusIndicators.forEach(indicator => {
            checkServiceStatus(indicator);
        });
    }, 30000);
}

function checkServiceStatus(indicator) {
    const serviceName = indicator.getAttribute('data-service');
    const url = getServiceHealthUrl(serviceName);
    
    if (!url) return;
    
    fetch(url)
        .then(response => {
            if (response.ok) {
                indicator.className = 'service-status online';
                indicator.textContent = 'Online';
            } else {
                indicator.className = 'service-status offline';
                indicator.textContent = 'Offline';
            }
        })
        .catch(() => {
            indicator.className = 'service-status offline';
            indicator.textContent = 'Offline';
        });
}

function getServiceHealthUrl(serviceName) {
    const healthUrls = {
        'config': 'http://localhost:8888/actuator/health',
        'discovery': 'http://localhost:8761/actuator/health',
        'gateway': 'http://localhost:8222/actuator/health',
        'zipkin': 'http://localhost:9411/health'
    };
    
    return healthUrls[serviceName];
}

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Press 'h' to go home
    if (e.key === 'h' && !e.ctrlKey && !e.metaKey) {
        document.getElementById('home').scrollIntoView({ behavior: 'smooth' });
    }
    
    // Press 'a' to go to architecture
    if (e.key === 'a' && !e.ctrlKey && !e.metaKey) {
        document.getElementById('architecture').scrollIntoView({ behavior: 'smooth' });
    }
    
    // Press 's' to go to services
    if (e.key === 's' && !e.ctrlKey && !e.metaKey) {
        document.getElementById('services').scrollIntoView({ behavior: 'smooth' });
    }
    
    // Press 'p' to go to patterns
    if (e.key === 'p' && !e.ctrlKey && !e.metaKey) {
        document.getElementById('patterns').scrollIntoView({ behavior: 'smooth' });
    }
});

// Print functionality
function printLearningGuide() {
    window.print();
}

// Export learning progress
function exportProgress() {
    const progress = {
        timestamp: new Date().toISOString(),
        sectionsVisited: getSectionsVisited(),
        currentSection: getCurrentSection(),
        timeSpent: getTimeSpent()
    };
    
    const blob = new Blob([JSON.stringify(progress, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'microservices-learning-progress.json';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function getSectionsVisited() {
    return JSON.parse(localStorage.getItem('sectionsVisited') || '[]');
}

function getCurrentSection() {
    return document.querySelector('section:target')?.id || 'home';
}

function getTimeSpent() {
    return parseInt(localStorage.getItem('timeSpent') || '0');
}

// Initialize time tracking
let startTime = Date.now();
setInterval(() => {
    const timeSpent = parseInt(localStorage.getItem('timeSpent') || '0');
    localStorage.setItem('timeSpent', (timeSpent + 60).toString());
}, 60000);

// Add utility functions for external use
window.MicroservicesLearningPlatform = {
    showServiceDetails,
    exportProgress,
    printLearningGuide,
    getCurrentSection,
    getSectionsVisited
};

console.log('🎓 Welcome to the Microservices Learning Platform!');
console.log('Press "h" for home, "a" for architecture, "s" for services, "p" for patterns');
console.log('Click on any service box to see detailed information');

// Kubernetes Deployment Interactive Features
function setupDeploymentInteractions() {
    // Setup blue-green deployment visualization
    setupBlueGreenVisualization();
    
    // Setup Helm command examples
    setupHelmCommands();
    
    // Setup deployment step animations
    setupDeploymentAnimations();
    
    // Setup circuit breaker demonstration
    setupCircuitBreakerDemo();
    
    console.log('🚢 Deployment interactions initialized');
}

// Blue-Green Deployment Visualization
function setupBlueGreenVisualization() {
    const deploymentSteps = document.querySelectorAll('.deployment-step');
    
    deploymentSteps.forEach((step, index) => {
        step.addEventListener('click', function() {
            // Reset all steps
            deploymentSteps.forEach(s => s.classList.remove('active', 'completed'));
            
            // Mark current and previous steps as completed
            for (let i = 0; i <= index; i++) {
                deploymentSteps[i].classList.add('completed');
            }
            
            // Mark current step as active
            this.classList.add('active');
            
            // Show deployment status
            showDeploymentStatus(index);
        });
    });
}

function showDeploymentStatus(step) {
    const statusMessages = [
        '🔄 Preparing green environment... Building and deploying new version.',
        '✅ Running health checks... Validating green environment readiness.',
        '🔀 Switching traffic... Routing production traffic to green environment.',
        '🔙 Rollback ready... Blue environment available for instant rollback.'
    ];
    
    // Create or update status display
    let statusDiv = document.querySelector('.deployment-status');
    if (!statusDiv) {
        statusDiv = document.createElement('div');
        statusDiv.className = 'deployment-status alert alert-info mt-3';
        document.querySelector('.blue-green-section').appendChild(statusDiv);
    }
    
    statusDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <div class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></div>
            <strong>Step ${step + 1}:</strong> ${statusMessages[step]}
        </div>
    `;
    
    // Remove spinner after 2 seconds
    setTimeout(() => {
        statusDiv.querySelector('.spinner-border')?.remove();
    }, 2000);
}

// Helm Command Examples
function setupHelmCommands() {
    const helmSection = document.querySelector('.helm-section');
    if (!helmSection) return;
    
    // Create interactive Helm command builder
    const commandBuilder = document.createElement('div');
    commandBuilder.className = 'helm-command-builder mt-3 p-3 bg-light border rounded';
    commandBuilder.innerHTML = `
        <h6><i class="fas fa-tools"></i> Interactive Helm Command Builder</h6>
        <div class="row">
            <div class="col-md-6">
                <label for="namespace">Namespace:</label>
                <select id="namespace" class="form-select form-select-sm mb-2">
                    <option value="microservices">microservices</option>
                    <option value="production">production</option>
                    <option value="staging">staging</option>
                    <option value="development">development</option>
                </select>
            </div>
            <div class="col-md-6">
                <label for="environment">Environment:</label>
                <select id="environment" class="form-select form-select-sm mb-2">
                    <option value="production">production</option>
                    <option value="staging">staging</option>
                    <option value="development">development</option>
                </select>
            </div>
        </div>
        <div class="generated-command bg-dark text-light p-2 rounded mt-2">
            <code id="helmCommand">helm install ecommerce-platform ./k8s/helm-charts --namespace microservices --create-namespace --set global.environment=production</code>
        </div>
        <button class="btn btn-sm btn-primary mt-2" onclick="copyHelmCommand()">
            <i class="fas fa-copy"></i> Copy Command
        </button>
    `;
    
    helmSection.appendChild(commandBuilder);
    
    // Update command on change
    const inputs = commandBuilder.querySelectorAll('select');
    inputs.forEach(input => {
        input.addEventListener('change', updateHelmCommand);
    });
}

function updateHelmCommand() {
    const namespace = document.getElementById('namespace').value;
    const environment = document.getElementById('environment').value;
    const command = `helm install ecommerce-platform ./k8s/helm-charts --namespace ${namespace} --create-namespace --set global.environment=${environment}`;
    document.getElementById('helmCommand').textContent = command;
}

function copyHelmCommand() {
    const command = document.getElementById('helmCommand').textContent;
    copyToClipboard(command);
    
    // Show feedback
    const btn = event.target.closest('button');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-check"></i> Copied!';
    btn.classList.add('btn-success');
    btn.classList.remove('btn-primary');
    
    setTimeout(() => {
        btn.innerHTML = originalText;
        btn.classList.remove('btn-success');
        btn.classList.add('btn-primary');
    }, 2000);
}

// Deployment Step Animations
function setupDeploymentAnimations() {
    // Intersection Observer for step animations
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.animationDelay = entry.target.dataset.delay + 'ms';
                entry.target.classList.add('animate-in');
            }
        });
    });
    
    document.querySelectorAll('.deployment-step').forEach((step, index) => {
        step.dataset.delay = index * 200;
        observer.observe(step);
    });
}

// Circuit Breaker Demonstration
function setupCircuitBreakerDemo() {
    const circuitBreakerCard = document.querySelector('.pattern-card:has(.fa-plug-circle-xmark)');
    if (!circuitBreakerCard) return;
    
    // Add interactive demo button
    const demoSection = document.createElement('div');
    demoSection.className = 'circuit-breaker-demo mt-3';
    demoSection.innerHTML = `
        <div class="demo-controls">
            <button class="btn btn-sm btn-outline-primary" onclick="simulateCircuitBreaker('saga-orchestration')">
                Test Saga Orchestration CB
            </button>
            <button class="btn btn-sm btn-outline-success" onclick="simulateCircuitBreaker('saga-compensation')">
                Test Saga Compensation CB
            </button>
            <button class="btn btn-sm btn-outline-warning" onclick="simulateCircuitBreaker('saga-recovery')">
                Test Saga Recovery CB
            </button>
        </div>
        <div class="demo-status mt-2"></div>
    `;
    
    circuitBreakerCard.appendChild(demoSection);
}

function simulateCircuitBreaker(type) {
    const statusDiv = document.querySelector('.demo-status');
    const states = ['CLOSED', 'OPEN', 'HALF_OPEN'];
    let currentState = 0;
    
    const typeNames = {
        'saga-orchestration': 'Saga Orchestration',
        'saga-compensation': 'Saga Compensation', 
        'saga-recovery': 'Saga Recovery'
    };
    
    const interval = setInterval(() => {
        const state = states[currentState];
        const color = state === 'CLOSED' ? 'success' : state === 'OPEN' ? 'danger' : 'warning';
        
        statusDiv.innerHTML = `
            <div class="alert alert-${color} py-2">
                <strong>${typeNames[type]} Circuit Breaker:</strong> 
                <span class="badge bg-${color}">${state}</span>
                ${getCircuitBreakerMessage(state)}
            </div>
        `;
        
        currentState = (currentState + 1) % states.length;
        
        if (currentState === 0) {
            clearInterval(interval);
            setTimeout(() => {
                statusDiv.innerHTML = '';
            }, 3000);
        }
    }, 2000);
}

function getCircuitBreakerMessage(state) {
    switch (state) {
        case 'CLOSED': return 'All requests passing through normally';
        case 'OPEN': return 'Failing fast, requests blocked';
        case 'HALF_OPEN': return 'Testing with limited requests';
        default: return '';
    }
}

// Saga Pattern Visualization
function setupSagaVisualization() {
    const sagaCard = document.querySelector('.pattern-card:has(.fa-project-diagram)');
    if (!sagaCard) return;
    
    const visualizer = document.createElement('div');
    visualizer.className = 'saga-visualizer mt-3';
    visualizer.innerHTML = `
        <div class="saga-flow-demo">
            <button class="btn btn-sm btn-primary" onclick="runSagaDemo()">
                <i class="fas fa-play"></i> Run Saga Flow Demo
            </button>
            <div class="saga-steps mt-2"></div>
        </div>
    `;
    
    sagaCard.appendChild(visualizer);
}

function runSagaDemo() {
    const stepsContainer = document.querySelector('.saga-steps');
    const steps = [
        { name: 'Customer Validation', status: 'running', duration: 1000 },
        { name: 'Inventory Reservation', status: 'pending', duration: 1500 },
        { name: 'Payment Processing', status: 'pending', duration: 2000 },
        { name: 'Order Completion', status: 'pending', duration: 1000 }
    ];
    
    let currentStep = 0;
    
    function updateDisplay() {
        stepsContainer.innerHTML = steps.map((step, index) => {
            let statusClass = 'secondary';
            let icon = 'fas fa-clock';
            
            if (index < currentStep) {
                statusClass = 'success';
                icon = 'fas fa-check';
            } else if (index === currentStep) {
                statusClass = 'primary';
                icon = 'fas fa-spinner fa-spin';
            }
            
            return `
                <div class="saga-step-item d-flex align-items-center mb-2">
                    <span class="badge bg-${statusClass} me-2">
                        <i class="${icon}"></i>
                    </span>
                    <span>${step.name}</span>
                </div>
            `;
        }).join('');
    }
    
    updateDisplay();
    
    const stepInterval = setInterval(() => {
        if (currentStep < steps.length) {
            currentStep++;
            updateDisplay();
            
            if (currentStep >= steps.length) {
                clearInterval(stepInterval);
                setTimeout(() => {
                    stepsContainer.innerHTML = '<div class="alert alert-success py-2">✅ Saga completed successfully!</div>';
                    setTimeout(() => stepsContainer.innerHTML = '', 3000);
                }, 1000);
            }
        }
    }, 2000);
}

// Update the main initialization function
const originalInitializeApp = initializeApp;
initializeApp = function() {
    originalInitializeApp();
    
    // Initialize deployment features
    setupDeploymentInteractions();
    setupSagaVisualization();
    
    console.log('🎯 Enhanced with Kubernetes deployment features');
};

// Add CSS animations for deployment steps
const deploymentCSS = `
.deployment-step {
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s ease;
}

.deployment-step.animate-in {
    opacity: 1;
    transform: translateY(0);
}

.deployment-step.active {
    border-top-width: 6px;
    box-shadow: 0 15px 35px rgba(0,0,0,0.2);
}

.deployment-step.completed {
    background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.deployment-step.completed .step-number {
    background: #28a745 !important;
}
`;

// Inject CSS
const style = document.createElement('style');
style.textContent = deploymentCSS;
document.head.appendChild(style);

console.log('🚀 Kubernetes & Circuit Breaker features loaded!');
console.log('Click deployment steps to see the blue-green process in action');
console.log('Try the circuit breaker demos to see resilience patterns');
