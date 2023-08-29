# Use a base image with Java and Maven installed
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the source code to the container
COPY . .
# Build the application
RUN mvn clean install package

FROM openjdk:17

WORKDIR /app

COPY --from=build /app/target/Mobile-Phone-Reservation-1.0-SNAPSHOT.jar .

EXPOSE 8080

# Define volumes for logs and configuration
VOLUME /app/logs
VOLUME /app/config

# Command to run the application
CMD ["java", "-jar", "Mobile-Phone-Reservation-1.0-SNAPSHOT.jar", "--spring.config.location=/app/conf/application.yaml"]
 