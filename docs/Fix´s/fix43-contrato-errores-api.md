# Fix 43 — Contrato de errores por endpoint

Se documentaron las respuestas HTTP de error en los endpoints REST protegidos mediante anotaciones OpenAPI reutilizables y específicas. Las respuestas con cuerpo declaran el esquema real `ErrorResponse`; el detalle de códigos por ruta está en [Contrato de errores de la API](../Contrato-Errores-API.md). También se corrigió la documentación de DTOs para reflejar que Spring Security devuelve JSON para `401` y `403`.

El contrato distingue validación (`400`), autenticación (`401`), autorización (`403`), recurso inexistente (`404`), conflicto de negocio (`409`), Content-Type no compatible (`415`), error inesperado (`500`) y fallo del proveedor de autenticación (`502`). También documenta el `404` sin cuerpo de `GraphController`, que permanece oculto en Swagger.

Los manejadores configurados de Spring Security sí devuelven `ErrorResponse` para `401` y `403`; la respuesta sin cuerpo sólo aplica al grafo cuando su archivo no existe.
