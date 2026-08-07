# Stage 1: Build the package
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root group and user
RUN addgroup -S campusgroup && adduser -S campususer -G campusgroup

# Create uploads directory and grant ownership
RUN mkdir -p uploads/resources && chown -R campususer:campusgroup /app

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Run as non-root user
USER campususer

# Expose backend port
EXPOSE 8080

# Production-ready JVM flags
# -XX:+UseG1GC: Enable G1 Garbage Collector
# -XX:+UseContainerSupport: Ensure JVM respects container memory limits
# -XX:MaxRAMPercentage=75.0: Dynamically allocate up to 75% of container RAM to JVM
# -XX:+ExitOnOutOfMemoryError: Terminate the container if JVM runs out of memory so it restarts
ENV JVM_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
