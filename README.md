# Microservicio de Solicitudes - CrediYa

Este microservicio es el componente central para la gestión del ciclo de vida de las solicitudes de préstamos en la plataforma **CrediYa**. Su responsabilidad principal es procesar, validar y almacenar todas las nuevas solicitudes de crédito generadas por los usuarios.

El servicio está construido siguiendo los principios de la **Arquitectura Limpia (Hexagonal)** para garantizar su autonomía, escalabilidad y facilidad de mantenimiento dentro del ecosistema de microservicios.

## Historia de Usuario Implementada (HU2): Crear Solicitud de Préstamo

La funcionalidad principal de este microservicio es permitir a los usuarios **crear una nueva request de préstamo**.

**Funcionalidades Clave Implementadas:**
* Exposición de un endpoint `POST` para registrar nuevas solicitudes.
* Recepción de los datos clave del préstamo: `monto`, `plazo`, `email` del cliente y `id` del tipo de préstamo.
* **Validación robusta de la información:**
    * Verifica que el tipo de préstamo seleccionado exista en el sistema.
    * Valida que el monto solicitado se encuentre dentro de los rangos (mínimo y máximo) permitidos para ese tipo de préstamo.
    * Asegura que los datos de entrada básicos (email, monto, plazo) sean válidos.
* Asignación automática del estado inicial **"RECIBIDA"** a toda nueva request.
* Persistencia de la request validada y enriquecida en la base de datos.
* Gestión de errores clara y consistente a través de respuestas HTTP bien definidas.

---

## ?? Arquitectura y Tecnologías

* **Arquitectura**: Limpia / Hexagonal (Scaffold Bancolombia).
* **Lenguaje**: Java 17
* **Framework**: Spring Boot 3 con WebFlux (Programación Reactiva)
* **Base de Datos**: PostgreSQL
* **Capa de Persistencia**: R2DBC (Reactiva)
* **Gestor de Dependencias**: Gradle

---

## ? Estructura del Proyecto

El proyecto mantiene una estricta separación de responsabilidades a través de su estructura modular:

* `applications/app-service`: Módulo principal que ensambla y ejecuta la aplicación.
* `domain/model`: Contiene las entidades (`Solicitud`, `TipoPrestamo`, `Estado`) y los puertos (`...Repository`).
* `domain/usecase`: Contiene la lógica de negocio pura (`SolicitudUseCase`).
* `infrastructure/entry-points/api-rest`: Implementa el controlador REST (`ApiRest.java`).
* `infrastructure/driven-adapters/r2dbc-postgresql`: Implementa la comunicación con la base de datos PostgreSQL.

---

## ? Cómo Ejecutar el Proyecto

### Pre-requisitos
* JDK 17 o superior.
* Gradle 7.x o superior.
* Tener una instancia de PostgreSQL corriendo.

### 1. Configuración de la Base de Datos
Antes de ejecutar la aplicación, es necesario crear la base de datos y las tablas correspondientes.
1.  Crea una nueva base de datos en PostgreSQL: `CREATE DATABASE solicitudes_db;`
2.  Conéctate a `solicitudes_db` y ejecuta los scripts de creación de tablas e inserción de datos iniciales que se encuentran en el proyecto.

### 2. Configuración de la Aplicación
1.  Navega al archivo `solicitudes/applications/app-service/src/main/resources/application.yml`.
2.  Asegúrate de que las propiedades de conexión coincidan con tu base de datos local, bajo el prefijo `adapters.r2dbc`:
    ```yaml
    adapters:
      r2dbc:
        host: localhost
        port: 5432
        database: solicitudes_db
        schema: public
        username: tu_usuario
        password: tu_contraseña
    ```

### 3. Ejecución
Abre una terminal en la raíz de este microservicio y ejecuta:

```bash
./gradlew bootRun
```
El servicio estará disponible en `http://localhost:8080`.

---

## ? Endpoints de la API (HU2)

### Crear una Nueva Solicitud de Préstamo
* **Método:** `POST`
* **URL:** `/api/v1/solicitudes`
* **Descripción:** Crea una nueva request de préstamo.

**Ejemplo de Request Body (`SolicitudDTO`):**
```json
{
  "monto": 1500000.00,
  "plazo": 24,
  "email": "nuevo.cliente@example.com",
  "idTipoPrestamo": 1
}
```

**Respuesta Exitosa (201 CREATED):**
```json
{
    "idSolicitud": 1,
    "monto": 1500000.00,
    "plazo": 24,
    "email": "nuevo.cliente@example.com",
    "idEstado": 1,
    "idTipoPrestamo": 1
}
```

**Respuesta de Error (400 BAD REQUEST):**
```json
{
    "message": "El monto solicitado está fuera de los rangos permitidos para el tipo de préstamo."
}
```

### Documentación Interactiva (Swagger)
La documentación completa de la API, con modelos y la capacidad de probar los endpoints, está disponible en:

* **URL de Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## ? Calidad de Código

### Pruebas
Se han implementado pruebas unitarias para el `SolicitudUseCase` que validan todos los flujos de la lógica de negocio, alcanzando una **cobertura superior al 90%**. Para ejecutar las pruebas:

```bash
./gradlew test
```

### Análisis Estático de Código
Se recomienda el uso del plugin **SonarLint** en el IDE para la validación continua de la calidad del código.