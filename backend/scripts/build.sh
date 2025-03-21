#!/bin/bash

# Script to build the backend application

echo "Building backend application..."

# Navigate to backend directory
cd "$(dirname "$0")/.." || exit

# Clean and install dependencies
echo "Maven clean and install..."
./mvnw clean install -DskipTests

# Run tests if needed
if [[ "$1" == "--tests" ]]; then
  echo "Running tests..."
  ./mvnw test
fi

echo "Build completed successfully!" 