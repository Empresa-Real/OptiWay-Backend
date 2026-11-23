# Historias de Usuario — Sprint 3

Índice de las guías de implementación del sprint 3, con el mismo formato usado en [Sprint 1](../Sprint%201/) y [Sprint 2](../Sprint%202/).

| HU | Título | Estado | Endpoint(s) principal(es) | Guía |
| :--- | :--- | :--- | :--- | :--- |
| **HU-08** | Desactivar tienda | ✅ Implementado | `PATCH /api/tiendas/{id}/estado` · `GET /api/tiendas/{id}/ordenes-en-transito` | [Ver](HU-08_Desactivar_tienda.md) |
| **HU-10** | Desactivar centro de distribución | ✅ Implementado | `PATCH /api/centros-distribucion/{id}/estado` (alias `/api/cd/...`) | [Ver](HU-10_Desactivar_centro_de_distribucion.md) |
| **HU-13** | Ajustar inventario de tienda | ✅ Implementado | `POST /api/inventario/ajustes` | [Ver](HU-13_Ajustar_inventario_tienda.md) |
| **HU-15** | Solicitar reposición manual | ✅ Implementado | `POST /api/tiendas/{id}/solicitudes-reposicion` · `GET /api/solicitudes-reposicion` | [Ver](HU-15_Solicitar_reposicion_manual.md) |
| **HU-17** | Ajustar inventario de CD | ✅ Implementado | `PATCH /api/centros-distribucion/{id}/inventario` (alias `/api/cd/...`) | [Ver](HU-17_Ajustar_inventario_CD.md) |
| **HU-19** | Consultar patrones de demanda | ⚠️ Sin backend | — | [Ver](HU-19_Consultar_patrones_de_demanda.md) |
| **HU-20** | Crear recomendación de reposición | ✅ Implementado | `POST /api/recomendaciones-reposicion` · `GET /api/recomendaciones-reposicion` | [Ver](HU-20_Crear_recomendacion_reposicion.md) |
| **HU-21** | Aprobar, ajustar o rechazar recomendación | ✅ Implementado | `PATCH /api/recomendaciones-reposicion/{id}/aprobar` · `/rechazar` | [Ver](HU-21_Aprobar_ajustar_rechazar_recomendacion.md) |
| **HU-27** | Reporte de rotación de productos | ⚠️ Sin backend | — | [Ver](HU-27_Reporte_rotacion_productos.md) |

---

## Notas transversales

* **Fuente autoritativa de endpoints:** Swagger de producción [`https://optiway-backend.onrender.com/docs`](https://optiway-backend.onrender.com/docs) o el JSON [`https://optiway-backend.onrender.com/v3/api-docs`](https://optiway-backend.onrender.com/v3/api-docs). El catálogo resumido está en [`docs/Swagger_OpenAPI.md`](../../Swagger_OpenAPI.md).
* **Roles:**
  * Desactivar tienda (HU-08) y CD (HU-10): solo `ADMINISTRADOR`.
  * Ajuste de inventario de tienda (HU-13): `ENCARGADO_TIENDA` asignado, `ADMINISTRADOR` y `PLANIFICADOR`.
  * Ajuste de inventario de CD (HU-17): solo `ENCARGADO_CD` asignado.
  * Solicitar reposición (HU-15): solo `ENCARGADO_TIENDA`; listado: solo `PLANIFICADOR`.
  * Recomendaciones (HU-20/HU-21): solo `PLANIFICADOR` (ADMINISTRADOR excluido).
* **Convención de errores:** `{"error": "<mensaje>", "message": "<mensaje>"}`; `code` se omite cuando es `null`.
* **Migración del sprint:** [`docs/migrations/hu21_ordenes_y_auditoria_recomendaciones.sql`](../../migrations/hu21_ordenes_y_auditoria_recomendaciones.sql) es la única migración SQL nueva; el resto del esquema se genera con `spring.jpa.hibernate.ddl-auto=update`.
* **Fuera de este sprint:** órdenes en preparación/despacho (HU-22/23), seguimiento global (HU-25), inventario consolidado multi-ubicación (HU-26) y niveles generales (HU-28). Las guías de HU-19 y HU-27 documentan explícitamente su ausencia de backend.
