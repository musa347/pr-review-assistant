FROM openjdk:17-jdk-slim

# Install git and bash (needed for analysis script)
RUN apt-get update && apt-get install -y git bash maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven files first (for better caching)
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src
COPY scripts ./scripts

# Build the application
RUN mvn clean package -DskipTests

# Create artifacts directory
RUN mkdir -p artifacts

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/review-assistant-0.0.1-SNAPSHOT.jar"]