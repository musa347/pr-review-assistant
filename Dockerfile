FROM openjdk:17-jdk-slim

# Install required tools
RUN apt-get update && apt-get install -y \
    git \
    bash \
    maven \
    curl \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# Install JDK 21 and 23 side-by-side (keeps JDK 17 as the default for building/running this app)
RUN curl -fsSL "https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk" -o /tmp/jdk21.tar.gz \
 && mkdir -p /opt \
 && tar -xzf /tmp/jdk21.tar.gz -C /opt \
 && rm /tmp/jdk21.tar.gz \
 && mv /opt/jdk-21* /opt/jdk-21
RUN curl -fsSL "https://api.adoptium.net/v3/binary/latest/23/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk" -o /tmp/jdk23.tar.gz \
 && mkdir -p /opt \
 && tar -xzf /tmp/jdk23.tar.gz -C /opt \
 && rm /tmp/jdk23.tar.gz \
 && mv /opt/jdk-23* /opt/jdk-23

# Set working directory
WORKDIR /app

# Copy Maven files first (for better caching)
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src
COPY scripts ./scripts

# Build the application using JDK 17 (default)
RUN mvn clean package -DskipTests

# Create artifacts directory
RUN mkdir -p artifacts

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/review-assistant-0.0.1-SNAPSHOT.jar"]