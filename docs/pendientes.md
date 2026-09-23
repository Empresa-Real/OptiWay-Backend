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

---

### 2. Operación de Ventas en Tienda (HU-11 - Prioridad Alta Sprint 1/2)
- [ ] **Registro de Ventas en Backend:**
  - Implementar caso de uso `RegistrarVentaUseCase` y servicio `VentaService`.
  - Exponer endpoint `POST /api/ventas` (o `POST /api/tiendas/{id}/ventas`).
  - Validar disponibilidad de existencias en `Inventario` y descontar la cantidad vendida de forma transaccional.
  - Registrar la transacción histórica de venta (producto, tienda, cantidad, fecha y usuario).
- [ ] **Conexión de Interfaz:**
  - Conectar el formulario de `registrar_venta_tienda.html` con el endpoint de ventas.

---

### 3. Persistencia de Productos y Stock en Tiendas (HU-12 / Extensión)
- [ ] **Crear producto / inventario en BD (Backend):**
  - Implementar caso de uso y endpoint `POST /api/inventario` (o `POST /api/productos`) para guardar productos en PostgreSQL y vincularlos a la tienda con su stock inicial.
- [ ] **Ajuste de stock en BD (Backend):**
  - Implementar caso de uso y endpoint `PATCH /api/inventario/{id}/stock` para sumar, restar o fijar existencias reales en BD.
- [ ] **Conectar vista estática (Frontend):**
  - En `inventario.html`, reemplazar los cambios temporales en memoria JavaScript (`itemsInventario.unshift`) por llamadas `fetch()` reales a los endpoints de creación y ajuste de stock.

---

### 4. Consulta y Visualización de Mercancía en Centros de Distribución (HU-16 / HU-18)
- [ ] **Listar inventario de CD en Backend:**
  - Agregar método `findByCentroDistribucionId(Long cdId)` en `InventarioRepositoryPort` y `InventarioRepositoryAdapter`.
  - Crear caso de uso `ConsultarInventarioCDUseCase`.
  - Exponer endpoint `GET /api/centros-distribucion/{id}/inventario` con cálculo de stock bajo.
- [ ] **Tabla de mercancía en Frontend:**
  - En `ingreso-mercancia.html`, agregar una tabla que cargue y muestre el stock acumulado y productos presentes en el CD seleccionado tras cada ingreso.

---

### 5. Validaciones y Mejoras de HU-18
- [ ] **Validación de entrada:** Agregar anotaciones `@NotNull`, `@Positive` (cantidad > 0) y `@NotBlank` (origen obligatorio) con `@Valid` en el controlador para responder `400 Bad Request`.
- [ ] **Alineación de endpoint:** Migrar de `POST /api/ingresos-mercancia` a `POST /api/centros-distribucion/{id}/ingresos` retornando `201 Created` con el detalle del movimiento.

---

### 6. Rendimiento y Optimización (Backend)
- [ ] **Resolver N+1 en Inventario:** Cambiar la búsqueda individual de productos (`findById` en bucle) en `InventarioService` por una consulta batch (`findAllById`) o proyección con `JOIN` en JPA.
