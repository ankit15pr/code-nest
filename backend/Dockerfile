# Use a Java base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Update the apt package list and upgrade the system
RUN apt-get update -y && apt-get upgrade -y && echo "System updated"

# Install basic dependencies to support Node.js, Python, and C++
RUN apt-get install -y \
    curl \
    gnupg \
    ca-certificates \
    lsb-release \
    build-essential \
    python3 \
    python3-pip \
    nodejs \
    && echo "Development tools, Python, Node.js installed" \
    && rm -rf /var/lib/apt/lists/*  # Clean up apt cache to reduce image size

# Confirm that the required tools are installed
RUN python3 --version && pip3 --version && node --version && g++ --version && echo "All tools are installed"

# Copy the jar file from the target directory into the container
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Copy the wait-for-it.sh script into the container
COPY wait-for-it.sh /app/wait-for-it.sh

# Make the wait-for-it.sh script executable
RUN chmod +x /app/wait-for-it.sh

# Ensure the app.jar file has execute permissions
RUN chmod +x /app/app.jar

# Expose the port your Spring Boot app is running on (default 8080)
EXPOSE 8081

# Use wait-for-it.sh to wait for MySQL and then run the Spring Boot application
# CMD ["./wait-for-it.sh", "mysql-db:3306", "--", "java", "-jar", "app.jar"]
CMD ["./wait-for-it.sh", "mysql-db:3306", "--timeout=60", "--retries=5", "--", "java", "-jar", "app.jar"]
