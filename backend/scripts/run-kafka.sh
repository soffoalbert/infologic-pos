#!/bin/bash

# Script to run a local Kafka instance using Docker Compose

echo "Setting up local Kafka environment..."

# Navigate to the script directory
cd "$(dirname "$0")" || exit

# Create docker-compose.yml file for Kafka if it doesn't exist
if [ ! -f docker-compose-kafka.yml ]; then
    cat > docker-compose-kafka.yml << EOF
version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    healthcheck:
      test: echo srvr | nc zookeeper 2181 || exit 1
      interval: 10s
      timeout: 5s
      retries: 5

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      zookeeper:
        condition: service_healthy
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    healthcheck:
      test: kafka-topics --bootstrap-server kafka:9092 --list || exit 1
      interval: 10s
      timeout: 10s
      retries: 5

  # Kafka UI for easy visualization
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      kafka:
        condition: service_healthy
    ports:
      - "8085:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
EOF
    echo "Created docker-compose-kafka.yml"
else
    echo "docker-compose-kafka.yml already exists"
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Start Kafka using Docker Compose
echo "Starting Kafka..."
docker-compose -f docker-compose-kafka.yml up -d

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
sleep 10

echo "Kafka should now be available on localhost:29092"
echo "Kafka UI is available at http://localhost:8085"
echo ""
echo "To stop Kafka, run: docker-compose -f docker-compose-kafka.yml down" 