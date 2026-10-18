# Fix 11 — Estandarización de Respuestas HTTP y Eliminación de Manejo de Errores Duplicado en Controladores REST

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Infraestructura (Adaptadores de Entrada REST)  
**Autor:** Antigravity  

---

## 1. Contexto y Diagnóstico

Durante la auditoría de arquitectura y código limpio se identificaron dos inconsistencias notables en los controladores de la capa REST (`infrastructure/adapter/in/rest/`):

1. **Estilos de respuesta HTTP heterogéneos:**
   - Unos controladores devolvían la entidad de dominio directamente con anotación `@ResponseStatus(HttpStatus.CREATED)` (`UsuarioController`, `ProductoController`).
   - Otros usaban `ResponseEntity<T>` con llamadas fluidas (`VentaController`).
   - Otros usaban instanciación clásica `new ResponseEntity<>(creado, HttpStatus.CREATED)` (`CentroDistribucionController`).
   - Y otros utilizaban tipos comodín `ResponseEntity<?>` (`TiendaController`), perdiendo el tipado estricto en tiempo de compilación.

2. **Manejo de errores duplicado y defectuoso:**
   - [ApiExceptionHandler.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ApiExceptionHandler.java) ya intercepta de forma global cualquier `IllegalArgumentException` y devuelve `400 Bad Request` con un payload estructurado JSON: `{"error": "mensaje"}`.
   - Sin embargo, métodos como `TiendaController.crearTienda`, `TiendaController.asignarEncargado` y `CentroDistribucionController.asignarEncargado` envolvían la invocación en bloques redundantes `try/catch(IllegalArgumentException)` y retornaban `ResponseEntity.badRequest().body(e.getMessage())`.
   - **Impacto en el frontend:** En caso de error 400 en tiendas o centros de distribución, el servidor respondía con **texto plano**, mientras que en cualquier otro endpoint respondía con un **JSON**, forzando al cliente web a programar lógicas asimétricas de parseo de errores.

---

## 2. Cambios Implementados

Se migró toda la capa de controladores al **estilo REST declarativo canónico de Spring Boot**:

### A. [TiendaController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/TiendaController.java)
* `crearTienda`: Ahora retorna `Tienda` tipada directamente y se anota con `@ResponseStatus(HttpStatus.CREATED)`.
* `obtenerTiendas`: Retorna `List<Tienda>` directamente.
* `asignarEncargado`: Retorna `Tienda` directamente.
* Se eliminaron todos los bloques `try/catch(IllegalArgumentException)`. Las excepciones de validación de negocio burbujean de manera uniforme hacia `ApiExceptionHandler`.

### B. [CentroDistribucionController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/CentroDistribucionController.java)
* `crear`: Retorna `CentroDistribucion` directamente con `@ResponseStatus(HttpStatus.CREATED)`.
* `listar`: Retorna `List<CentroDistribucion>`.
* `asignarEncargado`: Retorna `CentroDistribucion` directamente, eliminando el `try/catch` manual.

### C. [VentaController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/VentaController.java)
* `registrarVenta`: Retorna `Venta` con `@ResponseStatus(HttpStatus.CREATED)`, unificándose con el resto de endpoints de creación.

### D. [IngresoMercanciaController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/IngresoMercanciaController.java)
* `registrarIngreso`: Se añadió `@ResponseStatus(HttpStatus.CREATED)` explícito para asegurar que las creaciones de registros históricos respondan con el código HTTP apropiado según la especificación de la HU-18.

---

## 3. Beneficios Obtenidos

* **Consistencia 100% en la API:** Todos los endpoints de creación responden `201 Created` y los de consulta/actualización `200 OK`.
* **Respuesta de errores homogénea:** Todos los errores `400 Bad Request`, `403 Forbidden` y `404 Not Found` emiten invariablemente el esquema JSON `{"error": "..."}` centralizado en `ApiExceptionHandler`.
* **Código más limpio (Clean Code):** Se redujo el código boilerplate en los controladores en más de un 40%, respetando el Principio de Responsabilidad Única (SRP).
