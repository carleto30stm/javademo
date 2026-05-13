
Este repositorio contiene la implementación del Backend para el **Módulo de Gestión de Productos*, desarrollado con **Java 21** y **Spring Boot**.

## Redis##
levantar redis localmente antes de levantar el proyecto
    `docker-compose up -d`
    
El servicio está diseñado para centralizar la consulta, validación y reporte de productos, sirviendo datos a la interfaz de usuario.

## 🏛️ Estrategia de Arquitectura: Vertical Slices (Funcionalidad)

A diferencia de una arquitectura por capas tradicional, este proyecto utiliza un enfoque de **Slices Verticales** con una separación estricta entre el contrato API y la implementación.

## Estrategia de Desarrollo

Este proyecto tiene Devtools, para que funcione correctamente el IDE utilizado debe tener compilacion automatica
- `VSC` Asegurarse de tener Extension Pack for Java.
- `IntelliJ` Settings → Build, Execution, Deployment → Compiler → Build project automatically.

**El enfoque correcto del DTO es en la capa API** no en el service. El service debe poder confiar en que el input ya es valido
`Ejemplo`:
`@AssertTrue(message = "El campo fechaDesde no puede ser posterior a fechaHasta")`
` public boolean isFechaRangoValido() {`
` if (fechaDesde == null || fechaHasta == null) return true; // @NotNull ya maneja estos casos`
`  return !fechaDesde.isAfter(fechaHasta);`
`}`
**Manejo de Excepciones**
Nunca hacer `catch (Exception e)` silencioso.
Usar `@ControllerAdvice` para capturar excepciones y devolver un JSON de error estándar:

`{`
  `"timestamp": "2026-03-31T10:00:00Z",`
  `"traceId": "req-987654321",`
  `"status": 400,`
  `"error": "Bad Request",`
  `"codigo": "ERROR_VALIDACION",`
  `"message": "Datos de entrada invalidos",`
  `"path": "/api/v1/recurso",`
  `"details": [`
    `{`
      `"target": "password",`
      `"code": "MIN_LENGTH_NOT_MET",`
      `"message": "Se recibieron 2 caracteres, pero se requiere un mínimo de 8."`
    `}`
  `]`
`}`

### Conceptos Clave para el Equipo:
* **Separación de Contrato (API Interface):** Usaremos interfaces para definir el contrato REST. Estas interfaces contendrán todas las anotaciones de Spring (`@RequestMapping`, `@RequestParam`, etc.).
* **Implementación Limpia:** La clase Controller implementará dicha interfaz, pero contendrá solo código Java puro y lógica de orquestación, quedando libre de anotaciones de transporte.
* **Encapsulamiento por Dominio:** Cada carpeta raíz representa una capacidad de negocio (ej. `productos`). Todo lo que esa función necesita para trabajar vive junto.
* **Cumplimiento ISO 27001:** Esta estructura facilita la trazabilidad de los activos. Al separar el contrato de la implementación, se asegura un código más auditable y fácil de mantener ante revisiones de seguridad.

## 🚀 Stack Tecnológico

*   **Lenguaje:** Java 21 (JDK 21)
*   **Framework:** Spring Boot 4.0.x (Versión compatible con Java 21)
*   **Gestor de Dependencias:** Maven
*   **Documentación API:** OpenAPI 3.0 / Swagger UI
*   **Testing:** JUnit 5, Mockito
*   **Cobertura de Código:** JaCoCo (Mínimo requerido: **80%**)

---

## 📜 Estrategia de Desarrollo: Contract-First

Este proyecto sigue estrictamente una estrategia **Contract-First**. La implementación de los Controladores (`@RestController`) y DTOs debe reflejar fielmente las definiciones de la API acordadas con el equipo de Frontend.

### Artefactos del Contrato
Los desarrolladores deben basar su implementación en los siguientes archivos situados en la raíz del proyecto:

1.  **[`openapi.yaml`](./openapi.yaml):** Especificación técnica oficial. Define los endpoints, tipos de datos y validaciones.
2.  **[`api_full_examples.json`](./api_full_examples.json):** JSONs de referencia con datos reales.
    *   Este archivo muestra la estructura exacta de la respuesta para el **Listado** 
---

## 🛠 Endpoints Principales

El microservicio expone los siguientes recursos RESTful:

### 1. Listado y Búsqueda de Productos
*   **Método:** `GET /productos`
*   **Descripción:** Grilla paginada de productos.
    *   `page`, `limit` (Paginación)

### 2. Creacion de Productos
*   **Método:** `DELETE /productos/delete{id}`
*   **Descripción:** Elimina un producto.
*   **Secciones del JSON:**.

### 3. Guardado de Productos
*   **Método:** `POST /recetas`
*   **Descripción:** Guarda un producto.

---

## ✅ QA y Pruebas (Gherkin)

*   **Archivo de Escenarios:** [`doc/escenarios_backend.feature`](./doc/escenarios_backend.feature)
*   **Reporte de Cobertura:** Es obligatorio ejecutar `mvn clean test jacoco:report` y verificar que se cumpla el umbral del **80%** de cobertura en el código.

El equipo de QA y Desarrollo debe asegurar que las pruebas de integración cubran:
*   Filtros de búsqueda obligatorios.
*   Estructura correcta del JSON anidado.
*   Códigos de error HTTP (400 Bad Request, 404 Not Found).

### Por qué los Repositorios no tienen tests unitarios

Los repositorios (`domain/repository`) están **excluidos del cálculo de cobertura JaCoCo** y no cuentan con tests unitarios. Las razones son:

1. **Sin lógica de negocio:** Cada repositorio hace exactamente una cosa: ejecutar un SQL fijo y mapear el `ResultSet` a un modelo. No hay decisiones, transformaciones ni condiciones que requieran verificación unitaria.
2. **El `RowMapper` es mecánico:** Asigna columna por columna al modelo. Un test de este código solo verificaría que Mockito devuelve lo que se le configuró, sin valor real.
3. **Cobertura real via integración:** Los repositorios se verifican con la colección Postman/Newman en `/tests`, que corre contra un entorno Oracle real. Ese nivel de testing sí cubre el comportamiento observable.

Se establece como estándar el modelo de **Repositorio Unificado (In-Repo Testing)**.

Esto implica que los activos de prueba (Colecciones de Postman, Entornos y Scripts de Newman) **deben residir en el mismo repositorio de código fuente** que el backend, bajo la carpeta `/tests`.

Comando de consola de windows para generar reporte html de la herramienta newman:

newman run Auditoria_de_Recetas_API.postman_collection.json ^
-e "Auditoria_Dev_Environment.postman_environment.json" ^
-r cli,htmlextra ^
--reporter-htmlextra-export "reporte.html"

---

## 💻 Ejecución Local

### Prerrequisitos
*   JDK 21 instalado y configurado en el `PATH`.
*   Maven instalado (o usar `./mvnw`).

### Comandos
1.  **Compilar el proyecto:**
    ```bash
    mvn clean install
    ```
2.  **Ejecutar la aplicación:**
    ```bash
    mvn spring-boot:run
    ```
3.  **Ejecutar pruebas:**
    ```bash
    mvn test
    ```

La aplicación estará disponible por defecto en: `http://localhost:8098/api/v1`.
Swagger UI disponible en: `http://localhost:8080/api/v1/swagger-ui.html`.

OpenAPI JSON disponible en: `http://localhost:8080/api/v1/api-docs`.

---

## 📂 Estructura de Directorios (Sugerida)

```
src/main/java/com/flk/ms/auditoriaReceta
│
├── recetas                     <-- Slice Funcional: Gestión de Recetas
│   ├── api                     <-- Entrada: Contrato y Orquestación
│   │   ├── ProductoApi.java      <-- INTERFAZ: Contiene anotaciones REST (@GetMapping, etc.)
│   │   ├── ProductoController.java <-- IMPLEMENTACIÓN: Código Java limpio
│   │   ├── dto/                <-- Objetos de transferencia (mapeo de openapi.yaml)
│   │   └── mapper/             <-- Conversores DTO <-> Domain Model
│   ├── domain                  <-- Corazón: Lógica de negocio pura
│   │   ├── model/              <-- Entidades de Dominio
│   │   ├── service/            <-- Lógica de Auditoría y Reglas
│   │   └── repository/         <-- Interfaces de persistencia (Puertos)
│   └── infrastructure          <-- Salida: Implementación técnica
│       ├── persistence/        <-- Repositorios JPA y Entidades Oracle
│       └── reports/            <-- Generación de PDF trazable
│
├── maestros                    <-- Slice Funcional: Datos de soporte (Catálogos)
│   ├── api/                    <-- Sigue el mismo patrón (Interfaz / Implementación)
│   ├── domain/
│   └── infrastructure/
│
├── common                      <-- Transversal: Código compartido no funcional
│   ├── config/                 <-- Configuración Spring / Oracle
│   ├── exception/              <-- Manejo global de errores (ControllerAdvice)
│   └── security/               <-- Interceptores y Seguridad ISO 27001
├── test/

ms/auditoriaReceta/
├── src/                        <-- Código fuente y pruebas unitarias
├── tests/                      <-- Colecciones Postman / Newman (In-Repo Testing)
│   ├── Producto_API.postman_collection.json
│   └── Producto_Environment.postman_environment.json
```
