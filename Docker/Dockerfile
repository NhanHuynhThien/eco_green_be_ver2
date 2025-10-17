# ================================
# Stage 1: Build the application
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven wrapper and pom first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# ================================
# Stage 2: Run the application
# ================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]