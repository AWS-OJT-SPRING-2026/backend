# Build stage
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# ── Timezone: force Asia/Ho_Chi_Minh at OS + JVM level ──────────────────────
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy the built jar file from the build stage
COPY --from=build /app/target/BE-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on (default Spring Boot port)
EXPOSE 8080

# Command to run the application — JVM timezone flag as belt-and-suspenders
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-jar", "app.jar"]
