# Installing and Running the DBR Scala Application with Docker

## Overview

This guide details the steps to install and run the DBR Scala application, a robust solution leveraging the ZIO library for functional programming and http4s for HTTP service definition. Designed with Docker deployment in mind, the application is set to operate on a web server backend like Blaze, defaulting to listen on port 8080. Included within the package are scripts to facilitate both deployment and testing processes.

## Prerequisites

- Docker installed on your machine.
- Scala and sbt, if intending to build the application outside of Docker.
- Access to a terminal or command line interface.

## Installation Steps

### 1. Download the Application Archive

First, obtain the `DBR.zip` file containing the application's source code alongside essential scripts for deployment and testing.

### 2. Extract the Archive

Unzip the `DBR.zip` file, which yields a directory named `DBR`. Within this directory, you'll find the Scala application's source code and two pivotal scripts: `dbr_docker.sh` for deployment and `perform-requests.sh` for testing.

```bash
unzip DBR.zip
```

### 3. Make Scripts Executable

Transition into the `DBR` directory:

```bash
cd DBR
```

Subsequently, apply executable permissions to both scripts utilizing the `chmod +x` command as follows:

```bash
chmod +x dbr_docker.sh perform-requests.sh
```

This step is crucial for enabling script execution directly from the terminal.

## Running the Application

### Deploying with Docker

To deploy the application within a Docker container, execute the `dbr_docker.sh` script:

```bash
./dbr_docker.sh
```

Successful script execution will initiate an http4s web server on port `8080`, with the terminal displaying real-time logs, signifying the application's operational status.

### Testing the Application

Post-deployment, the application's functionality can be assessed through the `perform-requests.sh` script, which emulates HTTP requests akin to those used in unit testing:

```bash
./perform-requests.sh
```

Running this script executes a series of HTTP requests against the application, with the terminal outputting the respective responses for verification purposes.

## Expected Outcomes

- **Deployment**: Execution of `dbr_docker.sh` compiles the Scala application, encapsulates it within a Docker image, and launches a container set to receive HTTP requests on port `8080`.
- **Testing**: By running `perform-requests.sh`, predefined HTTP requests are sent to the application, replicating unit test scenarios with terminal-displayed responses for user review.

## Troubleshooting

- Confirm Docker's operational status on your system prior to running `dbr_docker.sh`.
- Encounter permission issues with script execution? Ensure both scripts have been granted executable permissions via `chmod +x`.
- Application start-up errors or operational issues? Consult Docker logs for insight.

---

### To Improve List
Concurrency and Atomicity:
Consider using ZIO's STM (Software Transactional Memory) for atomic state management when moving to a database or
persistent storage.

Validation and Error Handling: Ensure comprehensive validation of transaction requests to prevent processing invalid or
malicious data. Additionally, consistent and meaningful error responses will improve the API's usability and debuggability.

Persistence: For a production application,
integrating with a persistent storage system (e.g., relational database, NoSQL database)
would be necessary to maintain state across application restarts and ensure data durability.

Security: Implement security measures, such as authentication and authorization, to protect sensitive endpoints and data.
Ensure secure handling of user data and transactions.

Testing: Expanding the test coverage to include scenarios for concurrent transactions, edge cases in balance calculations,
and error handling would strengthen the application's reliability.

Documentation and API Design: Comprehensive documentation, including API endpoints, request/response schemas,
and error codes, will be beneficial for both development and usage of the application.
