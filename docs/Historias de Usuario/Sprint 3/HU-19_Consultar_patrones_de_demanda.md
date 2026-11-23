# HU-19 — Consultar patrones de demanda por tienda y producto

**Épica 5:** Análisis y reportes  
**Feature:** Análisis de demanda y rotación  
**Sprint:** 3  
**Estado:** ⚠️ **Pendiente de implementación en backend** (planeada para este sprint, sin merge)

---

## Historia de Usuario

> **Como** Analista/Planificador de cadena de suministro  
> **Quiero** ver el histórico de ventas de cada producto por tienda  
> **Para** identificar qué se vende más y en qué temporadas.

Fuente: `product_backlog.txt:391-401`.

---

## Criterios de Aceptación (backlog)

1. Puedo consultar la evolución de ventas de un producto en una tienda específica dentro de un rango de fechas.
2. Puedo comparar la demanda de un mismo producto entre varias tiendas seleccionadas.
3. El sistema resalta visualmente cuáles tiendas tienen mayor y cuál menor rotación de ese producto.

---

## Estado de Implementación

**No hay backend para esta historia.** Verificación realizada contra el código de `main`/`produccion` y contra el OpenAPI en vivo (`https://optiway-backend.onrender.com/v3/api-docs`):

* No existe controlador, caso de uso, servicio, repositorio ni entidad relacionada con patrones de demanda.
* No existe ningún endpoint de consulta de histórico de ventas ni de agregación por producto/tienda/fecha.
* No hay migración SQL ni campos calculados para rotación o estacionalidad.
* No hay tests asociados.

**Lo que sí existe y puede servir de base (pero no cumple la HU):**

| Recurso | Qué hace | Por qué no cubre HU-19 |
| :--- | :--- | :--- |
| `POST /api/tiendas/{id}/ventas` (HU-11) | Registra una venta y descuenta stock. | Solo escritura; no expone histórico consultable. |
| `GET /api/inventario` (HU-12) y `GET /api/inventario/centro-distribucion` (HU-16) | Consultan stock actual con filtros. | No incluyen ventas, rangos de fechas ni comparación entre tiendas. |
| `GET /api/solicitudes-reposicion` (HU-15) | Lista solicitudes pendientes. | No es demanda real ni serie temporal. |

---

## Contrato previsto (NO implementado — no usar desde el frontend todavía)

No existe contrato oficial. Cuando se implemente, deberá acordarse con el equipo de backend antes de programar consumos. Criterios que deberá satisfacer:

* Parámetros mínimos: `productoId`, `tiendaId` (o lista de tiendas para comparación), `fechaDesde`, `fechaHasta`.
* Respuesta con unidades vendidas por periodo y por tienda, e indicadores de mayor/menor rotación para el resaltado visual.
* Roles: Analista/Planificador (`PLANIFICADOR`), coherente con HU-20/HU-21.

> ⚠️ Cualquier pantalla que consuma estos datos debe mostrar un estado "sin endpoint disponible" hasta que el backend publique el contrato en `/docs`.

---

## Componentes Hexagonales Asociados

Ninguno en la implementación actual.

---

## Pruebas y Migraciones

No aplican. Esta guía se crea para dejar trazabilidad del hueco funcional y evitar que el frontend interprete la ausencia de endpoint como un error de integración.
