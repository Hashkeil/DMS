FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY paperless-rest/pom.xml paperless-rest/pom.xml
COPY paperless-ocr-worker/pom.xml paperless-ocr-worker/pom.xml
COPY paperless-ocr-worker/src paperless-ocr-worker/src
RUN mvn --batch-mode --no-transfer-progress -pl paperless-ocr-worker -am clean verify

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/paperless-ocr-worker/target/paperless-ocr-worker.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
