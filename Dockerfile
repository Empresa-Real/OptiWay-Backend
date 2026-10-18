# Etapa 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar configuración de Maven y dependencias primero para aprovechar cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Runtime ultra ligero
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar únicamente el archivo JAR generado
COPY --from=builder /app/target/*.jar app.jar

# Configurar puerto dinámico, desactivar Swagger en Docker y límite de RAM (256MB) para Render Free Tier (512MB)
ENV PORT=8080
ENV SWAGGER_ENABLED=false
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx256m", "-Xss512k", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar"]
