# Fix 28 — Desacoplamiento Hexagonal con Commands Inmutables y Blindaje de UseCases de Mutación

## 1. Contexto y Diagnóstico del Problema

En la auditoría técnica de la capa de puertos de entrada (`application.port.in`), se identificaron varias violaciones de diseño arquitectónico y de seguridad:

1. **Fuga de Dominio hacia los Casos de Uso:**
   Algunos casos de uso recibían entidades de dominio completas (o múltiples argumentos posicionales primitivos dispersos), obligando a las capas externas (como adaptadores REST) a instanciar entidades de dominio antes de llamar a la lógica de negocio.
2. **Sobrecargas Obsoletas y Bypass de Auditoría (`authUserId`):**
   Varios casos de uso mantenían métodos sobrecargados heredados sin el parámetro `UUID authUserId`, los cuales delegaban internamente pasando `null`:
   ```java
   // Patrón inseguro detectado:
   CentroDistribucion crearCentroDistribucion(CrearCentroDistribucionCommand command); // Llama pasando authUserId = null
   ```
   Esto representaba una vulnerabilidad de auditoría, permitiendo invocar mutaciones de estado eludiendo las comprobaciones de roles del sistema.
3. **Ausencia de Validación Declarativa en Capa de Aplicación:**
   Los parámetros de entrada carecían de Bean Validation a nivel de contrato de aplicación, delegando las validaciones únicamente a la capa web o de base de datos.

---

## 2. Solución Aplicada

### A. Introducción de Commands Inmutables con Bean Validation
Se implementaron Java Records canónicos para encapsular de forma inmutable todas las operaciones de mutación del sistema, con anotaciones de `jakarta.validation`:

- `CrearUsuarioCommand`: Validación de `email`, `nombre` y `rol`.
- `CrearTiendaCommand`: Validación de `nombre`, `direccion` y `ciudad`.
- `CrearCentroDistribucionCommand`: Validación de `nombre`, `direccion`, `capacidad` y `tiendasAbastecidasIds`.
- `ActualizarRolCommand`: Validación de `usuarioId` y `nuevoRol`.
- `CambiarEstadoProductoCommand`: Validación de `productoId` y `activo`.

### B. Normalización y Blindaje de `authUserId`
Se estandarizó la firma de todos los UseCases de mutación colocando `UUID authUserId` como **primer parámetro obligatorio**, eliminando todas las sobrecargas que permitían llamadas con `null`:

```java
// Contratos limpios y estandarizados:
Usuario crearUsuario(UUID authUserId, CrearUsuarioCommand command);
Tienda crearTienda(UUID authUserId, CrearTiendaCommand command);
CentroDistribucion crearCentroDistribucion(UUID authUserId, CrearCentroDistribucionCommand command);
Usuario actualizarRol(UUID authUserId, ActualizarRolCommand command);
Producto cambiarEstadoProducto(UUID authUserId, CambiarEstadoProductoCommand command);
void eliminarUsuario(UUID authUserId, Long id);
```

---

## 3. Archivos Intervenidos

### Nuevos Commands (`application.port.in`):
- `ActualizarRolCommand.java`
- `CambiarEstadoProductoCommand.java`
- `CrearCentroDistribucionCommand.java`
- `CrearTiendaCommand.java`
- `CrearUsuarioCommand.java`

### UseCases Refactorizados (`application.port.in`):
- `ActualizarRolUsuarioUseCase.java`
- `AsignarEncargadoCDUseCase.java`
- `AsignarEncargadoTiendaUseCase.java`
- `CambiarEstadoProductoUseCase.java`
- `CrearCentroDistribucionUseCase.java`
- `CrearProductoUseCase.java`
- `CrearTiendaUseCase.java`
- `CrearUsuarioUseCase.java`
- `EditarProductoUseCase.java`
- `EliminarUsuarioUseCase.java`
- `RegistrarIngresoMercanciaUseCase.java`
- `RegistrarVentaUseCase.java`
- `VerificarStockProductoUseCase.java`

---

## 4. Beneficios Obtenidos

1. **Inmutabilidad y Thread-Safety:** Los datos de entrada no pueden ser alterados durante el flujo del caso de uso.
2. **Auditoría Garantizada:** Ninguna mutación puede ejecutarse sin un `authUserId` verificado contra Supabase Auth.
3. **Desacoplamiento Total:** Los adaptadores de entrada solo conocen el Command y no las reglas internas de instanciación del Dominio.

---

## 5. Observaciones

- **Alcance por capas (split atómico):** este fix cubre exclusivamente los contratos de entrada (`application.port.in`). La adaptación de las implementaciones (`application.service`) y de los adaptadores REST se documenta por separado en `fix35` (servicios) y `fix37` (controladores).
- **Naming canónico de ingresos:** se conserva `RegistrarIngresoCommand` (y no `RegistrarIngresoMercanciaCommand`) por coherencia con la serie de fixes 18/20, pese a que el caso de uso se denomina `RegistrarIngresoMercanciaUseCase`.
- **`VerificarStockProductoUseCase`:** su firma pasó a `tieneStockActivo(UUID authUserId, Long productoId)`; el refuerzo de `requireAuthenticated` en la implementación llega en `fix35`.
- **Propagación del orden de parámetros:** en este fix `authUserId` se colocó como primer parámetro de los UseCases de mutación; el mismo criterio se propagó después a los controladores REST (`fix37`).
- **Verificación:** la suite se valida en estado integrado al cierre de la serie → `mvnw clean test -o`: BUILD SUCCESS, 38/38 pruebas, 0 fallos.
