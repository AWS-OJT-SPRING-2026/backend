# Build stage
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/BE-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on (default Spring Boot port)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
