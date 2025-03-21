#!/bin/bash

# A simple script to quickly fix Lombok issues in InfoLogic POS

echo "InfoLogic POS - Quick Lombok Fix"
echo "==============================="
echo

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Clean Maven repository Lombok cache
echo "Cleaning Maven repository Lombok cache..."
find "$HOME/.m2/repository/org/projectlombok" -name "*.lastUpdated" -delete
find "$HOME/.m2/repository/org/projectlombok" -name "resolver-status.properties" -delete
echo "Maven Lombok cache cleaned."

# Change to project directory
cd "$PROJECT_ROOT"

# Clean the project
echo "Cleaning project..."
mvn clean -q

# Force update dependencies
echo "Updating dependencies..."
mvn dependency:purge-local-repository -DreResolve=false -DactTransitively=false -DmanualIncludes=org.projectlombok:lombok

# Build with specific Maven options
echo "Building project with special options..."
mvn compile -Dmaven.compiler.forceJavacCompilerUse=true -Dmaven.compiler.fork=true -Dannotation.processing.enabled=true

# Check for success
if [ $? -eq 0 ]; then
    echo -e "\n✅ Build successful!"
    echo "The Lombok issue should be fixed. If you're still having issues:"
    echo "1. Try running the more comprehensive fix: ./scripts/fix-edu-lombok-issue.sh"
    echo "2. Restart your IDE"
    echo "3. Check annotation processing is enabled in your IDE"
else
    echo -e "\n❌ Build failed."
    echo "Please try the more comprehensive fix: ./scripts/fix-edu-lombok-issue.sh"
fi 