# Fix 17: Unificación de Servicios de Inventario y Eliminación de Duplicados

## 1. Contexto y Diagnóstico
Previamente existían dos servicios paralelos y parcialmente duplicados para la consulta de inventario:
- `InventarioService.java` (Inventario de Tienda).
- `ConsultarInventarioCDService.java` (Inventario de Centros de Distribución).

Ambos servicios contenían lógica idéntica de carga batch de productos (`findAllById`), enriquecimiento del modelo (`enriquecerConProducto`), cálculo de banderas de stock bajo y filtrado por nombre y categoría.

## 2. Solución Aplicada
1. **Consolidación en `InventarioService`:**
   - Se integró la lógica de consulta para Centros de Distribución dentro de `InventarioService`.
   - Se unificó el procesamiento de consultas batch, normalización de strings de búsqueda y enriquecimiento en métodos privados reutilizables.
2. **Eliminación de Código Muerto:**
   - Se eliminó definitivamente el archivo redundante `ConsultarInventarioCDService.java`.
3. **Estandarización del Caso de Uso:**
   - `InventarioService` centraliza la implementación de `ConsultarInventarioUseCase` admitiendo filtros por `tiendaId` o `centroDistribucionId`.

## 3. Matriz de Cambios
| Componente | Capa | Acción |
| :--- | :--- | :--- |
| `InventarioService.java` | Aplicación | Unificación de consultas de tienda y CD con batching. |
| `ConsultarInventarioCDService.java` | Aplicación | Eliminado (código duplicado). |

## 4. Validación
- **Compilación:** `BUILD SUCCESS`.
- **Commit:** `525e726` (`refactor: unificar servicios de inventario de tienda y cd en InventarioService y eliminar archivo duplicado (Fix 17)`).
