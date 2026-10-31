# Build stage — JDK 21. Builds from the repo ROOT so the Maven reactor resolves the
# internal `sdk-identity-jvm` + `sdk-identity-jvm-spring-boot-starter` modules.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY sdk-identity-jvm ./sdk-identity-jvm
COPY sdk-identity-jvm-spring-boot-starter ./sdk-identity-jvm-spring-boot-starter
COPY api-identity-jvm ./api-identity-jvm
# api-auth is only needed so Maven can read the full reactor declared in the root pom.xml
# (the -pl/-am build itself does not build it).
COPY api-auth ./api-auth
RUN mvn -q -pl api-identity-jvm -am package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/api-identity-jvm/target/*.jar app.jar
EXPOSE 4101
ENTRYPOINT ["java", "-jar", "app.jar"]
