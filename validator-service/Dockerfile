# --- Etape 1 : build du JAR avec Maven + JDK 21 ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package

# --- Etape 2 : image d'execution, JRE 21 uniquement (image finale allegee) ---
# On utilise explicitement une image basee sur le JDK/JRE Eclipse Temurin
# (distribution OpenJDK) afin que le conteneur execute un environnement
# Java de bout en bout, conformement a la consigne du cours.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/validator-service.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
