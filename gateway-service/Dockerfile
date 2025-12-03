# Usamos solo una etapa - esperamos que el JAR ya esté compilado
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR ya compilado desde el host
COPY target/gateway-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
