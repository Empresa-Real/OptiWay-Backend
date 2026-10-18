# HUS-01 — Restricción para Endpoints Privados

**Épica 1:** Usuarios y accesos  
**Feature 2:** Seguridad y control de acceso  
**Sprint:** Sprint 1  
**Estado:** `Implementado y Verificado`

---

## Historia de Usuario de Seguridad

> **Como** Administrador  
> **Quiero** que el sistema impida a los usuarios no autenticados acceder a los endpoints privados  
> **Para** proteger la información confidencial asegurando que solo los usuarios registrados puedan acceder a sus datos.  

---

## Criterios de Aceptación

1. **Rechazo a peticiones anónimas (HTTP 401):** Siendo un usuario no autenticado (sin header `Authorization: Bearer <token>` o con token inválido/expirado), al intentar realizar una petición a cualquier endpoint privado de la API (`/api/**`), el sistema rechaza la petición de forma inmediata y retorna un código de estado `HTTP 401 Unauthorized`.
2. **Acceso a usuarios autorizados:** Si un usuario presenta un token JWT válido y firmado por el proveedor de autenticación (Supabase Auth), el sistema valida la sesión y permite el acceso al recurso correspondiente.
3. **Exclusiones públicas:** Únicamente los assets estáticos necesarios para renderizar el inicio de sesión y la interfaz (`/`, `/*.html`, `/*.js`, `/config.js`, `/nav.js`, `/actuator/health`) quedan exentos de autenticación.

---

## Historia de Abuso

> **Como** atacante  
> **Quiero** realizar peticiones a un endpoint privado al cual no estoy autorizado  
> **Para** ver y robar información confidencial del usuario y de las operaciones de la cadena de suministro (tiendas, inventarios, ventas).  

---

## Mitigación Arquitectónica

* **Filtro de Seguridad Spring Security:** Configuración de `SecurityFilterChain` en [SecurityConfig.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/config/SecurityConfig.java).
* **Validador OAuth2 Resource Server:** Validación de JWT en el pipeline HTTP mediante `oauth2ResourceServer(oauth2 -> oauth2.jwt(...))` conectado al emisor de Supabase.
* **Bloqueo por Defecto:** Regla `anyRequest().authenticated()` que cierra por defecto cualquier endpoint que no esté explícitamente en la lista blanca pública.
* **Respuesta Estándar:** Emisión de `WWW-Authenticate: Bearer` con código `401 Unauthorized` ante peticiones sin credenciales.

---

## Matriz de Verificación de Endpoints

| Ruta | Método | Condición | Respuesta Esperada | Componente Responsable |
| :--- | :---: | :--- | :---: | :--- |
| `/api/productos` | `GET` / `POST` | Sin cabecera `Authorization` | **`401 Unauthorized`** | `SecurityFilterChain` |
| `/api/tiendas` | `GET` / `POST` | Sin cabecera `Authorization` | **`401 Unauthorized`** | `SecurityFilterChain` |
| `/api/ventas` | `POST` | Sin cabecera `Authorization` | **`401 Unauthorized`** | `SecurityFilterChain` |
| `/api/inventario` | `GET` | Sin cabecera `Authorization` | **`401 Unauthorized`** | `SecurityFilterChain` |
| `/api/centros-distribucion` | `GET` / `POST` | Sin cabecera `Authorization` | **`401 Unauthorized`** | `SecurityFilterChain` |
| `/api/productos` | `GET` | Con JWT válido de Supabase | **`200 OK`** | `OAuth2AuthenticationToken` |
| `/index.html` | `GET` | Sin cabecera (Público) | **`200 OK`** | Lista blanca `permitAll()` |

---

## Componentes Técnicos Asociados

* **Configuración de Seguridad:** [SecurityConfig.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/config/SecurityConfig.java)
* **Propiedades de Auth:** [application.properties](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties) (`spring.security.oauth2.resourceserver.jwt.issuer-uri`)
