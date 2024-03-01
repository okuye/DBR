#!/bin/bash

# Step 1: Compile and package the Scala application using sbt
echo "Compiling and packaging Scala application..."
sbt clean assembly

# Check if sbt succeeded
if [ $? -ne 0 ]; then
    echo "Scala application compilation and packaging failed."
    exit 1
fi

# Assuming the JAR is named dbr-scala-app-assembly-0.1.0-SNAPSHOT.jar and located in target/scala-2.13/
JAR_NAME="dbr-scala-app-assembly-0.1.0-SNAPSHOT.jar"
JAR_PATH="target/scala-2.13/$JAR_NAME"

# Step 2: Build the Docker image
echo "Building Docker image..."
docker build -t dbr-scala-app:0.1.0-SNAPSHOT .

# Check if Docker succeeded
if [ $? -ne 0 ]; then
    echo "Docker image build failed."
    exit 1
fi

# Optional: Remove any existing container with the same name
echo "Removing any existing Docker containers..."
docker rm -f scala-app

# Step 3: Run the Docker container
echo "Running Docker container..."
docker run -d -p 9000:9000 -p 9001:9001 --name scala-app dbr-scala-app:0.1.0-SNAPSHOT

# Check if Docker run succeeded
if [ $? -eq 0 ]; then
    echo "Docker container is running."
    echo "Following the logs. Press Ctrl+C to stop following."
    docker logs -f scala-app
else
    echo "Docker container failed to start."
    exit 1
fi
