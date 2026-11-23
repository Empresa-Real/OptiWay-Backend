# Documentación de API con Swagger y OpenAPI 3 - OptiWay

Este documento describe la integración, arquitectura y uso de **OpenAPI 3** y **Swagger UI** en el backend de **OptiWay**, desarrollado por el **Equipo de Desarrollo SupplyCore**.

---

## 1. Visión General

OptiWay utiliza la especificación **OpenAPI 3** a través de la librería oficial **SpringDoc OpenAPI Starter WebMVC UI**. Esto permite:

1. **Generación automática del contrato de la API**: A partir de los controladores Spring Boot (`@RestController`), métodos HTTP (`@GetMapping`, `@PostMapping`, etc.), DTOs estructurados como `records` con Bean Validation y respuestas HTTP.
2. **Interfaz interactiva de pruebas (Swagger UI)**: Permite a los desarrolladores y al equipo de frontend explorar y probar endpoints en tiempo real sin requerir herramientas externas como Postman.
3. **Autenticación Bearer JWT integrada**: Soporte nativo para tokens JWT emitidos por **Supabase Auth**.

---

## 2. Dependencia Maven

En [`pom.xml`](../pom.xml):

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

> [!NOTE]
> Se utiliza la versión `2.8.5` (compatible con Spring Boot 3.x/4.x, Java 17+ y Jakarta EE 10), garantizando soporte para validaciones de `records`, OpenAPI 3.1 y serialización moderna.

---

## 3. URLs de Acceso

Cuando el backend se ejecuta localmente (`./mvnw spring-boot:run` en el puerto `8080`):

| Recurso | URL | Descripción |
| :--- | :--- | :--- |
| **Swagger UI (Atajo Directo)** | `http://localhost:8080/swagger-ui` o `/swagger` | Redirige automáticamente a la interfaz web de Swagger UI. |
| **Swagger UI (Estándar)** | `http://localhost:8080/swagger-ui.html` | Ruta web estándar de Swagger UI. |
| **OpenAPI Docs (JSON)** | `http://localhost:8080/v3/api-docs` | Especificación completa en formato JSON para herramientas cliente o generación de SDKs. |
| **Grafo de Arquitectura** | `http://localhost:8080/graph.html` | Visualización interactiva pública de dependencias y arquitectura hexagonal. |

En producción (`https://optiway-backend.onrender.com`) están disponibles los mismos recursos:

| Recurso | URL |
| :--- | :--- |
| **Swagger UI (Producción)** | `https://optiway-backend.onrender.com/docs` (redirige a `/swagger-ui/index.html`; también `/swagger` y `/swagger-ui.html`) |
| **OpenAPI Docs JSON (Producción)** | `https://optiway-backend.onrender.com/v3/api-docs` |

> [!IMPORTANT]
> La fuente autoritativa de endpoints es el Swagger de producción (`/docs`) o su JSON (`/v3/api-docs`), generado automáticamente desde los controladores. El catálogo de la sección 6 es solo una referencia rápida: si difieren, manda el Swagger en vivo.

---

## 4. Autenticación con Supabase (Bearer JWT)

OptiWay implementa seguridad basada en **OAuth 2.0 / Resource Server** con **Supabase Auth**.

### 4.1. Configuración del Esquema de Seguridad (`OpenApiConfig.java`)
En [`OpenApiConfig.java`](../src/main/java/com/example/optiway/infrastructure/config/OpenApiConfig.java), se registró el esquema de seguridad global `bearerAuth`:

```java
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthUserId.class);
    }

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OptiWay Backend API")
                        .version("1.0.0")
                        .description("API REST para la plataforma logística OptiWay: Gestión de Tiendas, Centros de Distribución, Inventario, Mercancía y Ventas.")
                        .contact(new Contact().name("Equipo de Desarrollo SupplyCore"))
                        .license(new License().name("Apache 2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el Bearer JWT proporcionado por Supabase Auth para autorizar peticiones protegidas.")));
    }
}
```

### 4.2. Inyección Criptográfica de `@AuthUserId`
Los controladores de OptiWay reciben la identidad del usuario mediante el parámetro `@AuthUserId UUID authUserId`.

- **Seguridad**: El UUID del usuario **nunca** se recibe como parámetro de URL ni del body. [`AuthUserIdArgumentResolver`](../src/main/java/com/example/optiway/infrastructure/security/AuthUserIdArgumentResolver.java) lo extrae criptográficamente del claim `sub` del JWT firmado por Supabase.
- **Ocultación en Swagger**: Para evitar que Swagger solicite manualmente un `authUserId`, se configuró:
  1. `SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthUserId.class)`.
  2. `@Parameter(hidden = true)` en la anotación [`AuthUserId.java`](../src/main/java/com/example/optiway/infrastructure/security/AuthUserId.java).

### 4.3. Cómo Autenticarse en Swagger UI
1. Iniciar sesión en el frontend o en la web local.
2. Extraer el token de acceso desde la consola (`localStorage`):
   ```javascript
   JSON.parse(localStorage.getItem(Object.keys(localStorage).find(k => k.includes('auth-token')))).access_token
   ```
3. En Swagger UI, hacer clic en el botón verde **Authorize** (con candado).
4. Pegar el valor del token en el campo **Value** y presionar **Authorize**.
5. Todas las solicitudes subsecuentes incluirán automáticamente el encabezado:
   `Authorization: Bearer <jwt>`.

---

## 5. Control de Entornos y Seguridad en Producción

1. **Habilitado en Docker / Despliegue**:
   En [`Dockerfile`](../Dockerfile#L27):
   ```dockerfile
   ENV SWAGGER_ENABLED=true
   ```
   Permite explorar y probar la API directamente en el despliegue de Render (`https://optiway-backend.onrender.com/swagger-ui.html`). Si en algún momento se desea desactivar en producción sin reconstruir la imagen, basta con asignar la variable de entorno `SWAGGER_ENABLED=false` en el panel de Render.

2. **Propiedades configurables en [`application.properties`](../src/main/resources/application.properties)**:
   ```properties
   # Soporte para proxy inverso HTTPS (Render, Cloudflare)
   server.forward-headers-strategy=framework

   springdoc.api-docs.enabled=${SWAGGER_ENABLED:true}
   springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:true}
   springdoc.swagger-ui.path=/swagger-ui.html
   springdoc.swagger-ui.operationsSorter=method
   springdoc.swagger-ui.tagsSorter=alpha
   ```

3. **Filtro de Seguridad en [`SecurityConfig.java`](../src/main/java/com/example/optiway/infrastructure/config/SecurityConfig.java#L32-L41)**:
   Se permite acceso público sin requerir token a las rutas de documentación cuando Swagger está activo:
   ```java
   .requestMatchers(
           "/",
           "/*.html",
           "/*.js",
           "/web/**",
           "/actuator/**",
           "/v3/api-docs/**",
           "/swagger-ui/**",
           "/swagger-ui.html"
   ).permitAll()
   ```

---

## 6. Catálogo de Controladores Documentados

> [!NOTE]
> Catálogo sincronizado con el OpenAPI de producción (`/v3/api-docs`). Cualquier operación nueva debe agregarse aquí en el mismo PR que la implementa. Las rutas `/graph`, `/graph/` y `/graph.html` están marcadas como `@Hidden` y no aparecen en el OpenAPI.

| Controlador | Ruta Base | Operaciones |
| :--- | :--- | :--- |
| **`usuario-controller`** | `/api/usuarios` | `POST /api/usuarios`, `GET /api/usuarios`, `GET /api/usuarios/{id}`, `GET /api/usuarios/me`, `DELETE /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/rol`. |
| **`tienda-controller`** | `/api/tiendas` | `POST /api/tiendas`, `GET /api/tiendas`, `PUT /api/tiendas/{id}/encargado`, `POST /api/tiendas/{id}/ventas`, `PATCH /api/tiendas/{id}/estado` (activar/desactivar tienda), `GET /api/tiendas/{id}/ordenes-en-transito` (booleano para el modal de confirmación). |
| **`centro-distribucion-controller`** | `/api/centros-distribucion` (alias `/api/cd`) | `POST`, `GET`, `PATCH /{id}/estado` (activar/desactivar CD), `PUT /{id}/encargado`, `POST /{id}/ingresos`, `PATCH /{id}/inventario`. Todas las operaciones existen también bajo el alias `/api/cd`. |
| **`producto-controller`** | `/api/productos` | `POST /api/productos`, `GET /api/productos`, `PATCH /api/productos/{id}` (editar), `PATCH /api/productos/{id}/estado` (activar/desactivar), `GET /api/productos/{id}/tiene-stock`. |
| **`inventario-controller`** | `/api/inventario` | `GET /api/inventario` (Inventario de Tienda), `GET /api/inventario/centro-distribucion` (Inventario de CD), `POST /api/inventario/ajustes` (ajuste de inventario de tienda o CD con auditoría). |
| **`reposicion-controller`** | `/api/tiendas/{id}/solicitudes-reposicion`, `/api/solicitudes-reposicion` | `POST /api/tiendas/{id}/solicitudes-reposicion` (solicitud manual HU-15), `GET /api/solicitudes-reposicion` (listado para el planificador). |
| **`recomendacion-reposicion-controller`** | `/api/recomendaciones-reposicion` | `POST` (crear propuesta HU-20), `GET` (consultar), `PATCH /{id}/aprobar` (aprobar o ajustar, genera orden HU-21), `PATCH /{id}/rechazar` (rechazar con motivo HU-21). |
| **`demanda-controller`** | `/api/demanda` | `GET /api/demanda/patrones` (histórico de ventas por producto y tiendas; máx. 366 días y 50 tiendas; requiere rol `PLANIFICADOR`). |
| **`reporte-rotacion-controller`** | `/api/reportes/rotacion` | `GET /api/reportes/rotacion` (consulta JSON), `GET /api/reportes/rotacion/exportar` (descarga CSV UTF-8; requiere rol `PLANIFICADOR`). |

### 6.1. Notas de integración para frontend

- **Desactivar tienda**: `PATCH /api/tiendas/{id}/estado` con body `{"activo": false}`. Si la tienda tiene órdenes en tránsito, la API responde `409` hasta enviar `{"activo": false, "confirmarConOrdenesEnTransito": true}`. Se puede consultar primero `GET /api/tiendas/{id}/ordenes-en-transito`, que devuelve `{"tieneOrdenesEnTransito": true|false}`.
- **Desactivar CD**: `PATCH /api/centros-distribucion/{id}/estado` (o `/api/cd/{id}/estado`) con body `{"activo": false}`. Si abastece tiendas, responde `409` hasta enviar `{"activo": false, "confirmarConTiendasAsignadas": true}`.
- **Roles**: desactivar tienda/CD requiere `ADMINISTRADOR` (un `PLANIFICADOR` recibe `403`, no un `404`). Las operaciones de recomendaciones, demanda y reportes de rotación requieren `PLANIFICADOR`.
- **Editar producto** es `PATCH /api/productos/{id}` (no `PUT`).
- **HU-19 (consultar patrones de demanda)**: disponible en `GET /api/demanda/patrones` con `productoId`, `tiendaIds`, `desde` y `hasta` (máx. 366 días y 50 tiendas; rol `PLANIFICADOR`). La guía de HU-19 está actualizada con el contrato real.
- **HU-27 (reporte de rotación)**: disponible en `GET /api/reportes/rotacion` y `GET /api/reportes/rotacion/exportar` con `tiendaId`, `desde` y `hasta` (rol `PLANIFICADOR`). Ver guías detalladas en [`docs/Historias de Usuario/Sprint 3`](Historias%20de%20Usuario/Sprint%203/README.md).
- Para verificar cualquier endpoint: `https://optiway-backend.onrender.com/docs` o descargar el contrato desde `https://optiway-backend.onrender.com/v3/api-docs`.
