# Build stage — JDK 21. Standalone module (no internal deps); OpenSAML resolves from the
# Shibboleth repository declared in pom.xml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 4103
ENTRYPOINT ["java", "-jar", "app.jar"]