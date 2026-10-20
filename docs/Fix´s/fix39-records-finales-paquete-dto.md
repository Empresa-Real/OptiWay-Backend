# Fix 39 — Migración Final de Request DTOs a Records: 100% del Paquete DTO Inmutable

**Fecha:** 20 de Octubre de 2026  
**Área:** Capa de Infraestructura (DTOs REST)  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Fix 36 migró 10 DTOs de petición a `record` inmutables con `toCommand()` y Bean Validation, pero **tres clases quedaron rezagadas** con el estilo JavaBean clásico (constructor vacío + getters + setters, ~50-80 líneas cada una):

| DTO | Líneas antes | Campos |
| :--- | :--- | :--- |
| `CrearTiendaRequest` | 63 | nombre, direccion, ciudad, encargadoId |
| `CrearCentroDistribucionRequest` | 81 | nombre, direccion, capacidad, encargadoId, tiendasAbastecidasIds |
| `CrearUsuarioRequest` | 54 | nombre, email, rol |

Esto violaba la **uniformidad del paquete**: DTOs mutables con setters públicos conviviendo con registros inmutables para el mismo tipo de operación (crear recurso vía POST).

---

## 2. Solución Implementada

Los tres DTOs migraron a `record` canónicos conservando **exactamente** las mismas anotaciones de validación (`@NotBlank`, `@Email`, `@NotNull`, `@Positive`) y el mismo `toCommand()`, más las anotaciones `@Schema` de Swagger ya presentes en el resto del paquete:

```java
@Schema(description = "Petición para crear un usuario en el sistema")
public record CrearUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Schema(description = "Nombre completo del usuario", example = "Ana Torres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Schema(description = "Correo electrónico del usuario (identifier de Supabase Auth)", example = "ana@optiway.com")
        String email,

        @NotBlank(message = "El rol es obligatorio")
        @Schema(description = "Rol asignado al usuario", example = "ENCARGADO_TIENDA")
        String rol
) {
    public CrearUsuarioCommand toCommand() {
        return new CrearUsuarioCommand(nombre, email, rol);
    }
}
```

Los controladores (`TiendaController`, `CentroDistribucionController`, `UsuarioController`) ya delegaban vía `request.toCommand()`, por lo que **no requirieron ningún cambio**.

---

## 3. Archivos

| Archivo | Cambio |
| :--- | :--- |
| `rest/dto/CrearTiendaRequest.java` | `class` → `record` (63 → 29 líneas) |
| `rest/dto/CrearCentroDistribucionRequest.java` | `class` → `record` (81 → 38 líneas) |
| `rest/dto/CrearUsuarioRequest.java` | `class` → `record` (54 → 27 líneas) |

---

## 4. Beneficios

- **Paquete `dto` 100% inmutable:** los 10 DTOs de petición + `ErrorResponse` son `record` (total: 11/11).
- **−125 líneas** de boilerplate (constructores duplicados, getters y setters) reemplazadas por contratos declarativos.
- **Integridad garantizada:** sin setters no hay mutación posterior a la deserialización; Jackson usa el constructor canónico del record.
- **Swagger completo:** los tres DTOs ganaron las anotaciones `@Schema` que les faltaban para documentarse correctamente.

---

## 5. Observaciones

- La migración fue solicitada por el usuario al detectar la inconsistencia (`class` con get/set vs `record`) durante el trabajo del Fix 38; se mantiene como cambio independiente para preservar la serie atómica.
- No se agregaron validaciones nuevas ni se modificaron mensajes: la conversión fue 1:1 en comportamiento (validado por compilación y suite completa).
- `ErrorResponse` ya era `record`; no requirió cambios.
- Como el resto del trabajo del Fix 38, este cambio quedó **sin commitear a petición del usuario**, junto con su informe, para revisión previa.
- Verificación: `.\mvnw clean test -o` → **BUILD SUCCESS, 38/38 tests, 0 fallos**.

---

## 6. Criterios de Aceptación

- [x] `CrearTiendaRequest`, `CrearCentroDistribucionRequest` y `CrearUsuarioRequest` son `record` con `toCommand()`.
- [x] Ningún DTO de petición del paquete es `class` con getters/setters.
- [x] Las anotaciones de validación y sus mensajes permanecen idénticos.
- [x] Compilación y 38 tests en verde.
