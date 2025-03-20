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

## Setup Instructions

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

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Development Guidelines

- Follow the coding standards outlined in the documentation
- Write unit tests for new features
- Use conventional commits for version control
- Follow the branching strategy defined in the project rules

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