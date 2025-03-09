FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/ipic-service-discovery-0.0.1-SNAPSHOT.jar ipic-discovery.jar

ENTRYPOINT ["java" , "-jar", "ipic-discovery.jar"]