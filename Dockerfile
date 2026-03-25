FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
USER appuser

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=25.0", "-Dspring.profiles.active=prod", "-jar", "app.jar"]