# --- Stage 1: Build the Application ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the JAR file (skip tests to speed it up)
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR from the previous stage
# (Make sure the jar name matches what is in your pom.xml, usually matches artifactId-version)
COPY --from=build /app/target/multithreaded-api-1.0-SNAPSHOT.jar app.jar

# Expose the port (Render ignores this but it's good practice)
EXPOSE 8080

# Run the App
CMD ["java", "-jar", "app.jar"]