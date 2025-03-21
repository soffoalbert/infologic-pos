#!/bin/bash

# Script to fix the "edu cannot be resolved to a type" issue with Lombok
# This is a specialized fix for InfoLogic POS project

echo "InfoLogic POS - Lombok EDU Error Fix Script"
echo "=========================================="
echo

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Check if running in the right directory
if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
    echo "Error: pom.xml not found. Please run this script from the project root directory."
    exit 1
fi

# Create a backup of the .m2 repository lombok files
echo "Creating backup of Lombok files in Maven repository..."
LOMBOK_M2_PATH=$(find "$HOME/.m2/repository" -name "lombok-*.jar" -type f | head -n 1)
if [ -n "$LOMBOK_M2_PATH" ]; then
    LOMBOK_M2_DIR=$(dirname "$LOMBOK_M2_PATH")
    BACKUP_DIR="$HOME/.m2/lombok-backup-$(date +%Y%m%d%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    cp -r "$LOMBOK_M2_DIR" "$BACKUP_DIR"
    echo "Backup created at: $BACKUP_DIR"
else
    echo "No Lombok JAR found in Maven repository. Skipping backup."
fi

# Clean Maven repository cache for Lombok
echo "Cleaning Maven repository Lombok cache..."
find "$HOME/.m2/repository/org/projectlombok" -name "*.jar" -delete
find "$HOME/.m2/repository/org/projectlombok" -name "*.lastUpdated" -delete
find "$HOME/.m2/repository/org/projectlombok" -name "*.repositories" -delete
find "$HOME/.m2/repository/org/projectlombok" -name "resolver-status.properties" -delete
echo "Maven Lombok cache cleared."

# Check Maven version
echo "Checking Maven version..."
mvn --version

# Get Lombok version from pom.xml
LOMBOK_VERSION=$(grep -A 1 "<artifactId>lombok</artifactId>" "$PROJECT_ROOT/pom.xml" | grep -o "<version>.*</version>" | sed 's/<version>\(.*\)<\/version>/\1/' | tr -d '[:space:]' || echo "1.18.36")
echo "Detected Lombok version: $LOMBOK_VERSION"

# Download the latest Lombok version
echo "Downloading the latest Lombok JAR..."
LOMBOK_JAR="$PROJECT_ROOT/lombok-$LOMBOK_VERSION.jar"
curl -L "https://projectlombok.org/downloads/lombok-$LOMBOK_VERSION.jar" -o "$LOMBOK_JAR"
if [ $? -ne 0 ]; then
    echo "Error: Failed to download Lombok. Please check your internet connection."
    exit 1
fi

# Install Lombok in IDE
echo "Installing Lombok in your IDE..."
java -jar "$LOMBOK_JAR"

# Create a new Maven settings.xml with annotation processor path
echo "Creating a Maven settings.xml with annotation processor configuration..."
SETTINGS_FILE="$PROJECT_ROOT/mvn-lombok-settings.xml"

cat > "$SETTINGS_FILE" << EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <profiles>
    <profile>
      <id>lombok-fix</id>
      <properties>
        <lombok.version>$LOMBOK_VERSION</lombok.version>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>lombok-fix</activeProfile>
  </activeProfiles>
</settings>
EOF

echo "Settings file created at: $SETTINGS_FILE"

# Create a new target directory
echo "Creating a clean target directory..."
rm -rf "$PROJECT_ROOT/target"
mkdir -p "$PROJECT_ROOT/target"

# Modify IDE-specific files if needed
if [ -d "$PROJECT_ROOT/.idea" ]; then
    echo "IntelliJ IDEA project detected. Configuring annotation processing..."
    IDEA_CONFIG_DIR="$PROJECT_ROOT/.idea"
    
    # Create or update compiler.xml for annotation processing
    mkdir -p "$IDEA_CONFIG_DIR"
    COMPILER_XML="$IDEA_CONFIG_DIR/compiler.xml"
    
    if [ -f "$COMPILER_XML" ]; then
        # Backup the original
        cp "$COMPILER_XML" "$COMPILER_XML.bak"
        
        # Update existing file to enable annotation processing
        if grep -q "<annotationProcessing>" "$COMPILER_XML"; then
            sed -i.tmp 's/<annotationProcessing>/<annotationProcessing>\n      <profile default="true" name="Default" enabled="true">\n        <processorPath useClasspath="false">\n          <entry name="$MAVEN_REPOSITORY$/org\/projectlombok\/lombok\/'"$LOMBOK_VERSION"'\/lombok-'"$LOMBOK_VERSION"'.jar" \/>\n        <\/processorPath>\n      <\/profile>/' "$COMPILER_XML"
        else
            sed -i.tmp 's/<component name="CompilerConfiguration">/<component name="CompilerConfiguration">\n  <annotationProcessing>\n    <profile default="true" name="Default" enabled="true">\n      <processorPath useClasspath="false">\n        <entry name="$MAVEN_REPOSITORY$\/org\/projectlombok\/lombok\/'"$LOMBOK_VERSION"'\/lombok-'"$LOMBOK_VERSION"'.jar" \/>\n      <\/processorPath>\n    <\/profile>\n  <\/annotationProcessing>/' "$COMPILER_XML"
        fi
        rm -f "$COMPILER_XML.tmp"
    else
        # Create a new compiler.xml
        cat > "$COMPILER_XML" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="CompilerConfiguration">
    <annotationProcessing>
      <profile default="true" name="Default" enabled="true">
        <processorPath useClasspath="false">
          <entry name="\$MAVEN_REPOSITORY$/org/projectlombok/lombok/$LOMBOK_VERSION/lombok-$LOMBOK_VERSION.jar" />
        </processorPath>
      </profile>
    </annotationProcessing>
  </component>
</project>
EOF
    fi
    
    echo "IntelliJ IDEA annotation processing configured."
fi

# Clean and build with Maven using the custom settings
echo "Cleaning and rebuilding the project..."
mvn clean compile -s "$SETTINGS_FILE" -Dmaven.compiler.forceJavacCompilerUse=true -Dmaven.compiler.fork=true

echo
echo "Fix attempt completed."
echo
echo "If the issue persists, try these manual steps:"
echo "1. Delete the '.m2/repository/org/projectlombok' directory completely"
echo "2. Restart your IDE"
echo "3. Run 'mvn clean install -U' to force update dependencies"
echo "4. Check that annotation processing is enabled in your IDE"
echo
echo "For IntelliJ IDEA users:"
echo "- Go to Settings > Build, Execution, Deployment > Compiler > Annotation Processors"
echo "- Check 'Enable annotation processing'"
echo "- Choose 'Obtain processors from project classpath'"
echo
echo "For Eclipse users:"
echo "- Right-click on the project > Properties > Java Compiler > Annotation Processing"
echo "- Enable project specific settings and annotation processing"
echo
echo "If all else fails, you can try manually adding the Lombok JAR to your build path." 