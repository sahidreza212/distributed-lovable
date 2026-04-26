# Distributed Lovable — Microservices Platform

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=flat-square&logo=springboot) ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-Gateway-brightgreen?style=flat-square) ![Kubernetes](https://img.shields.io/badge/Kubernetes-K8s-blue?style=flat-square&logo=kubernetes) ![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=flat-square&logo=docker) ![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square)

A distributed, microservices-based backend platform inspired by Lovable — enabling AI-powered workspace management, account handling, and intelligent services — built with Spring Boot and designed with a clean, scalable architecture using Spring Cloud and Kubernetes.

---

## Table of Contents

1. [About the Project](#about-the-project)
2. [Key Features](#key-features)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Architecture Overview](#architecture-overview)
6. [Services](#services)
7. [Getting Started](#getting-started)
8. [Kubernetes Deployment](#kubernetes-deployment)
9. [CI/CD Pipeline](#cicd-pipeline)
10. [Contributing](#contributing)
11. [Author](#author)

---

## About the Project

**Distributed Lovable** is a production-grade, cloud-native backend system modeled after the Lovable platform. It decomposes core platform functionality into independently deployable microservices — each responsible for a single domain such as accounts, workspaces, or AI intelligence. Services communicate through an API Gateway, are discovered via a Eureka Discovery Service, and are configured centrally through a Config Service.

The system is fully containerized with Docker and deployable on Kubernetes, making it suitable for cloud environments at scale.

---

## Key Features

- **Microservices Architecture** — Domain-driven service separation for high cohesion and loose coupling
- **API Gateway** — Centralized entry point for routing, load balancing, and cross-cutting concerns
- **Service Discovery** — Eureka-based dynamic service registration and discovery
- **Centralized Configuration** — Spring Cloud Config Server for environment-specific configuration management
- **JWT Authentication** — Stateless, token-based security across all services
- **AI Intelligence Service** — Dedicated service for AI-powered features and processing
- **Kubernetes Ready** — Full K8s manifests included for production deployment
- **CI/CD Pipeline** — GitHub Actions workflows for automated build and deployment
- **Common Library** — Shared DTOs, utilities, and exceptions to avoid code duplication

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Config Management | Spring Cloud Config Server |
| Security | JWT (JSON Web Tokens) |
| Build Tool | Maven 3.8+ |
| Containerization | Docker |
| Orchestration | Kubernetes (K8s) |
| CI/CD | GitHub Actions |

---

## Project Structure

```
distributed-lovable/
├── .github/
│   └── workflows/               # GitHub Actions CI/CD pipelines
├── api-gateway/
│   └── api-gateway/             # Spring Cloud Gateway — routing & security
├── account-service/
│   └── account-service/         # User account management & JWT auth
├── workspace-service/
│   └── workspace-service/       # Workspace lifecycle management
├── intelligence-service/
│   └── intelligence-service/    # AI-powered intelligence & processing
├── discovery-service/
│   └── discovery-service/       # Eureka Service Registry
├── config-service/
│   └── config-service/          # Centralized Spring Cloud Config Server
├── common-lib (1)/
│   └── common-lib/              # Shared models, DTOs, and utilities
├── k8s/                         # Kubernetes manifests for all services
└── .gitignore
```

## Architecture Overview

```
                        ┌─────────────────┐
                        │   API Gateway   │  ← Single entry point
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼───────┐ ┌───────▼────────┐ ┌──────▼───────────┐
    │ Account Service │ │Workspace Service│ │Intelligence Svc  │
    └─────────────────┘ └────────────────┘ └──────────────────┘
              │                  │                  │
              └──────────────────┼──────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Discovery Service     │  ← Eureka Registry
                    └─────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    Config Service       │  ← Centralized Config
                    └─────────────────────────┘
```

## Services

### API Gateway (`api-gateway`)
The central entry point for all client requests. Handles routing to downstream services, JWT token validation, and cross-cutting concerns like CORS and rate limiting. Built with Spring Cloud Gateway.

### Account Service (`account-service`)
Manages user registration, authentication, and profile operations. Issues and validates JWT tokens for stateless authentication across the platform.

### Workspace Service (`workspace-service`)
Handles the full lifecycle of user workspaces — creation, retrieval, updates, and deletion. Integrates with other services for a seamless workspace experience.

### Intelligence Service (`intelligence-service`)
The AI brain of the platform. Powers intelligent features, content generation, and AI-assisted operations inspired by Lovable's core functionality.

### Discovery Service (`discovery-service`)
A Spring Cloud Netflix Eureka server that acts as the service registry. All microservices register themselves here and use it to locate other services dynamically.

### Config Service (`config-service`)
A Spring Cloud Config Server that serves externalized configuration to all microservices. Supports environment-specific profiles (dev, staging, prod).

### Common Library (`common-lib`)
A shared Maven library containing common DTOs, exception classes, response wrappers, and utility methods used across all services.

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- Kubernetes cluster (for K8s deployment) or Minikube for local

### Clone the Repository

```bash
git clone https://github.com/sahidreza212/distributed-lovable.git
cd distributed-lovable
```

### Build All Services

Build the common library first, then each service:

```bash
# Build common library
cd "common-lib (1)/common-lib"
mvn clean install
cd ../..

# Build each service
for service in config-service discovery-service api-gateway account-service workspace-service intelligence-service; do
  cd "$service/$service"
  mvn clean package -DskipTests
  cd ../..
done
```

### Running Locally (Recommended Order)

```bash
# 1. Config Service (first — others depend on it)
cd config-service/config-service && mvn spring-boot:run

# 2. Discovery Service (Eureka)
cd discovery-service/discovery-service && mvn spring-boot:run

# 3. Domain Services (any order)
cd account-service/account-service && mvn spring-boot:run
cd workspace-service/workspace-service && mvn spring-boot:run
cd intelligence-service/intelligence-service && mvn spring-boot:run

# 4. API Gateway (last)
cd api-gateway/api-gateway && mvn spring-boot:run
```

### Default Ports

| Service | Default Port |
|---|---|
| Config Service | 8888 |
| Discovery Service (Eureka) | 8761 |
| API Gateway | 8080 |
| Account Service | 8081 |
| Workspace Service | 8082 |
| Intelligence Service | 8083 |

Access the Eureka dashboard at: `http://localhost:8761`

---

## Kubernetes Deployment

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods
kubectl get services

# View logs for a specific service
kubectl logs -l app=api-gateway -f
```

---

## CI/CD Pipeline

The project uses **GitHub Actions** for continuous integration and deployment. Workflows are defined in `.github/workflows/` and handle automated builds, tests, Docker image creation, and Kubernetes deployments.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## Author

**Sahid Reza**

- GitHub: [@sahidreza212](https://github.com/sahidreza212)
- Repository: [distributed-lovable](https://github.com/sahidreza212/distributed-lovable)

---

> Built with ❤️ using Spring Boot, Spring Cloud, and Kubernetes.

                
