# Fix 35 — Servicios de aplicación: Commands inmutables, authUserId primero y retornos DTO

> **Commit asociado:** este fix (commit `refactor(service): ... (Fix 35)`) de la serie de estandarización hexagonal.

## 1. Contexto y Diagnóstico del Problema

Con los puertos ya renovados (`fix28`, `fix29`, `fix33`), la capa `application/service` quedó desfasada respecto a sus propios contratos:

1. **Firmas obsoletas:** las implementaciones aún declaraban `authUserId` al final (`eliminarUsuario(Long, UUID)`, `asignarEncargado(Long, Long, UUID)`, `registrarIngreso(...)`) o lo recibían como simple argumento posicional en medio de listas de escalares.
2. **Retornos de dominio:** los servicios devolvían `Producto`, `Tienda`, `CentroDistribucion`, `Usuario`, `Venta`, `IngresoMercancia` e `Inventario`, trasladando a los controladores la tarea de mapear a DTOs.
3. **Autorización inconsistente:** convivían `requireAdministrador`, `requireAdministradorIfPresent` y operaciones de lectura sin verificación alguna (`obtenerUsuarioPorId`, `tieneStockActivo`, `obtenerProductos`).
4. **Lógica de mapeo en la capa equivocada:** los controladores ejecutaban `XResponse.from(...)` en lugar de recibir DTOs ya listos.

## 2. Solución Aplicada

### A. `authUserId` como primer parámetro en toda implementación
```java
public void eliminarUsuario(UUID authUserId, Long id)
public TiendaResponse asignarEncargado(UUID authUserId, Long tiendaId, Long encargadoId)
public CentroDistribucionResponse asignarEncargado(UUID authUserId, Long centroId, Long encargadoId)
public IngresoMercanciaResponse registrarIngreso(UUID authUserId, RegistrarIngresoCommand command)
public VentaResponse registrarVenta(UUID authUserId, RegistrarVentaCommand command)
```

### B. Implementación de los Commands de mutación
`crearProducto`, `editarProducto`, `cambiarEstadoProducto`, `crearUsuario`, `actualizarRol`, `crearTienda`, `crearCentroDistribucion`, `registrarIngreso`, `registrarVenta` — todos reciben `(UUID authUserId, XCommand command)`.

### C. Autorización estricta
- `requireAdministrador` para operaciones de escritura y listados administrativos (productos, usuarios, tiendas, CDs).
- `requireAuthenticated` para lecturas de cualquier usuario autenticado (`obtenerProductos`, `obtenerUsuarioPorId`, `tieneStockActivo`).

### D. Mapeo dominio → DTO dentro del servicio
- Todos los `obtener*` y mutaciones retornan `*Response` (`ProductoResponse`, `UsuarioResponse`, `TiendaResponse`, `CentroDistribucionResponse`, `IngresoMercanciaResponse`, `VentaResponse`).
- Helper `mapearRespuestas(...)` en `TiendaService` y `CentroDistribucionService` para listas filtradas por rol.

## 3. Archivos Intervenidos

- `application/service/ProductoService.java`
- `application/service/UsuarioService.java`
- `application/service/TiendaService.java`
- `application/service/CentroDistribucionService.java`
- `application/service/IngresoMercanciaService.java`
- `application/service/VentaService.java`

> `InventarioService.java` **no** viaja aquí: su adaptación al `ObtenerInventarioCommand` quedó en el grupo atómico `fix29`.

## 4. Beneficios Obtenidos

1. **Contrato uniforme en las 3 capas:** controller → puente → servicio, todos con `authUserId` primero.
2. **Controladores livianos:** reciben DTOs listos; solo delegan.
3. **Seguridad verificable:** toda ejecución de un caso de uso pasa por `AuthorizationService`.

## 5. Observaciones

- Este fix **no cambia comportamiento visible de la API**: solo reposiciona parámetros, encapsula retornos y refuerza autorizaciones que las rutas ya exigían a nivel de seguridad HTTP.
- El reorden de `authUserId` a primer lugar exige `git`-blame conjunto con `fix28` (puertos) para rastrear el cambio completo.
- **Suite de pruebas asociada:** los tests de servicios (`UsuarioServiceTest`, `VentaServiceTest`, `IngresoMercanciaServiceTest`, `AsignacionUbicacionTest`) fueron sincronizados en el commit `test(unit): sincronizar suite de pruebas...`.
- `InventarioService` y sus 8 llamadas con `ObtenerInventarioCommand` viajan en `fix29`.

## 6. Criterios de Aceptación

- [x] Ningún servicio declara `authUserId` en posición distinta a la primera
- [x] Cero retornos de `domain.model.*` desde los servicios hacia controladores
- [x] Todo caso de uso con endpoint aplica `requireAdministrador` o `requireAuthenticated`
- [x] `mvnw clean test` → BUILD SUCCESS, 38/38 pruebas
