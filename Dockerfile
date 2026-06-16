# =====================================================================
# Dockerfile multi-stage para SIGA Backend (Spring Boot 4.1 / Java 21).
# Pensado para desplegar en Render conectándose a Neon Postgres.
#
# Las credenciales NO van en la imagen: se inyectan como variables de
# entorno en Render (SIGA_DB_URL, SIGA_DB_USER, SIGA_DB_PASSWORD).
# =====================================================================

# ---- Etapa 1: build (Maven + JDK 21) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cachea dependencias: primero el pom (capa estable), luego el código fuente.
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---- Etapa 2: runtime (solo JRE 21, imagen liviana) ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
# Copia el fat-jar ejecutable generado por spring-boot-maven-plugin.
COPY --from=build /app/target/*.jar app.jar
# Usuario no-root por buenas prácticas de seguridad.
RUN useradd -r -u 1001 siga && chown -R siga:siga /app
USER siga
# Render inyecta la variable PORT; la app la lee vía server.port=${PORT:8080}.
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
