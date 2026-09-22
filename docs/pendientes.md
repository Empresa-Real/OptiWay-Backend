# Lista de Tareas Pendientes (OptiWay)

Este documento resume de forma concisa los puntos pendientes identificados en el backend y en las vistas web para completar la persistencia y la visibilidad de inventarios.

---

### 1. Persistencia de Productos y Stock en Tiendas (HU-12 / Extensión)
- [ ] **Crear producto / inventario en BD (Backend):**
  - Implementar caso de uso y endpoint `POST /api/inventario` (o `POST /api/productos`) para guardar productos en PostgreSQL y vincularlos a la tienda con su stock inicial.
- [ ] **Ajuste de stock en BD (Backend):**
  - Implementar caso de uso y endpoint `PATCH /api/inventario/{id}/stock` para sumar, restar o fijar existencias reales en BD.
- [ ] **Conectar vista estática (Frontend):**
  - En `inventario.html`, reemplazar los cambios temporales en memoria JavaScript (`itemsInventario.unshift`) por llamadas `fetch()` reales a los endpoints de creación y ajuste de stock.

---

### 2. Consulta y Visualización de Mercancía en Centros de Distribución (HU-18 / Extensión)
- [ ] **Listar inventario de CD en Backend:**
  - Agregar método `listarPorCentroDistribucion(Long cdId)` en `InventarioCentroDistribucionRepositoryPort`.
  - Crear caso de uso `ConsultarInventarioCDUseCase`.
  - Exponer endpoint `GET /api/centros-distribucion/{id}/inventario`.
- [ ] **Tabla de mercancía en Frontend:**
  - En `ingreso-mercancia.html`, agregar una tabla que cargue y muestre el stock acumulado y productos presentes en el CD seleccionado tras cada ingreso.

---

### 3. Validaciones y Correcciones de HU-18 (Detalladas en Fix 4)
- [ ] **Validación de entrada:** Agregar anotaciones `@NotNull`, `@Positive` (cantidad > 0) y `@NotBlank` (origen obligatorio) con `@Valid` en el controlador para responder `400 Bad Request`.
- [ ] **Control de roles:** Validar en `IngresoMercanciaService` que solo usuarios con rol `ADMINISTRADOR` o `ENCARGADO_CD` puedan registrar ingresos.
- [ ] **Alineación de endpoint:** Migrar de `POST /api/ingresos-mercancia` a `POST /api/centros-distribucion/{id}/ingresos` retornando `201 Created` con el detalle del movimiento.

---

### 4. Rendimiento (Backend)
- [ ] **Resolver N+1 en Inventario:** Cambiar la búsqueda individual de productos (`findById` en bucle) en `InventarioService` por una consulta batch (`findAllById`) o proyección con `JOIN`.
