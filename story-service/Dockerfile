# -- STAGE 1: Build --
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY common-lib ./common-lib
COPY story-service ./story-service
RUN cd common-lib && mvn clean install -DskipTests
RUN cd story-service && mvn clean package -DskipTests
# -- STAGE 2: Chạy --
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/story-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
