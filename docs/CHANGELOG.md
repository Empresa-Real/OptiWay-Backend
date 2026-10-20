# Changelog — Cambios relevantes de OptiWay

> Resumen **por tema** de lo más relevante del proyecto. No es un diario de commits: cada sección agrupa la historia de una funcionalidad o refactorización completa. El detalle paso a paso de cada corrección vive en [`docs/Fix´s/`](./Fix´s/) y el estado de deuda abierta en [`deudaTecnica.md`](./deudaTecnica.md).

---

## Estandarización hexagonal de la API (Fix 28–40)

- **Commands inmutables en casos de uso** (Fix 28, 35): firmas de mutación con `authUserId` obligatorio como primer parámetro en toda capa; comandos como records inmutables.
- **Nomenclatura unificada de consultas** (Fix 29): lecturas bajo el prefijo `Obtener`; filtros de inventario encapsulados en `ObtenerInventarioCommand`.
- **DTOs de respuesta centralizados** (Fix 30–33): los servicios devuelven `*Response` en lugar de entidades de dominio; errores 4xx/5xx estandarizados en español vía `ApiExceptionHandler`.
- **Request DTOs 100% records** (Fix 36, 39): los 11 DTOs de entrada son records inmutables con `toCommand()` y Bean Validation; eliminados los últimos POJOs mutables (`CrearTiendaRequest`, `CrearCentroDistribucionRequest`, `CrearUsuarioRequest`).
- **Controladores con delegación pura** (Fix 37): `@AuthUserId` inyectado automáticamente desde el JWT; los controladores solo delegan en casos de uso.
- **Endpoints de stock anidados a su recurso padre** (Fix 38, 40):
  - `POST /api/centros-distribucion/{id}/ingresos` (alias `POST /api/cd/{id}/ingresos`) sustituye a la ruta plana `POST /api/ingresos-mercancia`; eliminados `IngresoMercanciaController` y el DTO muerto `RegistrarIngresoRequest`.
  - `POST /api/tiendas/{id}/ventas` sustituye a `POST /api/ventas`; eliminado el controlador-un-método `VentaController`.
  - El id del padre viaja en la URL, no en el body.
- **Graph fuera de Swagger**: `GraphController` marcado con `@Hidden`; `/graph` sigue público pero ya no aparece en Swagger UI ni en `/v3/api-docs`.

## Registro de ventas en tienda (HU-11)

- `POST /api/tiendas/{id}/ventas`: descuento automático y transaccional de stock sobre el modelo unificado `Inventario`.
- Validaciones de negocio: existencia de tienda, producto activo en catálogo, existencia en inventario y suficiencia de stock (`StockInsuficienteException` → `400 Bad Request`).
- Control de acceso: reservado a `ADMINISTRADOR` o al `ENCARGADO_TIENDA` asignado a esa tienda.
- Persistencia (`VentaJpaEntity`, `VentaRepositoryAdapter`), suite `VentaServiceTest` y guía `docs/guias/guia-trabajo-git-ramas-y-forks.md`.

## Catálogo de productos (HU-04, HU-05, HU-06)

- `POST /api/productos` y `GET /api/productos`: catálogo maestro con identificador único en mayúsculas, categoría, precio no negativo y estado inicial activo.
- `PATCH /api/productos/{id}`: edición parcial con auditoría del editor y la fecha (`producto_ediciones`).
- `PATCH /api/productos/{id}/estado` y `GET /api/productos/{id}/tiene-stock`: desactivar/reactivar productos con verificación de existencias para el frontend.

## Control de acceso por rol y ubicación (HU-29)

- Redirección automática por rol tras el login y componente de sesión centralizado `nav.js`.
- Aislamiento por ubicación: los encargados solo ven y operan su sede asignada (`403` en caso contrario); el planificador no gestiona usuarios ni crea sedes.
- Asignaciones del administrador: `PUT /api/tiendas/{id}/encargado`, `PUT /api/centros-distribucion/{id}/encargado` y `PATCH /api/usuarios/{id}/rol`.
- Resiliencia ante Supabase Auth: fallback para reutilizar cuentas preexistentes ante `429`/`422`.

## Inventario unificado (HU-12, HU-16)

- Modelo único `Inventario` con regla de exclusividad de ubicación (`tiendaId` XOR `centroDistribucionId`); eliminada la entidad redundante `InventarioCentroDistribucion` y su puerto dedicado.
- `GET /api/inventario?tiendaId=...` (HU-12) y `GET /api/inventario/centro-distribucion?centroDistribucionId=...` (HU-16), con filtros por nombre, categoría y producto y cálculo dinámico de `stockBajo`.
- Autorización multirol: administradores y planificadores supervisan; el encargado accede exclusivamente a su sede.
- Cobertura: `InventarioExclusividadTest`, `InventarioServiceTest` e `IngresoMercanciaServiceTest`; optimización anti N+1 en la consulta de CDs.

## Ingreso de mercancía a CD (HU-18)

- `POST /api/centros-distribucion/{id}/ingresos`: acumula stock en el inventario del CD y registra el histórico con su origen (transaccional).
- Validaciones: cantidad > 0, origen obligatorio, existencia de CD y producto; el encargado del CD solo ingresa a su sede (`403`).

## Tiendas y centros de distribución (HU-07, HU-09)

- `POST /api/tiendas` con código de negocio automático `TND-XXXXXXXX` y `GET /api/tiendas`.
- `POST /api/centros-distribucion` con código `CD-XXXXXXXX`, capacidad y zonas de cobertura mediante relación `@ManyToMany` real (`centro_distribucion_tiendas` con claves foráneas).

## Usuarios y autenticación (HU-01)

- CRUD en `/api/usuarios` (crear, listar, detalle, eliminar, `me`), enum `Rol` y autenticación JWT con Supabase Auth.
- Reglas: correo único (`409 Conflict`), prohibición de autoeliminación y de eliminar administradores, sincronización con `auth.users` vía Admin API.

## Fundaciones técnicas

- Arquitectura hexagonal (puertos y adaptadores) con paquete `infrastructure` unificado y credenciales exclusivamente en variables de entorno (`DB_PASSWORD`, `SUPABASE_SERVICE_ROLE_KEY`).
- `@AuthUserId` + `AuthUserIdArgumentResolver`: el `UUID` del usuario autenticado se inyecta sin manipular objetos `Jwt` en los controladores.
- Suite de pruebas unitarias sobre H2 en memoria (`MODE=PostgreSQL`) para correr sin acceso a Supabase; la ejecución real usa PostgreSQL en la nube.
- Contratos de API para frontend en `docs/dto/` (JSON + TypeScript) e historias de usuario en `docs/Historias de Usuario/`.
