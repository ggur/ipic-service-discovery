FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar ipic-discovery.jar

ENTRYPOINT ["java" , "-jar", "ipic-discovery.jar"]