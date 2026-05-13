# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copiar archivos necesarios para Maven
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

# Copiar código fuente
COPY src/ src/

# Compilar y empaquetar
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR del stage anterior
COPY --from=builder /workspace/target/*.jar app.jar

# Puerto por defecto de Spring Boot
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
