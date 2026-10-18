# Lista de Tareas Pendientes (OptiWay)

Este documento resume los puntos pendientes y el estado de la deuda técnica en el backend y vistas web para completar la persistencia, visibilidad y operaciones de la cadena de suministro.

---

### 1. Refactorización Realizada Recientemente
- [x] **Unificación del Modelo de Inventario:**
  - Fusión de `InventarioCentroDistribucion` e `Inventario` en una sola entidad `Inventario` con regla de exclusividad de ubicación (`tiendaId` XOR `centroDistribucionId`).
  - Eliminación de `InventarioCentroDistribucionRepositoryPort` y su adaptador; integración completa en `InventarioRepositoryPort`.
  - Validación con tests unitarios en `InventarioExclusividadTest` e `IngresoMercanciaServiceTest`.
- [x] **Control de Roles en Ingreso de Mercancía (HU-18 / Fix 4):**
  - Implementada la validación en `IngresoMercanciaService` para permitir operaciones únicamente a administradores, planificadores y al encargado asignado del CD.
- [x] **Gestión de Productos en Catálogo (HU-04, HU-05, HU-06, Fix 6, Fix 9):**
  - Casos de uso `CrearProductoUseCase`, `ObtenerProductosUseCase`, `EditarProductoUseCase` y `CambiarEstadoProductoUseCase`.
  - Endpoints `POST /api/productos`, `GET /api/productos`, `PATCH /api/productos/{id}`, `PATCH /api/productos/{id}/estado` y `GET /api/productos/{id}/tiene-stock`.
  - Unificación hacia booleanos seguros (`Boolean activo`), desacoplando la confirmación interactiva al frontend.
- [x] **Consulta de Inventario de Centros de Distribución (HU-16 / Fix 8):**
  - Implementado `GET /api/inventario/centro-distribucion` con permisos multirrol (Admin, Planificador, Encargado) y optimización de consultas N+1 en memoria.
- [x] **Estandarización de Inyección de Identidad (Fix 7):**
  - Implementación de `@AuthUserId` con `AuthUserIdArgumentResolver` y `WebMvcConfig`, eliminando 15 duplicaciones de extracción de JWT en los controladores.
- [x] **Patrón Information Expert y Fachada de Autorización (Fix 15):**
  - Eliminado *Feature Envy* en servicios delegando la lógica de pertenencia y permisos a `Usuario` (`puedeOperarTienda`, `puedeConsultarTienda`, `puedeOperarCentroDistribucion`, etc.).
  - Centralización de comprobaciones de autorización en `AuthorizationService` (`requirePermisoTienda`, `requirePermisoCentroDistribucion`, `requirePermisoConsultarTienda`).
  - Filtrado de tiendas delegado a PostgreSQL (`tiendaRepositoryPort.obtenerPorEncargadoId`) en lugar de stream en memoria.
- [x] **Optimización N+1 y Limpieza en Servicios de Inventario (Fix 16):**
  - Incorporación del método batch `findAllById(Iterable<Long> ids)` en `ProductoRepositoryPort` y `ProductoRepositoryAdapter`.
  - Reemplazo de consultas individuales repetitivas (`findById` en bucle/stream) por una sola consulta SQL (`SELECT ... WHERE id IN (...)`).
  - Simplificación drástica de validaciones y dependencias en `IngresoMercanciaService`, `InventarioService` y `VentaService`.
- [x] **Unificación de Servicios de Inventario (Fix 17):**
  - Consolidación total de la lógica de inventario en `InventarioService`, implementando tanto `ConsultarInventarioUseCase` como `ConsultarInventarioCDUseCase`.
  - Eliminación definitiva del servicio duplicado `ConsultarInventarioCDService.java`.
  - Centralización del procesamiento de batching, filtrado y enriquecimiento (`enriquecerConProducto`) en un único flujo reutilizado.


---

### 2. Operación de Ventas en Tienda (HU-11 - Sprint 2)
- [x] **Registro de Ventas en Backend (HU-11):**
  - Implementado caso de uso `RegistrarVentaUseCase` y servicio `VentaService`.
  - Expuesto endpoint `POST /api/ventas` con validación de rol `ENCARGADO_TIENDA` asignado y `ADMINISTRADOR`.
  - Validación de existencia y estado activo de producto en catálogo y en inventario de tienda.
  - Validación y descuento transaccional automático de existencias en `Inventario`.
  - Registro de la transacción histórica de venta en PostgreSQL (tabla `ventas`).
  - Pruebas unitarias completas en `VentaServiceTest`.
- [ ] **Conexión de Interfaz:**
  - Conectar el formulario de `registrar_venta_tienda.html` con el endpoint `POST /api/ventas`.

---

### 3. Persistencia de Productos y Stock en Tiendas (HU-12 / Extensión)
- [x] **Crear, editar y activar/desactivar producto en BD (Backend - HU-04 / HU-05 / HU-06):**
  - Implementado catálogo maestro con endpoints `POST /api/productos`, `PATCH /api/productos/{id}`, `PATCH /api/productos/{id}/estado` y `GET /api/productos` en PostgreSQL.
- [ ] **Ajuste de stock en BD (Backend):**
  - Implementar caso de uso y endpoint `PATCH /api/inventario/{id}/stock` para sumar, restar o fijar existencias reales en BD.
- [ ] **Conectar vista estática (Frontend):**
  - En `inventario.html`, reemplazar los cambios temporales en memoria JavaScript (`itemsInventario.unshift`) por llamadas `fetch()` reales a los endpoints de creación y ajuste de stock.

---

### 4. Consulta y Visualización de Mercancía en Centros de Distribución (HU-16 / HU-18)
- [x] **Listar inventario de CD en Backend (HU-16):**
  - Método `findByCentroDistribucionId(Long cdId)` en `InventarioRepositoryPort` y `InventarioRepositoryAdapter`.
  - Caso de uso `ConsultarInventarioCDUseCase` y endpoint `GET /api/inventario/centro-distribucion` con cálculo dinámico de `stockBajo`.
- [ ] **Tabla de mercancía en Frontend:**
  - En `ingreso-mercancia.html`, agregar una tabla que cargue y muestre el stock acumulado y productos presentes en el CD seleccionado tras cada ingreso.

---

### 5. Validaciones y Mejoras de HU-18
- [ ] **Validación de entrada:** Agregar anotaciones `@NotNull`, `@Positive` (cantidad > 0) y `@NotBlank` (origen obligatorio) con `@Valid` en el controlador para responder `400 Bad Request`.
- [ ] **Alineación de endpoint:** Migrar de `POST /api/ingresos-mercancia` a `POST /api/centros-distribucion/{id}/ingresos` retornando `201 Created` con el detalle del movimiento.

---

### 6. Rendimiento y Optimización (Backend)
- [x] **Resolver N+1 en Inventario:** Implementada la carga batch de productos (`findAllById`) en `ProductoRepositoryPort` y adaptador JPA (`ProductoRepositoryAdapter`), eliminando las búsquedas repetitivas `findById` en bucle/stream tanto en `InventarioService` como en `ConsultarInventarioCDService`.
