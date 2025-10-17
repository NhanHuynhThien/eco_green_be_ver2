# ================================
# Stage 1: Build the application
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven files để cache dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source và build
COPY src ./src
RUN ./mvnw clean package -DskipTests -T 1C

# ================================
# Stage 2: Run the application
# ================================
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy jar từ build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
