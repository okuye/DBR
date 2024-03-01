# Use an official OpenJDK runtime as a parent image
FROM openjdk:11

# Set the working directory in the container to /app
WORKDIR /app

# Copy the compiled JAR(s) into the container at /app
COPY ./target/scala-2.13 /app

# Define environment variables if needed
ENV JAVA_OPTS=""

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the Scala application when the container launches
# Update the JAR file name to match your Scala application's JAR
CMD ["java", "-jar", "dbr-scala-app-assembly-0.1.0-SNAPSHOT.jar"]


