# -- STAGE 1: Build --
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY common-lib ./common-lib
COPY recommend-service ./recommend-service
RUN cd common-lib && ./mvnw clean install -DskipTests
RUN cd recommend-service && ./mvnw clean package -DskipTests
# -- STAGE 2: Chạy --
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/recommend-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
