# Guia de continuacion: HU-01

## Estado actual

La API permite crear usuarios con nombre, correo y rol mediante:

```text
POST /api/usuarios
```

El cuerpo esperado es:

```json
{
  "nombre": "Ana Gonzalez",
  "email": "ana@empresa.com",
  "rol": "ADMINISTRADOR"
}
```

Los roles aceptados actualmente son:

- `ADMINISTRADOR`
- `ENCARGADO_TIENDA`
- `ENCARGADO_CD`
- `PLANIFICADOR`

Los roles se representan con el enum `Rol` y se persisten como texto mediante `@Enumerated(EnumType.STRING)`.

Solo un usuario autenticado cuyo registro local tenga rol `ADMINISTRADOR` puede crear usuarios.

## Respuestas esperadas

- `201 Created`: usuario creado y guardado.
- `400 Bad Request`: nombre, correo o rol invalido.
- `409 Conflict`: el correo ya existe.
- `401 Unauthorized`: falta un JWT valido de Supabase.
- `403 Forbidden`: el usuario autenticado no es administrador.

Para consultar los usuarios:

```text
GET /api/usuarios
```

Para consultar uno:

```text
GET /api/usuarios/{id}
```

## Prueba local

1. Define `DB_PASSWORD` y `SUPABASE_PROJECT_REF` en la configuracion de ejecucion de IntelliJ.
2. Inicia `OptiwayApplication`.
3. Crea el administrador inicial en Supabase Authentication.
4. Crea manualmente la fila local con el mismo `auth_user_id` y rol `ADMINISTRADOR`.
5. Inicia sesion en Supabase y obtén el `access_token`.
6. Abre `http://localhost:8080/` y usa la vista de login.
7. Completa nombre, correo y uno de los roles permitidos.
8. Pulsa `Crear usuario`.

Cuando el administrador crea un usuario, Supabase envia la invitacion a su correo. El enlace abre `set-password.html`, donde el usuario configura su contraseña. Al terminar, se redirige a `main.html` y el backend resuelve su rol mediante `auth_user_id`.

La ejecucion debe tener configuradas estas variables:

```text
DB_PASSWORD=...
SUPABASE_SERVICE_ROLE_KEY=...
SUPABASE_INVITE_REDIRECT_URL=http://localhost:8080/set-password.html
```

La `SUPABASE_SERVICE_ROLE_KEY` solo vive en el backend y nunca debe colocarse en el HTML.

Tambien se puede probar desde Postman con `POST http://localhost:8080/api/usuarios`, seleccionando `Body > raw > JSON`.

## Sesion, JWT y redireccion por rol

El flujo de un usuario autenticado es:

```text
Supabase Auth inicia sesion
  ↓
Supabase entrega access_token (JWT)
  ↓
main.html envia Authorization: Bearer <access_token>
  ↓
GET /api/usuarios/me
  ↓
UsuarioController lee jwt.getSubject()
  ↓
ObtenerUsuarioUseCase busca usuarios.auth_user_id
  ↓
main.html recibe el rol y decide la pagina
```

La implementacion concreta esta en estos puntos:

- `main.html`: obtiene `data.session.access_token` y llama a `/api/usuarios/me`.
- `UsuarioController`: convierte `jwt.getSubject()` a UUID y llama a `obtenerUsuarioPorAuthId`.
- `UsuarioService`: delega la busqueda por `auth_user_id`.
- `UsuarioRepositoryAdapter`: consulta `findByAuthUserId` en JPA.
- `main.html`: redirige a `admin.html` solo si el rol es `ADMINISTRADOR`; los demas permanecen en `main.html`.

`sub` significa *subject*. Es el UUID de la cuenta en `auth.users` incluido en el JWT. Ese mismo UUID debe estar en `usuarios.auth_user_id`; así el backend enlaza la identidad de Supabase con el perfil local y su rol. El email no se usa para autorizar porque puede cambiar.

### Que es un JWT

Un JWT es un token firmado que Supabase entrega despues del login. Contiene, entre otros datos:

- `sub`: UUID estable del usuario en `auth.users`.
- `email`: correo del usuario. Se usa como dato de contacto, no para autorizar.
- `exp`: instante en que vence el access token.
- `iss`: emisor esperado de Supabase.

El navegador envia el token en cada peticion protegida:

```http
Authorization: Bearer <access_token>
```

Spring Security valida la firma, el emisor y la expiracion antes de ejecutar el controlador. Si el token falta, es invalido o vencio, responde `401 Unauthorized`. Si el token es valido pero el usuario no tiene permiso, la aplicacion responde `403 Forbidden`.

### Vencimiento y renovacion

El `access_token` dura un tiempo limitado. Supabase JS mantiene tambien un `refresh_token` y normalmente renueva la sesion automaticamente mientras el usuario siga autenticado. El frontend debe obtener la sesion actual con `getSession()` antes de llamar al backend, en lugar de guardar manualmente el JWT.

Si la renovacion falla o el refresh token expira, `getSession()` ya no devuelve una sesion valida; la pagina debe enviar al usuario a `/` para iniciar sesion de nuevo. El backend nunca debe aceptar un token vencido ni intentar renovarlo con la `service_role`.

El access token y el refresh token son secretos de sesion: no deben copiarse a logs, commits, capturas ni mensajes.

## Reglas que ya estan implementadas

- El nombre y el correo son obligatorios.
- El correo se recorta antes de guardarse.
- La aplicacion comprueba si el correo ya existe ignorando mayusculas y minusculas.
- La entidad JPA tiene una restriccion unica para el correo como proteccion adicional.
- Cada usuario nuevo guarda el UUID de Supabase del administrador autenticado que lo creo.
- Los errores de validacion y duplicidad se convierten en respuestas HTTP claras.

## Seguridad del registro

El frontend no debe llamar directamente a `supabase.auth.signUp` para crear usuarios de la empresa.
La creacion pasa por `POST /api/usuarios`, que valida el JWT y comprueba que su UUID (`sub`) corresponde a un registro local con rol `ADMINISTRADOR`. El email no se usa para autorizar.

## Trabajo pendiente

La HU tambien indica enviar informacion de acceso por correo. Eso aun no esta implementado. Antes de hacerlo hay que decidir:

1. Si el acceso sera con contrasena temporal, enlace de activacion o Supabase Auth.
2. Que proveedor SMTP o servicio de correo utilizara el proyecto.
3. Como se almacenaran las credenciales sin guardar contrasenas en texto plano.

OAuth con Google queda para una etapa posterior. Actualmente se usa autenticacion por correo y contraseña mediante Supabase Auth.

## Siguiente paso recomendado

Agregar pruebas para:

- Crear un usuario valido.
- Rechazar un correo duplicado.
- Rechazar un rol invalido.
- Verificar que el rol se persista y se devuelva en la respuesta.
