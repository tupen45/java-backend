# --- Stage 1: Build the Application ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the JAR file
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# ⚠ CHANGED: Switched to eclipse-temurin (Official OpenJDK successor)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/multithreaded-api-1.0-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Run the App
CMD ["java", "-jar", "app.jar"]