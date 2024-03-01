#!/bin/bash

# Step 1: Compile and package the Scala application
echo "Compiling and packaging Scala application..."
sbt clean compile package

if [ $? -ne 0 ]; then
    echo "Scala application compilation and packaging failed."
    exit 1
fi

# Step 2: Build the Docker image
echo "Building Docker image..."
sbt docker:publishLocal

if [ $? -ne 0 ]; then
    echo "Docker image build failed."
    exit 1
fi

# Optional: Remove any existing container with the same name
echo "Removing any existing Docker containers..."
docker rm -f scala-app

# Step 3: Run the Docker container
echo "Running Docker container..."
docker run -d -p 8080:8080 --name scala-app drb:0.1.0-SNAPSHOT

if [ $? -eq 0 ]; then
    echo "Docker container is running."
    echo "Following the logs. Press Ctrl+C to stop following."
    docker logs -f scala-app
else
    echo "Docker container failed to start."
    exit 1
fi
