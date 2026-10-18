# Fix 21: Segregación de Interfaces, Estandarización de `UUID` y Nombres de Parámetros

## 1. Contexto y Diagnóstico
1. **Violación del Principio de Segregación de Interfaces (ISP):**
   - El caso de uso `CambiarEstadoProductoUseCase` exponía el método `boolean tieneStockActivo(Long productoId)`, el cual no corresponde a la mutación de estado de producto sino a una consulta de inventario.
2. **Nombres Calificados `java.util.UUID`:**
   - Varios puertos de entrada utilizaban `java.util.UUID authUserId` inline en la firma en lugar de declarar `import java.util.UUID;`.
3. **Inconsistencia en Parámetros de Autenticación:**
   - Algunos puertos usaban `adminAuthUserId` mientras otros usaban `authUserId`, generando divergencia en la convención de inyección de credenciales.

## 2. Solución Aplicada
1. **Segregación de Caso de Uso (`VerificarStockProductoUseCase`):**
   - Se extrajo el método de consulta hacia su propio puerto de entrada:
     ```java
     public interface VerificarStockProductoUseCase {
         boolean tieneStockActivo(Long productoId);
     }
     ```
   - `ProductoService` implementa tanto `CambiarEstadoProductoUseCase` como `VerificarStockProductoUseCase`.
2. **Estandarización de `import java.util.UUID;`:**
   - Se agregaron las declaraciones de import en todos los puertos afectados (`CrearTiendaUseCase`, `ObtenerTiendasUseCase`, `ObtenerUsuariosUseCase`, `CrearCentroDistribucionUseCase`, `AsignarEncargadoTiendaUseCase`).
3. **Unificación a `authUserId`:**
   - Se renombraron los parámetros a `authUserId` en todos los casos de uso para reflejar consistencia con `@AuthUserId`.

## 3. Matriz de Cambios
| Componente | Capa | Acción |
| :--- | :--- | :--- |
| `VerificarStockProductoUseCase.java` | Puerto de Entrada | Creación de interfaz segregada (ISP). |
| `CambiarEstadoProductoUseCase.java` | Puerto de Entrada | Remoción de `tieneStockActivo`. |
| `ProductoService.java` | Aplicación | Implementación de ambos casos de uso. |
| Puertos de entrada varios | Puertos | Inclusión de `import java.util.UUID;` y unificación a `authUserId`. |

## 4. Validación
- **Compilación:** `BUILD SUCCESS`.
- **Commit:** `ac68d6a` (`refactor: segregar VerificarStockProductoUseCase e importar UUID consistentemente (Fix 21)`).
