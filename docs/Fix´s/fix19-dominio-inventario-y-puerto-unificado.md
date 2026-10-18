# Fix 19: Dominio de Inventario Rico, Encapsulamiento y Puerto Unificado

## 1. Contexto y Diagnóstico
1. **Anemia en el Modelo de Dominio (`Inventario`):**
   - La manipulación de existencias (`cantidadActual + cantidad` o `cantidadActual - cantidad`) se realizaba directamente en los servicios (`VentaService`, `IngresoMercanciaService`), generando duplicación de cálculos y riesgo de inconsistencias de stock negativo.
2. **Encapsulamiento Roto en `Usuario`:**
   - La comprobación de roles seguía realizándose con `usuario.getRol() == Rol.ADMINISTRADOR` o `equals()` dispersos por el código, en lugar de consultar métodos semánticos en `Usuario` (principio *Tell, Don't Ask*).
3. **Inconsistencia de Puertos:**
   - Quedaban referencias ambiguas a `ConsultarInventarioCDUseCase` cuando toda la lógica ya residía en `ConsultarInventarioUseCase`.

## 2. Solución Aplicada
1. **Lógica de Negocio en la Entidad `Inventario`:**
   - `agregarStock(int cantidad)`: Incrementa existencias asegurando cantidad positiva.
   - `descontarStock(int cantidad)`: Descuenta existencias validando que no quede en negativo.
   - `tieneStockDisponible(int requerida)`: Consulta disponibilidad sin exponer mutabilidad.
2. **Encapsulamiento Semántico en `Usuario`:**
   - Se añadieron métodos en el dominio `Usuario`: `esAdministrador()`, `esPlanificador()`, `esEncargadoTienda()`, `esEncargadoCD()`.
3. **Unificación de Casos de Uso:**
   - Se removieron los puertos y adaptadores residuales de inventario de CD, canalizando todo a través de `ConsultarInventarioUseCase`.

## 3. Matriz de Cambios
| Componente | Capa | Acción |
| :--- | :--- | :--- |
| `Inventario.java` | Dominio | Métodos de negocio `agregarStock`, `descontarStock`, `tieneStockDisponible`. |
| `Usuario.java` | Dominio | Métodos semánticos de roles `esAdministrador`, `esPlanificador`, etc. |
| `ConsultarInventarioUseCase.java` | Puerto de Entrada | Interfaz unificada de consulta de inventario. |

## 4. Validación
- **Compilación:** `BUILD SUCCESS`.
- **Commit:** `52fe07c` (`refactor: corregir inconsistencias de puertos, dominio de inventario y casos de uso de CD (Fix 19)`).
