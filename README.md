# Infologic POS System

A modern Point of Sale (POS) system built with React Native (Expo) and Spring Boot.

## Project Structure

```
infologic-pos/
├── frontend/           # React Native (Expo) frontend
│   └── MobilePOS/     # Main frontend application
├── backend/           # Spring Boot backend
└── k8s/              # Kubernetes deployment configurations
```

## Prerequisites

- Node.js (v20.11.0 or later)
- Java Development Kit (JDK 17)
- Docker Desktop
- Minikube (for local Kubernetes development)
- Expo CLI
- PostgreSQL (for local development)

## Setup Instructions

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   ./scripts/build.sh
   ```

3. Start local Kafka (requires Docker):
   ```bash
   ./scripts/run-kafka.sh
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend/MobilePOS
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

## Development Guidelines

- Follow the coding standards outlined in the documentation
- Write unit tests for new features
- Use conventional commits for version control
- Follow the branching strategy defined in the project rules

## Architecture Overview

### Event-Driven Backend

The backend follows an event-driven architecture using Apache Kafka as the messaging system. Events are produced when state changes occur and consumed by relevant services. This ensures:

- Real-time updates across the system
- Decoupling of components
- Reliable event processing

Key Kafka topics include:
- `sales-events`: For sales-related operations
- `inventory-events`: For inventory management
- `payment-events`: For payment processing
- `sync-events`: For offline data synchronization

### Multi-Tenant Design

The application supports multiple tenants (merchants) through:
- Schema-based isolation in PostgreSQL
- Tenant context management in requests
- Tenant-specific event handling

## Documentation

Detailed documentation can be found in the `documentation/` directory:
- Project Requirements
- Technical Stack
- Implementation Plan
- Frontend Guidelines
- Backend Structure
- App Flow
- Project Rules

## License

[License information to be added] 