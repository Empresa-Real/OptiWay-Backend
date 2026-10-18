# Fix 10 — Integración y Armonización de HU-11 (Registro de Ventas en Tienda)

**Fecha:** 18 de Octubre de 2026  
**Historia Asociada:** HU-11 (Registrar Venta)  
**Autor:** Antigravity  

---

## 1. Contexto y Diagnóstico

La rama remota `origin/OptiWay-Backend-feature-HU-11-registrar-venta` fue creada como un repositorio huérfano desconectado de `main` (`no merge base`), conteniendo un proyecto completo anidado dentro de la carpeta `OptiWay-Backend-feature-HU-07-Crear-Tienda/`. Además, el código hacía referencia a métodos desactualizados de inventario y dependencias ausentes en Maven (`jakarta.validation`).

Para evitar contaminar el historial de Git y garantizar la integridad arquitectónica en `main`, se rescataron los componentes de dominio y puertos de la funcionalidad, adaptándolos a la arquitectura hexagonal moderna del proyecto.

---

## 2. Cambios Implementados

### Dominio y Puertos
* **`Venta`:** Modelo de dominio para encapsular los atributos de la venta (`id`, `tiendaId`, `productoId`, `cantidad`, `fechaHora`) con validación de cantidad positiva (`> 0`).
* **`RegistrarVentaCommand`:** Record inmutable para transportar los parámetros requeridos.
* **`RegistrarVentaUseCase`:** Puerto de entrada primario con soporte para identidad de usuario autenticado (`authUserId`).
* **`VentaRepositoryPort`:** Puerto de salida secundario para persistencia y consulta.

### Servicio de Aplicación (`VentaService`)
* Integración con el modelo unificado de `Inventario` mediante `InventarioRepositoryPort.findByTiendaIdAndProductoId`.
* Validación de existencia de la tienda (`404 Not Found` vía `RecursoNoEncontradoException`).
* Control estricto de roles: verificación de que el usuario autenticado sea `ADMINISTRADOR` o el `ENCARGADO_TIENDA` asignado a esa sucursal (`403 Forbidden` vía `AccesoDenegadoException`).
* Verificación de que el producto exista en catálogo y tenga estado activo (`activo = true`).
* Verificación y descuento transaccional de existencias en el inventario de la tienda (`StockInsuficienteException` -> `400 Bad Request`).
* Persistencia histórica de la venta en base de datos.

### Adaptadores REST y Persistencia
* **`VentaController`:** Expone `POST /api/ventas` inyectando `@AuthUserId UUID authUserId`.
* **`RegistrarVentaRequest`:** DTO limpio sin acoplamiento a librerías externas.
* **`VentaJpaEntity` y `VentaJpaRepository`:** Mapeo ORM a la tabla PostgreSQL `ventas`.
* **`VentaRepositoryAdapter`:** Adaptador secundario desacoplado.
* **`ApiExceptionHandler`:** Centralización de respuestas para `StockInsuficienteException` (`400 Bad Request`) y `RecursoNoEncontradoException` (`404 Not Found`).

---

## 3. Pruebas Unitarias

Se implementó la suite completa de pruebas en [VentaServiceTest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/VentaServiceTest.java):
* `registrarVenta_exitoso_descuentaStockYGuardaVenta` (Verifica descuento exacto y guardado).
* `registrarVenta_stockInsuficiente_lanzaExcepcion`.
* `registrarVenta_productoInactivo_lanzaExcepcion`.
* `registrarVenta_permisosInsuficientes_lanzaAccesoDenegado`.

Resultado: **4/4 Tests aprobados exitosamente.**
