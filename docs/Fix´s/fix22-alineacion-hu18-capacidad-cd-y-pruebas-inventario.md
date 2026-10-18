# Fix 22: Alineación REST de HU-18, Control de Capacidad en CD y Pruebas Unitarias de Inventario

## 1. Contexto y Diagnóstico del Problema

Tras auditar las correcciones y pendientes históricos del Sprint 1 (documentados en `fix3-hu-12-18.md`, `fix4-hu-18.md` y `docs/pendientes.md`), se identificaron tres deudas técnicas pendientes:

1. **Desalineación del Endpoint REST de Ingreso de Mercancía (HU-18 / Fix 4 - Tarea 3):**
   - El endpoint original `POST /api/ingresos-mercancia` solicitaba el ID del centro de distribución dentro del cuerpo de la petición en lugar de anidarlo como un sub-recurso RESTful (`/api/centros-distribucion/{id}/ingresos` o `/api/cd/{id}/ingresos`).
   - El controlador retornaba `void` (HTTP 200/201 con cuerpo vacío), descartando la constancia del movimiento (identificador, fecha/hora, usuario responsable y balance de stock resultante), impidiendo que el frontend confirme la recepción.

2. **Omisión de la Capacidad Máxima del Centro de Distribución (HU-18):**
   - La entidad `CentroDistribucion` cuenta con el atributo `capacidad` (`Integer`), pero `IngresoMercanciaService` no validaba si el volumen acumulado de stock excedía el límite físico del almacén antes de persistir los cambios.

3. **Ausencia de Pruebas Unitarias en `InventarioService` y Brechas en `IngresoMercanciaServiceTest` (HU-12):**
   - `InventarioService` (HU-12) no contaba con ninguna clase de pruebas unitarias dedicada.
   - `IngresoMercanciaServiceTest` solo cubría 3 validaciones básicas, sin evaluar la creación de inventario nuevo, la capacidad máxima del CD ni los escenarios donde el CD o el producto no existen.

---

## 2. Solución Aplicada

### A. Endpoint Anidado REST y DTO de Constancia (`IngresoMercanciaResponse`)
1. **Nuevo DTO de Entrada:** Se creó `RegistrarIngresoRequest.java` con Bean Validation (`@NotNull`, `@Positive`, `@NotBlank`), permitiendo registrar mercancía enviando únicamente producto, cantidad y origen (el ID del CD proviene de la URL).
2. **DTO de Constancia:** Se creó el record `IngresoMercanciaResponse.java` que retorna:
   - `id`: Identificador histórico generado.
   - `centroDistribucionId`: Centro receptor.
   - `productoId`: Producto ingresado.
   - `cantidad`: Unidades recibidas.
   - `origen`: Proveedor o CD emisor.
   - `usuarioId`: Usuario que ejecutó la operación.
   - `fechaIngreso`: Timestamp registrado.
   - `stockTotalActualizado`: Balance resultante del producto en el CD.
3. **Controlador de CD y Alias:** En `CentroDistribucionController`, se habilitaron las rutas:
   - `POST /api/centros-distribucion/{id}/ingresos`
   - `POST /api/cd/{id}/ingresos` (alias corto)
4. **Retrocompatibilidad:** En `IngresoMercanciaController` (`POST /api/ingresos-mercancia`), se actualizó el retorno para responder también con `IngresoMercanciaResponse` en lugar de `void`.

### B. Control de Capacidad Máxima en `IngresoMercanciaService`
Antes de persistir el stock, se totaliza el inventario actual del CD mediante `inventarioRepositoryPort.findByCentroDistribucionId(centro.getId())`. Si `stockTotalActual + nuevaCantidad > centro.getCapacidad()`, se rechaza con `IllegalArgumentException` informando la capacidad máxima y el espacio disponible restante.

### C. Suite de Pruebas Unitarias Exhaustiva
1. **`InventarioServiceTest.java` (8 pruebas nuevas):**
   - Consulta enriquecida por tienda con cálculo dinámico de `stockBajo`.
   - Filtrado combinado por nombre parcial y categoría.
   - Consulta directa por `productoId`.
   - Control de excepciones para tiendas inexistentes.
   - Validación de seguridad (`AccesoDenegadoException`) para encargados no asignados.
   - Consulta enriquecida por Centro de Distribución.
   - Control de excepciones para CDs inexistentes.
   - Manejo de inventarios vacíos sin sobrecarga de consultas SQL.
2. **`IngresoMercanciaServiceTest.java` (Ampliación a 7 pruebas):**
   - Incremento sobre inventario preexistente con retorno de `stockTotalActualizado`.
   - Inicialización atómica de nuevo inventario.
   - Rechazo por exceso de capacidad física del CD.
   - Excepciones por CD o producto inexistente.
   - Rechazo de cantidades <= 0.

---

## 3. Matriz de Cambios

| Componente | Capa Hexagonal | Tipo de Cambio |
| :--- | :--- | :--- |
| `IngresoMercancia.java` | Dominio | Se agregó el campo `stockTotalActualizado` y sus métodos de acceso. |
| `RegistrarIngresoMercanciaUseCase.java` | Puerto de Entrada | Cambio de firma de `void` a `IngresoMercancia`. |
| `RegistrarIngresoRequest.java` | Adaptador REST (DTO) | DTO de entrada para endpoints anidados con validación declarativa. |
| `IngresoMercanciaResponse.java` | Adaptador REST (DTO) | Record DTO para la constancia del movimiento con HTTP 201 Created. |
| `CentroDistribucionController.java` | Adaptador REST | Mapeo de `POST /api/centros-distribucion/{id}/ingresos` y `/api/cd/{id}/ingresos`. |
| `IngresoMercanciaController.java` | Adaptador REST | Retorno de `IngresoMercanciaResponse` en lugar de `void`. |
| `IngresoMercanciaService.java` | Servicio de Aplicación | Validación de capacidad máxima de CD y retorno del registro persistido. |
| `InventarioServiceTest.java` | Pruebas Unitarias | Creación de suite completa con 8 casos de prueba. |
| `IngresoMercanciaServiceTest.java` | Pruebas Unitarias | Ampliación a 7 casos de prueba incluyendo aforo de CD e inicialización. |
| `docs/pendientes.md` | Documentación | Actualización de la Sección 5 reflejando el cierre de la deuda del Sprint 1. |

---

## 4. Estado de Validación

* **Compilación y Tests:** `./mvnw test '-Dtest=!OptiwayApplicationTests'` $\rightarrow$ **BUILD SUCCESS (34/34 tests pasados)**.
* **Grafo de Conocimiento:** Actualizado con `python -m graphify update .` (1312 nodos, 2724 relaciones).
