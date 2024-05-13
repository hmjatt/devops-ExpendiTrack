# Build stage: Use Maven to compile the project
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /home/app
# Copy source code and project definition, then package the application
COPY src ./src
COPY pom.xml .
RUN mvn clean package

# Runtime stage: Use a slim Java image to run the packaged application
FROM amazoncorretto:17-alpine
# Copy the built application jar from the build stage
COPY --from=build /home/app/target/*.jar /usr/local/lib/backend.jar
# Ensure the application jar is executable
RUN chmod +x /usr/local/lib/backend.jar

# Command to run the application
ENTRYPOINT ["java","-jar","/usr/local/lib/backend.jar"]