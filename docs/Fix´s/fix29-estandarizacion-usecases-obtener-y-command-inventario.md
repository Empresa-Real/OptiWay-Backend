# Fix 29 — Estandarización de Casos de Uso de Consulta bajo el Prefijo Obtener y Creación de ObtenerInventarioCommand

## 1. Contexto y Diagnóstico del Problema

Durante la auditoría de los puertos de entrada de lectura (`application.port.in`), se detectaron dos inconsistencias arquitectónicas principales:

1. **Inconsistencia de Nomenclatura en Operaciones de Consulta:**
   Los casos de uso de lectura utilizaban verbos dispares para la misma semántica de consulta de datos:
   - `ConsultarInventarioUseCase` (usaba el verbo *Consultar*).
   - `ListarCentrosDistribucionUseCase` (usaba el verbo *Listar*).
   - `ObtenerProductosUseCase`, `ObtenerTiendasUseCase`, `ObtenerUsuariosUseCase` (usaban el verbo *Obtener*).
   Esta disparidad reducía la coherencia léxica del equipo y dificultaba la navegación en la base de código.

2. **Firma Posicional Compleja en Consulta de Inventario:**
   `ConsultarInventarioUseCase` exponía un método con **6 parámetros escalares** (`UUID authUserId, Long tiendaId, Long centroDistribucionId, String nombre, String categoria, Long productoId`), conteniendo además reglas invariantes no expresadas por el sistema de tipos (*"uno de tiendaId o centroDistribucionId debe ser null y el otro no"*).
   Cualquier cambio futuro en los filtros obligaba a modificar la firma del método en cascada.

---

## 2. Solución Aplicada

### A. Unificación Léxica al Prefijo `Obtener*`
Se renombraron las interfaces y métodos para consolidar una convención homogénea en todo el dominio de aplicación:
- `ConsultarInventarioUseCase` $\rightarrow$ `ObtenerInventarioUseCase` (`obtenerInventario`).
- `ListarCentrosDistribucionUseCase` $\rightarrow$ `ObtenerCentrosDistribucionUseCase` (`obtenerCentrosDistribucion`).

### B. Encapsulación en `ObtenerInventarioCommand`
Se diseñó un Java Record inmutable para agrupar los criterios de consulta de existencias:

```java
public record ObtenerInventarioCommand(
        Long tiendaId,
        Long centroDistribucionId,
        String nombre,
        String categoria,
        Long productoId
) {
}
```

La firma del caso de uso queda reducida a:
```java
List<InventarioResponse> obtenerInventario(UUID authUserId, ObtenerInventarioCommand command);
```

---

## 3. Archivos Intervenidos

### Nuevos Archivos:
- `application/port/in/ObtenerInventarioCommand.java`
- `application/port/in/ObtenerInventarioUseCase.java`
- `application/port/in/ObtenerCentrosDistribucionUseCase.java`

### Archivos Eliminados:
- `application/port/in/ConsultarInventarioUseCase.java`
- `application/port/in/ListarCentrosDistribucionUseCase.java`

### Interfaces Actualizadas:
- `application/port/in/ObtenerProductosUseCase.java`
- `application/port/in/ObtenerTiendasUseCase.java`
- `application/port/in/ObtenerUsuarioUseCase.java`
- `application/port/in/ObtenerUsuariosUseCase.java`

---

## 4. Beneficios Obtenidos

1. **Predecibilidad del API Interno:** Toda consulta de entidades sigue el patrón canónico `Obtener*UseCase`.
2. **Extensibilidad sin Breaking Changes:** Nuevos filtros de inventario (p. ej., estado de stock o rango de fechas) pueden añadirse al record `ObtenerInventarioCommand` sin romper la firma del caso de uso.
3. **Reducción de Deuda Técnica:** Eliminación de interfaces y métodos huérfanos o redundantes.

---

## 5. Observaciones

- **Grupo atómico "Obtener":** junto con los puertos se incluye `application/service/InventarioService.java` (su implementación), de modo que el grupo de consulta de inventario viaja completo de punta a punta en este commit.
- **Regla XOR tienda/CD no es Bean-Validable:** la invariante *"uno de `tiendaId` o `centroDistribucionId` debe ser null"* no tiene anotación estándar de `jakarta.validation`; se mantiene en el servicio (`validarAcceso` + construcción de la consulta). El record solo encapsula los filtros, sin cambio de comportamiento.
- **Naming simétrico de usuario:** `obtenerUsuario(Long)` → `obtenerUsuarioPorId(UUID authUserId, Long id)`, paralelo a `obtenerUsuarioPorAuthId(UUID authUserId)`. El refuerzo de `requireAuthenticated` en `UsuarioService` viaja en `fix35`.
- **Retornos DTO:** los 6 puertos `Obtener*` devuelven DTOs de aplicación (`*Response`); el mapeo `from(...)` vive en los servicios, nunca en los controladores (detalle en `fix35` y `fix37`).
- **Verificación:** `mvnw clean test -o`: BUILD SUCCESS, 38/38 pruebas, 0 fallos.
