# Fix 20: Validación Declarativa Bean Validation en Ingreso de Mercancía

## 1. Contexto y Diagnóstico
En la auditoría de `HU-18` (Fix 4, Tarea 2), se constató que el DTO `RegistrarIngresoMercanciaRequest` carecía de anotaciones Jakarta Bean Validation. El controlador `IngresoMercanciaController` no utilizaba `@Valid`, dejando pasar payloads inválidos (cantidades nulas o negativas, orígenes en blanco) hasta la capa de servicio. Además, el proyecto no contaba con la dependencia `spring-boot-starter-validation`.

## 2. Solución Aplicada
1. **Inclusión de Dependencia en `pom.xml`:**
   - Se agregó `spring-boot-starter-validation` para habilitar Jakarta Bean Validation en Spring Boot 3.
2. **Anotaciones Declarativas en el DTO:**
   - En `RegistrarIngresoMercanciaRequest`:
     - `@NotNull` en `centroDistribucionId` y `productoId`.
     - `@NotNull` y `@Positive` en `cantidad`.
     - `@NotBlank` en `origen`.
3. **Activación de Validación en el Adaptador REST:**
   - Se anotó `@Valid @RequestBody RegistrarIngresoMercanciaRequest request` en `IngresoMercanciaController`.
4. **Manejo Centralizado de Excepciones:**
   - Se añadió un manejador para `MethodArgumentNotValidException` en `ApiExceptionHandler`, retornando un mapa de errores con código `400 Bad Request`.

## 3. Matriz de Cambios
| Componente | Capa | Acción |
| :--- | :--- | :--- |
| `pom.xml` | Configuración | Inclusión de `spring-boot-starter-validation`. |
| `RegistrarIngresoMercanciaRequest.java` | Adaptador REST (DTO) | Anotaciones `@NotNull`, `@Positive`, `@NotBlank`. |
| `IngresoMercanciaController.java` | Adaptador REST | Inclusión de `@Valid` en `@RequestBody`. |
| `ApiExceptionHandler.java` | Adaptador REST | Handler para `MethodArgumentNotValidException`. |

## 4. Validación
- **Compilación:** `BUILD SUCCESS`.
- **Commit:** `3e4d2c3` (`feat: validacion declarativa @Valid y Bean Validation en ingreso de mercancia (Fix 20)`).
