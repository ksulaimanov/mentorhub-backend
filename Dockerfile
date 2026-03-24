# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Build with Maven
RUN ./mvnw clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run as non-root user
RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/false appuser
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=25.0", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

