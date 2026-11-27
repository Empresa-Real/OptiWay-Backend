# Documentación del Sistema OptiWay

**Proyecto:** OptiWay
**Tipo:** Sistema de gestión logística e inventarios
**Backend:** Java y Spring Boot
**Base de datos:** PostgreSQL mediante Supabase
**Arquitectura:** Hexagonal (Puertos y Adaptadores)


## 1. Introducción

OptiWay es un sistema orientado a la gestión de inventarios y operaciones logísticas. Permite administrar productos, tiendas, centros de distribución, existencias y procesos relacionados con la reposición de mercancía.

El backend proporciona una API REST que centraliza las reglas de negocio, la validación de operaciones, la autorización de usuarios y la persistencia de información.

Esta documentación describe la estructura técnica, los componentes principales, los mecanismos de seguridad y las funcionalidades implementadas.

## 2. Objetivos del sistema

- Centralizar la administración de productos y ubicaciones logísticas.
- Consultar y controlar las existencias de inventario.
- Registrar ajustes de inventario y conservar su historial.
- Gestionar operaciones de ingreso y venta de mercancía.
- Generar propuestas de reposición entre centros de distribución y tiendas.
- Controlar el acceso según la identidad, el rol y los permisos del usuario.
- Facilitar el seguimiento de operaciones mediante registros y evidencias.

## 3. Tecnologías utilizadas

| Área | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework backend | Spring Boot 4.1.2 |
| Seguridad | Spring Security 7 (OAuth2 Resource Server JWT) |
| API | REST |
| Persistencia | Spring Data JPA y Hibernate |
| Base de datos | PostgreSQL |
| Plataforma de base de datos y autenticación | Supabase |
| Contrato de API | OpenAPI 3 y Springdoc (`3.2.0`) |
| Interfaz de pruebas | Swagger UI |
| Pruebas automatizadas | JUnit 5, Mockito, Spring `MockMvc` y Python `requests` |
| Cobertura de código | JaCoCo (`0.8.13`, mínimo 40% en `verify`) |
| Base de datos para pruebas | H2 en memoria (`MODE=PostgreSQL`) |
| Análisis de seguridad | OWASP ZAP |
| Análisis de dependencias | OWASP Dependency-Check |
| Diagramas | PlantUML y notación C4 |

## 4. Arquitectura del sistema

El backend utiliza arquitectura hexagonal, también conocida como arquitectura de puertos y adaptadores.

Su propósito es separar las reglas del negocio de los detalles de infraestructura, como HTTP, bases de datos y servicios externos.

### 4.1. Organización de paquetes

| Paquete | Responsabilidad |
|---|---|
| `domain/model` | Modelos y conceptos del dominio. |
| `application/port/in` | Contratos de los casos de uso. |
| `application/port/out` | Contratos para persistencia y servicios externos. |
| `application/service` | Implementación de los casos de uso y reglas de aplicación. |
| `infrastructure/adapter/in/rest` | Controladores y adaptadores HTTP. |
| `infrastructure/adapter/out/persistence` | Adaptadores y entidades de persistencia. |
| `infrastructure/adapter/out/auth` | Integración con autenticación externa. |
| `infrastructure/config` | Configuración de la aplicación. |
| `infrastructure/security` | Resolución de identidad y mecanismos de seguridad. |

### 4.2. Flujo general de una petición

1. El cliente envía una petición HTTP al controlador REST.
2. El controlador recibe los datos y los transforma en una solicitud o comando.
3. El caso de uso correspondiente ejecuta la operación.
4. El servicio aplica validaciones y reglas de negocio.
5. Cuando se requiere información persistente, utiliza los puertos de salida.
6. Los adaptadores implementan la comunicación con PostgreSQL u otros servicios.
7. El resultado se transforma en una respuesta HTTP.

Esta organización permite mantener separadas las responsabilidades y facilita las pruebas unitarias.

## 5. Módulos funcionales

El backend contempla funcionalidades relacionadas con:

- Usuarios y roles.
- Productos (con catálogo estándar de unidades de medida).
- Tiendas (creación, asignación de encargado, activación/desactivación con validación de órdenes en tránsito).
- Centros de distribución (creación, asignación de encargado, activación/desactivación y consulta de órdenes entrantes).
- Inventarios y ajustes de inventario con historial de auditoría.
- Ingreso de mercancía en centros de distribución.
- Registro de ventas en tiendas.
- Solicitudes manuales de reposición.
- Recomendaciones de reposición (creación, consulta, aprobación, ajuste y rechazo).
- Órdenes de reposición.
- Patrones de demanda por producto y tienda.
- Reportes de rotación por tienda (consulta JSON y exportación CSV).
- Observabilidad y panel interno (`/admin` y `/api/estadisticas`).
- Autenticación y autorización.

La disponibilidad de cada operación depende de los permisos definidos para el usuario.

## 6. Autenticación y autorización

### 6.1. Autenticación

El sistema utiliza Supabase Auth para la autenticación de usuarios.

Después de iniciar sesión, el cliente obtiene un JWT que se envía al backend mediante el encabezado:

`Authorization: Bearer <token>`

El backend funciona como servidor de recursos OAuth2 y valida los tokens utilizando la configuración del emisor de Supabase.

### 6.2. Identidad del usuario

Los controladores utilizan el mecanismo `@AuthUserId` para obtener la identidad del usuario autenticado a partir del token.

La identidad no debe confiarse a un identificador enviado libremente en el cuerpo de una petición.

### 6.3. Roles

Los roles definidos en el dominio son:

- `ADMINISTRADOR`
- `ENCARGADO_TIENDA`
- `ENCARGADO_CD`
- `PLANIFICADOR`

La autorización se aplica según la acción solicitada y el recurso al que se intenta acceder.

Por ejemplo, el servicio de recomendaciones exige el rol `PLANIFICADOR` para crear, consultar, aprobar y rechazar recomendaciones.

Un usuario autenticado puede recibir una respuesta HTTP 403 si no dispone del permiso requerido.

## 7. Persistencia de información

La persistencia utiliza PostgreSQL y Spring Data JPA.

Las entidades JPA representan la estructura persistente, mientras que los adaptadores de salida implementan los puertos definidos por la aplicación.

La configuración de conexión se gestiona mediante propiedades y variables de entorno, evitando incorporar credenciales directamente en el código fuente.

La configuración de Hibernate y las migraciones deben administrarse considerando el entorno en el que se ejecuta la aplicación.

## 8. Registro de eventos (logging)

El backend utiliza el sistema de logging de Spring Boot y Logback.

Se registran eventos relevantes para facilitar el diagnóstico y seguimiento de las operaciones (usando IDs y cantidades, sin exponer contraseñas, tokens ni correos completos), entre ellos:

- Ajustes de inventario.
- Ingresos de mercancía.
- Registro de ventas.
- Creación y gestión de recomendaciones.
- Gestión de usuarios y cambios de rol.
- Eventos relacionados con autenticación.
- Errores no controlados (`ApiExceptionHandler`).

La configuración contempla:

- Nivel general y por paquete configurables mediante variables de entorno (`LOG_LEVEL`, `APP_LOG_LEVEL`).
- Perfil local (`application-local.properties`) para diagnóstico `DEBUG` y salida SQL en desarrollo.
- Registro en consola (`stdout`) y archivo local (`logs/optiway.log`) con rotación de `10MB`, retención de `14` días y tope de `100MB`.

El detalle completo de cómo funcionan se documenta en `docs/logs.md` y la configuración principal reside en `src/main/resources/application.properties`.

## 9. Seguridad

El proyecto incorpora mecanismos de autenticación, autorización y análisis de seguridad.

### 9.1. Análisis de dependencias (SCA)

Se utilizó OWASP Dependency-Check para analizar las dependencias del proyecto.

El informe generado documenta las dependencias examinadas y los resultados obtenidos de las fuentes consultadas.

El resultado del análisis no debe interpretarse como una garantía absoluta de ausencia de vulnerabilidades.

### 9.2. Análisis dinámico (DAST)

Se utilizó OWASP ZAP para realizar un análisis dinámico sobre el backend.

Se conservaron informes iniciales y finales para documentar los resultados y las alertas detectadas.

Los hallazgos deben evaluarse y corregirse según su impacto y prioridad. La existencia de un informe final no implica que todos los hallazgos estén resueltos.

### 9.3. Resumen de resultados

El detalle de los análisis y resultados se documenta en:

`docs/seguridad.md`

## 10. API y documentación interactiva

La API se documenta mediante OpenAPI 3 y Springdoc.

Swagger UI permite consultar los endpoints, sus parámetros, esquemas de solicitud y respuestas documentadas.

Cuando el backend se ejecuta localmente, las rutas disponibles son:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html` (también `/docs`, `/swagger` y `/swagger-ui.html`)
- Especificación OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

En producción (Render), la documentación y especificación se encuentran en:

- Swagger UI: `https://optiway-backend.onrender.com/docs` (también `/swagger-ui/index.html`)
- Especificación OpenAPI (JSON): `https://optiway-backend.onrender.com/v3/api-docs`

Las operaciones protegidas requieren autorización mediante un JWT válido.

La documentación ampliada se encuentra en:

`docs/Swagger_OpenAPI.md` y `docs/Contrato-Errores-API.md`

## 11. Pruebas automatizadas

El proyecto organiza sus pruebas en dos grupos principales:

1. **Suite Java (`src/test/java/com/example/optiway/`):** **31 archivos** (**248** métodos `@Test` / **258** ejecuciones en Surefire), divididos en:
   - **`integration/` (7 clases, 21 pruebas):** pruebas `@SpringBootTest` + `MockMvc` + `jwt()` contra H2 en memoria (`AjustarInventarioIntegrationTest`, `CentroDistribucionOrdenesIntegrationTest`, `PatronesDemandaIntegrationTest`, `ReporteRotacionIntegrationTest`, `RecomendacionReposicionRollbackIntegrationTest`, `RepositoriosConsultasDerivadasTest`, `OptiwayApplicationTests`).
   - **`unit/` (24 archivos, 227 métodos):** pruebas unitarias con JUnit 5 y Mockito organizadas en `application/`, `domain/`, `infrastructure/` y `infrastructure/adapter/out/auth/`.
2. **Suite HTTP / E2E en Python (`tests/api/`):** **5 scripts** (`test_suite.py`, `endpoints.py`, `endpoints_errores.py`, `endpoints_permisos.py`, `endpoints_supabase.py`) para validar el contrato HTTP en vivo contra el servidor Spring MVC y Supabase Auth.

La documentación individual de las suites de integración Spring y de los scripts Python se encuentra en `docs/tests/README.md`.

## 12. Estructura de documentación y evidencias

| Ruta | Contenido |
|---|---|
| `docs/Hexagonal-Arquitecture.md` | Explicación de arquitectura hexagonal. |
| `docs/Swagger_OpenAPI.md` | Documentación de la API. |
| `docs/Contrato-Errores-API.md` | Contrato estándar de respuestas de error por endpoint. |
| `docs/dto/` | Esquemas JSON y contratos DTO para el frontend. |
| `docs/logs.md` | Funcionamiento del sistema de logs, auditoría y observabilidad. |
| `docs/CI-CD-Gitflow.md` | Flujo Gitflow, validaciones del pipeline CI y despliegue CD en Render. |
| `docs/guias/` | Guías de trabajo del equipo (BD y snippet de frontend). |
| `docs/seguridad.md` | Controles de seguridad y resumen de evidencias DAST (ZAP) y SCA. |
| `docs/tests/` | Documentación individual de las pruebas de integración Spring y scripts Python. |
| `docs/diagramas/` | Diagramas C4 y de secuencia. |
| `docs/migrations/` | Scripts SQL de migración y ajuste de esquema en PostgreSQL (Supabase). |
| `docs/deudaTecnica.md` | Deuda técnica y decisiones pendientes. |
| `docs/CHANGELOG.md` | Historial de cambios. |
| `tests/api/` | Scripts de pruebas HTTP / E2E en Python. |

## 13. Consideraciones y limitaciones

- Los permisos dependen del rol y de la acción solicitada.
- Un código HTTP 403 indica que el usuario no dispone de autorización para la operación solicitada.
- Los resultados de pruebas automatizadas no garantizan por sí solos el correcto funcionamiento de todos los escenarios de producción.
- Los análisis de seguridad reflejan los resultados y el alcance de las herramientas utilizadas en el momento de su ejecución.
- Las operaciones que modifican información deben probarse en un entorno controlado para evitar alterar datos compartidos.
- La disponibilidad de funcionalidades en el frontend debe verificarse por separado de su implementación en el backend.

## 14. Conclusión

OptiWay cuenta con un backend organizado mediante arquitectura hexagonal, una API REST documentada, integración con Supabase para autenticación y persistencia, controles de autorización, registro de eventos y pruebas automatizadas.

La documentación y las evidencias organizadas en el repositorio permiten consultar la estructura del sistema, comprender sus principales procesos y dar seguimiento a las actividades de desarrollo, pruebas y seguridad.
