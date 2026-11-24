# Contrato de errores de la API

El contrato OpenAPI de cada operación protegida declara `401`, `403` y `500`. Las operaciones con parámetros o body declaran además `400`; las que aceptan body JSON incluyen `415`. Cada respuesta con cuerpo usa el esquema [`ErrorResponse`](../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/ErrorResponse.java), con los campos `error`, `message` y `code` (opcional). El estado HTTP es la fuente de verdad; `code` sólo se informa cuando un proveedor externo entrega un código propio.

Las anotaciones reutilizables están en [`rest/docs`](../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/docs/). Los controladores declaran además los errores particulares de cada operación:

| Operación | Errores adicionales documentados |
|---|---|
| `POST /api/centros-distribucion` | `415` Content-Type no soportado |
| `GET /api/centros-distribucion` | — |
| `PATCH /api/centros-distribucion/{id}/estado` | `404` centro inexistente, `409` tiene tiendas asignadas y falta confirmación, `415` |
| `PUT /api/centros-distribucion/{id}/encargado` | `404`, `415` |
| `POST /api/centros-distribucion/{id}/ingresos` | `404`, `415` |
| `PATCH /api/centros-distribucion/{id}/inventario` | `404`, `415` |
| `POST /api/tiendas` | `415` |
| `GET /api/tiendas` | — |
| `PUT /api/tiendas/{id}/encargado` | `404`, `409` tienda inactiva, `415` |
| `POST /api/tiendas/{id}/ventas` | `404`, `415` |
| `PATCH /api/tiendas/{id}/estado` | `404`, `409` órdenes en tránsito/enviadas sin confirmación, `415` |
| `GET /api/tiendas/{id}/ordenes-en-transito` | `400` ID inválido |
| `GET /api/inventario?tiendaId=...` | `400` parámetro inválido, `404` tienda inexistente |
| `GET /api/inventario/centro-distribucion?centroDistribucionId=...` | `400` parámetro inválido, `404` centro inexistente |
| `POST /api/inventario/ajustes` | `404`, `415` |
| `POST /api/productos` | `409` producto duplicado, `415` |
| `GET /api/productos` | — |
| `PATCH /api/productos/{id}` | `404`, `415` |
| `PATCH /api/productos/{id}/estado` | `404`, `409` existencias activas sin confirmación, `415` |
| `GET /api/productos/{id}/tiene-stock` | `400` ID inválido |
| `POST /api/usuarios` | `409` usuario duplicado, `502` fallo del proveedor de autenticación, `415` |
| `GET /api/usuarios` | — |
| `GET /api/usuarios/{id}` | `400` ID inválido, `404` usuario inexistente |
| `GET /api/usuarios/me` | `404` perfil inexistente |
| `DELETE /api/usuarios/{id}` | `404` usuario inexistente |
| `PATCH /api/usuarios/{id}/rol` | `404`, `415` |
| `POST /api/tiendas/{id}/solicitudes-reposicion` | `404`, `409` tienda inactiva, `415` |
| `GET /api/solicitudes-reposicion` | — |
| `POST /api/recomendaciones-reposicion` | `404`, `409` cobertura sin confirmar, `415` |
| `GET /api/recomendaciones-reposicion` | — |
| `PATCH /api/recomendaciones-reposicion/{id}/aprobar` | `404`, `409` recomendación ya resuelta/reglas de stock, `415` |
| `PATCH /api/recomendaciones-reposicion/{id}/rechazar` | `404`, `409` recomendación ya resuelta, `415` |

`400` cubre validación de body, parámetros obligatorios o conversiones de tipo. `401` y `403` también devuelven `ErrorResponse`: los manejadores configurados en Spring Security escriben el JSON explícitamente. `415` sólo aplica a operaciones que reciben un body JSON. `409` y `404` usan el manejador central y conservan el mismo esquema.

## Respuestas fuera del contrato por operación

- Un método HTTP no admitido puede responder `405` con `ErrorResponse`; la ruta no corresponde a una operación válida y por eso no se repite en cada operación OpenAPI.
- `GET /graph`, `/graph/` y `/graph.html` no aparecen en Swagger porque `GraphController` está marcado `@Hidden`. Si el archivo del grafo no está disponible, responde `404` **sin cuerpo**. El controlador declara esa respuesta sin asociarle `ErrorResponse`.
- Rutas inexistentes se manejan con `404` y `ErrorResponse` cuando Spring las entrega al manejador REST. El `404` del grafo es la excepción sin cuerpo descrita arriba.
