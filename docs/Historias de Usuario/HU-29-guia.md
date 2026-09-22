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

## Implementado actualmente

- Login con correo y contraseña mediante Supabase Auth.
- Validacion de JWT en Spring Security.
- Relacion entre `auth.users.id` y `usuarios.auth_user_id`.
- Endpoint autenticado `GET /api/usuarios/me`.
- Busqueda del rol local mediante el claim `sub` del JWT.
- Redireccion de `ADMINISTRADOR` a `admin.html`.
- Usuarios con otros roles permanecen en `main.html`.
- Creacion de usuarios restringida a un usuario cuyo rol local sea `ADMINISTRADOR`.

## Pendiente de HU-29

### Paneles por rol

Crear las vistas y endpoints propios de cada rol:

- `admin.html`: administracion de usuarios, productos, tiendas y centros.
- Panel de `ENCARGADO_TIENDA`.
- Panel de `ENCARGADO_CD`.
- Panel de `PLANIFICADOR`.

### Ubicaciones y asignaciones

Actualmente no existen en el modelo las entidades necesarias para filtrar por ubicacion. Se necesitan otras HU para definir:

- Tiendas.
- Centros de distribucion.
- Relacion usuario-tienda.
- Relacion usuario-centro de distribucion.
- Creacion y edicion de asignaciones.

El modelo recomendado es:

```text
usuarios
  |
  +-- usuario_tienda -- tiendas
  |
  +-- usuario_centro_distribucion -- centros_distribucion
```

Las tablas de asignacion deben tener restricciones unicas para evitar duplicados y claves foraneas hacia usuarios y ubicaciones.

### Autorizacion por rol y ubicacion

Cuando existan esas tablas, cada caso de uso debera recibir la identidad autenticada o un contexto de seguridad derivado del JWT. El servicio debe aplicar reglas como:

```text
ADMINISTRADOR
  puede consultar y administrar todo

ENCARGADO_TIENDA
  solo puede consultar sus tiendas asignadas

ENCARGADO_CD
  solo puede consultar sus centros asignados

PLANIFICADOR
  puede acceder a las funciones de planificacion definidas por su HU
```

Nunca se debe aceptar `usuarioId`, `tiendaId` o `centroId` como prueba de permisos enviada por el cliente. El backend debe obtener la identidad del JWT y comprobar la asignacion en la base de datos.

## Dependencias con otras HU

HU-29 depende de que se definan previamente:

- Modelo definitivo de tiendas.
- Modelo definitivo de centros de distribucion.
- Reglas de asignacion de encargados.
- Funcionalidades de productos y planificacion.
- Paneles frontend por rol.
- Datos iniciales del administrador y de las ubicaciones.

Por eso, la parte de login y resolucion de rol puede probarse ahora, pero la restriccion por ubicacion no debe darse por terminada hasta que existan las tablas, puertos, adaptadores, servicios y pruebas de asignacion.

## Flujo de sesion

```text
Usuario abre /
  ↓
Supabase Auth valida correo y contraseña
  ↓
Supabase devuelve access_token (JWT)
  ↓
Frontend consulta /api/usuarios/me con Bearer token
  ↓
Spring Security valida firma, emisor y expiracion
  ↓
Backend lee JWT.sub
  ↓
Busca usuarios.auth_user_id
  ↓
Devuelve rol local
  ↓
Frontend redirige o muestra el panel correspondiente
```

Si el token falta o vencio, la API responde `401 Unauthorized`. Si el token es valido pero el usuario no tiene permisos para la operacion, responde `403 Forbidden`.

## Pruebas pendientes

- Login valido de un administrador.
- Login valido de cada rol.
- Redireccion del administrador a `admin.html`.
- Permanencia de usuarios normales en `main.html`.
- Usuario autenticado sin fila local.
- Token vencido.
- Encargado consultando una ubicacion no asignada.
- Encargado consultando una ubicacion asignada.
- Intento de manipular IDs de usuario o ubicacion desde el frontend.
