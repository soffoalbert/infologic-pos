#!/bin/bash

# Script to help with Lombok-related issues in the InfoLogic POS project

echo "InfoLogic POS - Lombok Helper Script"
echo "===================================="
echo

# Verify Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java not found. Please install Java 17 or higher."
    exit 1
fi

# Verify Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven not found. Please install Maven."
    exit 1
fi

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Get Lombok version from pom.xml
LOMBOK_VERSION=$(grep -A 1 "<artifactId>lombok</artifactId>" "$PROJECT_ROOT/pom.xml" | grep -o "<version>.*</version>" | sed 's/<version>\(.*\)<\/version>/\1/' | tr -d '[:space:]' || echo "1.18.36")

if [ -z "$LOMBOK_VERSION" ]; then
    LOMBOK_VERSION="1.18.36"  # Default if not found
fi

echo "Using Lombok version: $LOMBOK_VERSION"

# Download Lombok jar if needed
LOMBOK_JAR="$PROJECT_ROOT/lombok-$LOMBOK_VERSION.jar"
if [ ! -f "$LOMBOK_JAR" ]; then
    echo "Downloading Lombok $LOMBOK_VERSION..."
    curl -L "https://projectlombok.org/downloads/lombok-$LOMBOK_VERSION.jar" -o "$LOMBOK_JAR"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download Lombok. Please check your internet connection."
        exit 1
    fi
fi

echo
echo "Cleaning project..."
mvn clean -q
echo "Done!"

echo
echo "Options:"
echo "1. Install Lombok in your IDE"
echo "2. Verify Lombok is working"
echo "3. Clean and rebuild project"
echo "4. Update .gitignore for Lombok"
echo "5. Exit"
echo

read -p "Select an option (1-5): " option

case $option in
    1)
        echo "Running Lombok installer..."
        java -jar "$LOMBOK_JAR"
        echo
        echo "After installation, please restart your IDE."
        ;;
    2)
        echo "Checking if Lombok is working..."
        echo "Creating a test file with Lombok annotations..."
        
        # Create a test file
        TEST_FILE="$PROJECT_ROOT/src/test/java/com/infologic/pos/LombokTest.java"
        mkdir -p "$(dirname "$TEST_FILE")"
        
        cat > "$TEST_FILE" << EOF
package com.infologic.pos;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class LombokTest {
    
    @Test
    public void testLombokDataAnnotation() {
        TestData data = new TestData();
        data.setName("Test");
        data.setValue(123);
        
        assertEquals("Test", data.getName());
        assertEquals(123, data.getValue());
        
        log.info("Lombok @Data annotation is working!");
    }
    
    @Data
    public static class TestData {
        private String name;
        private int value;
    }
}
EOF
        
        echo "Running the test..."
        mvn test -Dtest=LombokTest
        
        echo
        if [ $? -eq 0 ]; then
            echo "Success! Lombok is working correctly."
        else
            echo "Test failed. Lombok may not be configured correctly."
            echo "Please check your IDE settings or run option 1 to install Lombok."
        fi
        ;;
    3)
        echo "Cleaning and rebuilding project..."
        mvn clean compile
        echo
        if [ $? -eq 0 ]; then
            echo "Build successful!"
        else
            echo "Build failed. Please check the error messages above."
        fi
        ;;
    4)
        echo "Updating .gitignore for Lombok..."
        if ! grep -q "lombok-.*\.jar" "$PROJECT_ROOT/.gitignore"; then
            echo "" >> "$PROJECT_ROOT/.gitignore"
            echo "# Lombok" >> "$PROJECT_ROOT/.gitignore"
            echo "lombok-*.jar" >> "$PROJECT_ROOT/.gitignore"
            echo ".lombok" >> "$PROJECT_ROOT/.gitignore"
            echo "Updated .gitignore to exclude Lombok files."
        else
            echo ".gitignore already contains Lombok exclusions."
        fi
        ;;
    5)
        echo "Exiting."
        exit 0
        ;;
    *)
        echo "Invalid option. Exiting."
        exit 1
        ;;
esac

echo
echo "Done!" 