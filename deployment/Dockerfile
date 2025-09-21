# --- Etapa 1: Construcción (Build Stage) ---
# Usamos una imagen con el JDK completo para compilar nuestro proyecto
FROM eclipse-temurin:17-jdk-jammy AS builder

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /workspace/app

# Copiamos los archivos de Gradle y descargamos las dependencias
COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY main.gradle .
RUN ./gradlew dependencies --no-daemon

# Copiamos el resto del código fuente y construimos el JAR
COPY . .
RUN ./gradlew bootJar --no-daemon

# --- Etapa 2: Ejecución (Run Stage) ---
# Usamos una imagen mucho más ligera, solo con el JRE, para la ejecución
FROM eclipse-temurin:17-jre-jammy

# Argumento para especificar el nombre del JAR
ARG JAR_FILE=/workspace/app/applications/app-service/build/libs/*.jar

# Establecemos el directorio de trabajo
WORKDIR /opt/app

# Copiamos solo el JAR compilado desde la etapa de construcción
COPY --from=builder ${JAR_FILE} app.jar

# Exponemos el puerto 8080 (lo cambiaremos en docker-compose si es necesario)
EXPOSE 8081

# Comando para ejecutar la aplicación cuando el contenedor arranque
ENTRYPOINT ["java","-jar","app.jar"]