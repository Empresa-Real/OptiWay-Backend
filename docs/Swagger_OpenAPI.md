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

En [`pom.xml`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/pom.xml):

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
| **Swagger UI (Interfaz Web)** | `http://localhost:8080/swagger-ui.html` | Interfaz gráfica interactiva para explorar y ejecutar peticiones. |
| **OpenAPI Docs (JSON)** | `http://localhost:8080/v3/api-docs` | Especificación completa en formato JSON para herramientas cliente o generación de SDKs. |

---

## 4. Autenticación con Supabase (Bearer JWT)

OptiWay implementa seguridad basada en **OAuth 2.0 / Resource Server** con **Supabase Auth**.

### 4.1. Configuración del Esquema de Seguridad (`OpenApiConfig.java`)
En [`OpenApiConfig.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/config/OpenApiConfig.java), se registró el esquema de seguridad global `bearerAuth`:

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

- **Seguridad**: El UUID del usuario **nunca** se recibe como parámetro de URL ni del body. [`AuthUserIdArgumentResolver`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthUserIdArgumentResolver.java) lo extrae criptográficamente del claim `sub` del JWT firmado por Supabase.
- **Ocultación en Swagger**: Para evitar que Swagger solicite manualmente un `authUserId`, se configuró:
  1. `SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthUserId.class)`.
  2. `@Parameter(hidden = true)` en la anotación [`AuthUserId.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthUserId.java).

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
   En [`Dockerfile`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/Dockerfile#L27):
   ```dockerfile
   ENV SWAGGER_ENABLED=true
   ```
   Permite explorar y probar la API directamente en el despliegue de Render (`https://optiway-backend.onrender.com/swagger-ui.html`). Si en algún momento se desea desactivar en producción sin reconstruir la imagen, basta con asignar la variable de entorno `SWAGGER_ENABLED=false` en el panel de Render.

2. **Propiedades configurables en [`application.properties`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties)**:
   ```properties
   # Soporte para proxy inverso HTTPS (Render, Cloudflare)
   server.forward-headers-strategy=framework

   springdoc.api-docs.enabled=${SWAGGER_ENABLED:true}
   springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:true}
   springdoc.swagger-ui.path=/swagger-ui.html
   springdoc.swagger-ui.operationsSorter=method
   springdoc.swagger-ui.tagsSorter=alpha
   ```

3. **Filtro de Seguridad en [`SecurityConfig.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/config/SecurityConfig.java#L32-L41)**:
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

| Controlador | Ruta Base | Operaciones Principales |
| :--- | :--- | :--- |
| **`inventario-controller`** | `/api/inventario` | `GET /api/inventario` (Inventario de Tienda), `GET /api/inventario/centro-distribucion` (Inventario de CD). |
| **`ingreso-mercancia-controller`** | `/api/ingresos-mercancia` | `POST /api/ingresos-mercancia` (Registro de ingresos con validación de capacidad). |
| **`venta-controller`** | `/api/ventas` | `POST /api/ventas` (Registro de venta con deducción atómica de stock). |
| **`producto-controller`** | `/api/productos` | `GET /api/productos`, `POST /api/productos`, `PUT /api/productos/{id}`, `PATCH /api/productos/{id}/estado`. |
| **`tienda-controller`** | `/api/tiendas` | `GET /api/tiendas`, `POST /api/tiendas`, `PUT /api/tiendas/{id}/encargado`. |
| **`centro-distribucion-controller`** | `/api/centros-distribucion` | `GET /api/centros-distribucion`, `POST /api/centros-distribucion`, `PUT /api/centros-distribucion/{id}/encargado`. |
| **`usuario-controller`** | `/api/usuarios` | `GET /api/usuarios`, `POST /api/usuarios`, `GET /api/usuarios/me`, `PATCH /api/usuarios/{id}/rol`, `DELETE /api/usuarios/{id}`. |
| **`root-controller`** | `/` | `GET /` (Estado general del servicio). |
