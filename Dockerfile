# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy the gradle configuration files first
COPY gradle/ gradle/
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle .
COPY build.gradle .

# Copy the source code
COPY src/ src/

# Install protoc (for protocol buffers)
RUN apt-get update && apt-get install -y protobuf-compiler

# Build the application
RUN ./gradlew build -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]