# Fix 13 — DTOs Tipados en Capa REST para Prevenir Fuga de Dominio y Eliminación de Mapas Genéricos

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Infraestructura (Adaptadores REST y DTOs)  
**Autor:** Antigravity  

---

## 1. Contexto y Diagnóstico

En la auditoría de Arquitectura Hexagonal y Clean Code se detectaron dos problemas de diseño en la capa de exposición web:

1. **Fuga del Modelo de Dominio hacia la Capa Web (Violación de Hexagonal):**
   - [TiendaController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/TiendaController.java) recibía directamente la entidad de dominio `Tienda` en `@RequestBody`.
   - [UsuarioController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/UsuarioController.java) recibía directamente la entidad de dominio `Usuario` en `@RequestBody`.
   - **Riesgo:** Un cliente HTTP externo podía enviar campos reservados como `id`, `creadoPor`, `codigo`, etc., y existía un acoplamiento directo entre el esquema JSON de la API y las entidades de negocio internas.

2. **Uso de Mapas Genéricos sin Tipado (`Map<String, Object>` y `Map<String, String>`):**
   - `TiendaController.asignarEncargado` y `CentroDistribucionController.asignarEncargado` recibían un `Map<String, Object> body`, parseando a mano `Long.valueOf(body.get("encargadoId").toString())`.
   - `UsuarioController.actualizarRol` recibía un `Map<String, String> body`.
   - **Riesgo:** Pérdida de validación en tiempo de compilación, riesgo de `NullPointerException` o `ClassCastException`, y falta de contratos claros para documentación de API (OpenAPI/Swagger).

---

## 2. Cambios Implementados

### A. Creación de DTOs Dedicados en `infrastructure/adapter/in/rest/dto/`

1. **[CrearTiendaRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CrearTiendaRequest.java):**
   - Encapsula exclusivamente los campos admitidos para dar de alta una tienda: `nombre`, `direccion`, `ciudad` y `encargadoId`.
   - Provee el método conversor `toDomain()` para aislar la entidad de dominio de la capa REST.

2. **[CrearUsuarioRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CrearUsuarioRequest.java):**
   - Encapsula `nombre`, `email` y `rol` con método conversor `toDomain()`.
   - Previene la inyección externa de `id`, `authUserId` o `creadoPor`.

3. **[AsignarEncargadoRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/AsignarEncargadoRequest.java):**
   - Java Record inmutable y fuertemente tipado:
     ```java
     public record AsignarEncargadoRequest(Long encargadoId) {}
     ```

4. **[ActualizarRolRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/ActualizarRolRequest.java):**
   - Java Record que vincula directamente el enum `Rol`:
     ```java
     public record ActualizarRolRequest(Rol rol) {}
     ```

### B. Refactorización de Controladores

* **`TiendaController`:**
  - `@PostMapping`: Ahora recibe `@RequestBody CrearTiendaRequest request` y convierte a dominio con `request.toDomain()`.
  - `@PutMapping("/{id}/encargado")`: Recibe `@RequestBody(required = false) AsignarEncargadoRequest request`.
* **`CentroDistribucionController`:**
  - `@PutMapping("/{id}/encargado")`: Recibe `@RequestBody(required = false) AsignarEncargadoRequest request`.
* **`UsuarioController`:**
  - `@PostMapping`: Recibe `@RequestBody CrearUsuarioRequest request` y convierte a dominio con `request.toDomain()`.
  - `@PatchMapping("/{id}/rol")`: Recibe `@RequestBody(required = false) ActualizarRolRequest request`, validando de forma tipada `request.rol()`.

### C. Manejo Global de Formatos y Enums Inválidos en [ApiExceptionHandler.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ApiExceptionHandler.java)

Se añadió el interceptor para `HttpMessageNotReadableException`:
```java
@ExceptionHandler(HttpMessageNotReadableException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String, String> mensajeNoLegible(HttpMessageNotReadableException exception) {
    return Map.of("error", "Formato de solicitud o valor no válido.");
}
```
Si un cliente envía un rol inexistente o un JSON con sintaxis rota, el servidor responde ordenadamente con `400 Bad Request` y el JSON estructurado estándar.

---

## 3. Verificación de Pruebas

Se ejecutó la suite completa de pruebas unitarias:
```bash
./mvnw test -Dtest="!OptiwayApplicationTests"
```
**Resultado:** **22 tests ejecutados, 0 fallos, 0 errores (BUILD SUCCESS)**.
