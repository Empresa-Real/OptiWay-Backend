# HU-27 — Reporte de rotación de productos por tienda

**Épica 5:** Análisis y reportes  
**Feature:** Análisis de demanda y rotación  
**Sprint:** 3  
**Estado:** ⚠️ **Pendiente de implementación en backend** (planeada para este sprint, sin merge)

---

## Historia de Usuario

> **Como** Analista/Planificador de cadena de suministro  
> **Quiero** un reporte de rotación de productos por tienda, con métricas como unidades vendidas y tiempo promedio en inventario  
> **Para** identificar cuáles se venden rápido y cuáles casi no rotan.

Fuente: `product_backlog.txt:403-411`.

---

## Criterios de Aceptación (backlog)

1. Al generar el reporte para un rango de fechas, el sistema muestra por tienda qué productos rotan rápido y cuáles casi no se venden, con sus métricas.
2. Puedo exportar el reporte generado a un archivo descargable.

---

## Estado de Implementación

**No hay backend para esta historia.** Verificación realizada contra el código de `main`/`produccion` y contra el OpenAPI en vivo (`https://optiway-backend.onrender.com/v3/api-docs`):

* No existe controlador, caso de uso, servicio, repositorio ni entidad de reportes de rotación.
* No existen métricas calculadas de rotación ni de tiempo promedio en inventario.
* No existe endpoint de exportación (CSV/XLSX/PDF) en ningún controlador.
* No hay tests ni migraciones asociadas.

**Relación con HU-19:** comparten dominio (ventas + inventario), pero HU-19 es consulta de series de demanda y HU-27 es un reporte agregado con exportación. Ninguna de las dos está implementada; conviene resolverlas juntas para reutilizar la consulta de ventas por rango de fechas.

**Lo que sí existe y puede servir de base (pero no cumple la HU):**

| Recurso | Qué hace | Por qué no cubre HU-27 |
| :--- | :--- | :--- |
| `POST /api/tiendas/{id}/ventas` (HU-11) | Registra ventas. | No expone agregados ni exportación. |
| `GET /api/inventario` (HU-12) | Consulta inventario de tienda con stock bajo. | No calcula rotación ni tiempo promedio en inventario. |
| `GET /api/productos` | Lista catálogo. | No contiene métricas por tienda. |

---

## Contrato previsto (NO implementado — no usar desde el frontend todavía)

No existe contrato oficial. Requisitos mínimos derivados del backlog:

* Parámetros: rango de fechas y, opcionalmente, tienda y categoría.
* Respuesta por tienda y producto: unidades vendidas, tiempo promedio en inventario y clasificación de rotación (rápida / lenta).
* Exportación descargable del reporte.
* Roles: Analista/Planificador (`PLANIFICADOR`).

> ⚠️ Cualquier pantalla que consuma estos datos debe mostrar un estado "sin endpoint disponible" hasta que el backend publique el contrato en `/docs`.

---

## Componentes Hexagonales Asociados

Ninguno en la implementación actual.

---

## Pruebas y Migraciones

No aplican. Esta guía deja constancia del hueco funcional dentro del sprint para que no se confunda con un fallo de integración del frontend.
