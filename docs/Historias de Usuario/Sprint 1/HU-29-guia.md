# HU-29: Iniciar sesion segun rol asignado

## Historia de usuario

Como usuario del sistema, sin importar mi rol, quiero iniciar sesion con mi correo y contraseña para acceder unicamente a las funciones y ubicaciones que corresponden a mi rol.

## Criterios de aceptacion

### Redireccion por rol

- Con credenciales validas, Supabase Auth autentica al usuario y entrega un JWT.
- `main.html` envia el `access_token` a `GET /api/usuarios/me`.
- El backend obtiene el UUID del claim `sub`.
- El backend busca ese UUID en `usuarios.auth_user_id` y devuelve el usuario local con su rol.
- Un `ADMINISTRADOR` es redirigido a `admin.html`.
- Un `ENCARGADO_TIENDA`, `ENCARGADO_CD` o `PLANIFICADOR` permanece en `main.html` hasta que exista su panel especifico.
- La redireccion visual del frontend no reemplaza la autorizacion del backend.

### Restriccion por ubicacion

Un usuario con rol `ENCARGADO_TIENDA` solo debe consultar tiendas asignadas a su usuario.

Un usuario con rol `ENCARGADO_CD` solo debe consultar centros de distribucion asignados a su usuario.

El backend debe aplicar este filtro en cada consulta. No es suficiente ocultar botones o filas en el frontend.

## Implementado actualmente (Fix 5)

### 1. Autenticación y Redirección por Rol
- Login con correo y contraseña mediante Supabase Auth.
- Validación de JWT en Spring Security (`sub` claim).
- Relación entre `auth.users.id` y `usuarios.auth_user_id`.
- Endpoint autenticado `GET /api/usuarios/me` para obtener el rol y datos del usuario local.
- **Redirección automática inmediata tras login**:
  - `ADMINISTRADOR` $\rightarrow$ `/admin.html`
  - `PLANIFICADOR` $\rightarrow$ `/main.html`
  - `ENCARGADO_TIENDA` $\rightarrow$ `/inventario.html`
  - `ENCARGADO_CD` $\rightarrow$ `/ingreso-mercancia.html`
- Componente compartido `nav.js` (`window.OptiWay.initNav`):
  - Verifica sesión con Supabase.
  - Valida permisos por vista (`rolesPermitidos`). Si no está autorizado, alerta `[Acceso Denegado]` y redirige.
  - Construye el navbar dinámico mostrando únicamente los enlaces autorizados.
  - Gestiona el cierre de sesión (`signOut`).

### 2. Aislamiento por Ubicación (Backend y Frontend)
- **`ENCARGADO_TIENDA`**:
  - Backend: `GET /api/tiendas` filtra por `encargadoId == usuario.id`. Si intenta consultar inventario ajeno (`GET /api/inventario`), el backend responde `403 Forbidden`.
  - Frontend: Selectores en `inventario.html` y tabla en `tiendas.html` limitados exclusivamente a sus tiendas asignadas.
- **`ENCARGADO_CD`**:
  - Backend: `GET /api/centros-distribucion` filtra por `encargadoId == usuario.id`. Si intenta registrar ingresos en un CD ajeno (`POST /api/ingresos-mercancia`), el backend responde `403 Forbidden`.
  - Frontend: Selectores en `ingreso-mercancia.html` y tabla en `centros-distribucion.html` limitados a su CD asignado.

### 3. Restricciones y Capacidades del Planificador
- **Visualización Global**: Puede consultar toda la red de tiendas, centros de distribución e inventarios para tareas logísticas.
- **Restricción Estricta**:
  - No puede ver la lista de usuarios: `GET /api/usuarios` responde `403 Forbidden` (`AccesoDenegadoException`).
  - No puede crear ni eliminar usuarios: `POST /api/usuarios` y `DELETE /api/usuarios/{id}` responden `403 Forbidden`.
  - No puede crear tiendas ni CDs: `POST /api/tiendas` y `POST /api/centros-distribucion` responden `403 Forbidden`. Formularios ocultos en UI.
  - No puede asignar encargados a sedes: `PUT /api/tiendas/{id}/encargado` y `PUT /api/centros-distribucion/{id}/encargado` responden `403 Forbidden`.

### 4. Gestión del Administrador
- Panel interactivo en `admin.html`:
  - Listado de usuarios con visualización clara de sus sedes asignadas (soporta múltiples tiendas/CDs con viñetas).
  - Asignación/reasignación interactiva de Tiendas y Centros (`PUT /{id}/encargado`).
  - Actualización de roles de usuario (`PATCH /api/usuarios/{id}/rol`).
  - Creación de usuarios con invitación por correo (y fallback automático si el usuario ya preexiste en Supabase Auth ante rate limits `429`).

---

## Flujo de Sesión y Seguridad

```text
Usuario abre /
  ↓
Supabase Auth valida correo y contraseña
  ↓
Supabase devuelve access_token (JWT)
  ↓
Frontend llama window.OptiWay.initNav({ rolesPermitidos })
  ↓
Consulta /api/usuarios/me con Bearer token
  ↓
Spring Security valida firma, emisor y expiracion
  ↓
Backend lee JWT.sub y devuelve usuario local con rol
  ↓
Frontend verifica si el rol está en rolesPermitidos:
  - Si NO: Alerta [Acceso Denegado] y redirige a su panel natural
  - Si SÍ: Renderiza navbar dinámico y carga vista
```

Si el token falta o expiró, la API responde `401 Unauthorized`. Si el usuario no tiene permisos para la operación o la ubicación, responde `403 Forbidden`.

---

## Pruebas de Aceptación Verificadas

- [x] Login válido de Administrador $\rightarrow$ Redirección a `admin.html`.
- [x] Login válido de Encargado de Tienda $\rightarrow$ Redirección a `inventario.html`.
- [x] Login válido de Encargado de CD $\rightarrow$ Redirección a `ingreso-mercancia.html`.
- [x] Login válido de Planificador $\rightarrow$ Redirección a `main.html`.
- [x] Encargado de Tienda solo ve su tienda asignada (tanto en backend como en frontend).
- [x] Encargado de CD solo ve su CD asignado (tanto en backend como en frontend).
- [x] Planificador tiene prohibido el acceso a gestión de usuarios (backend `403` y frontend oculto).
- [x] Planificador tiene prohibida la creación de tiendas y centros (backend `403` y frontend oculto).
- [x] Administrador asigna y reasigna tiendas y CDs a usuarios.
- [x] Administrador actualiza roles de usuarios en caliente.
