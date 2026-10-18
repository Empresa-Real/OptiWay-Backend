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

## Modelo de Seguridad en Dos Capas (Autenticación vs. Autorización)

El sistema implementa una arquitectura de seguridad en dos barreras consecutivas:

```text
Solicitud HTTP
      │
      ▼
┌────────────────────────────────────────────────────────┐
│  CAPA 1: AUTENTICACIÓN (Spring Security - HUS-01)      │
│  ¿El usuario ha iniciado sesión con token válido?      │
└────────────────────────────────────────────────────────┘
      │
      ├── NO ──► HTTP 401 Unauthorized (Bloqueo inmediato)
      │
      ▼ SÍ
┌────────────────────────────────────────────────────────┐
│  CAPA 2: AUTORIZACIÓN POR ROLES Y UBICACIÓN (RBAC)     │
│  ¿El rol y la sede asignada le permiten esta acción?   │
└────────────────────────────────────────────────────────┘
      │
      ├── NO ──► HTTP 403 Forbidden (AccesoDenegadoException)
      │
      ▼ SÍ
  Ejecución en Controlador, Caso de Uso y Base de Datos
```

### Matriz de Autorización por Rol (Capa 2)

| Recurso / Acción | Endpoint | `ADMINISTRADOR` | `PLANIFICADOR` | `ENCARGADO_TIENDA` | `ENCARGADO_CD` |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **Gestión de Usuarios** | `POST/GET /api/usuarios` | ✅ Sí | ❌ No (`403`) | ❌ No (`403`) | ❌ No (`403`) |
| **Crear / Editar Tiendas** | `POST/PUT /api/tiendas` | ✅ Sí | ✅ Sí | ❌ No (`403`) | ❌ No (`403`) |
| **Crear / Editar CDs** | `POST/PUT /api/centros-distribucion` | ✅ Sí | ✅ Sí | ❌ No (`403`) | ❌ No (`403`) |
| **Gestión Catálogo Productos** | `POST/PATCH /api/productos` | ✅ Sí | ❌ No (`403`) | ❌ No (`403`) | ❌ No (`403`) |
| **Inventario de Tienda** | `GET /api/inventario?tiendaId=X` | ✅ Todas | ✅ Todas | 🔒 Solo tienda asignada | ❌ No (`403`) |
| **Inventario de CD** | `GET /api/inventario/centro-distribucion` | ✅ Todos | ✅ Todos | ❌ No (`403`) | 🔒 Solo CD asignado |
| **Ingreso de Mercancía CD** | `POST /api/ingresos-mercancia` | ✅ Sí | ✅ Sí | ❌ No (`403`) | 🔒 Solo CD asignado |
| **Registrar Ventas Tienda** | `POST /api/ventas` | ✅ Sí | ❌ No (`403`) | 🔒 Solo tienda asignada | ❌ No (`403`) |

---

## Matriz de Verificación de Endpoints

| Ruta | Método | Condición | Respuesta Esperada | Componente Responsable |
| :--- | :---: | :--- | :---: | :--- |
| `/api/**` (Cualquier endpoint privado) | `Cualquiera` | Sin cabecera `Authorization` | **`401 Unauthorized`** | `SecurityFilterChain` |
| `/api/usuarios` | `POST` | Autenticado con rol `PLANIFICADOR` | **`403 Forbidden`** | `UsuarioService` |
| `/api/ventas` | `POST` | Autenticado con `ENCARGADO_TIENDA` ajeno | **`403 Forbidden`** | `VentaService` |
| `/api/inventario/cd` | `GET` | Autenticado con `ENCARGADO_CD` ajeno | **`403 Forbidden`** | `ConsultarInventarioCDService` |
| `/api/productos` | `GET` | Autenticado con rol válido | **`200 OK`** | `ProductoController` |
| `/index.html` | `GET` | Sin cabecera (Público) | **`200 OK`** | Lista blanca `permitAll()` |

---

## Componentes Técnicos Asociados

* **Configuración de Seguridad:** [SecurityConfig.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/config/SecurityConfig.java)
* **Propiedades de Auth:** [application.properties](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties) (`spring.security.oauth2.resourceserver.jwt.issuer-uri`)
