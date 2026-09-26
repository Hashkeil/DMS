FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY paperless-rest/pom.xml paperless-rest/pom.xml
COPY paperless-rest/src paperless-rest/src
RUN mvn --batch-mode --no-transfer-progress clean verify

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/paperless-rest/target/paperless-rest.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
