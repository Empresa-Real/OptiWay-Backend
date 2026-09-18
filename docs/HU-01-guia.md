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
4. Define `INITIAL_ADMIN_EMAIL` con ese mismo correo.
5. Inicia la aplicacion una vez; el bootstrap crea la fila local con rol `ADMINISTRADOR` si no existe.
6. Inicia sesion en Supabase y obtén el `access_token`.
7. Abre `http://localhost:8080/` y pega el token en la vista estatica.
8. Completa nombre, correo y uno de los roles permitidos.
9. Pulsa `Crear usuario`.

Tambien se puede probar desde Postman con `POST http://localhost:8080/api/usuarios`, seleccionando `Body > raw > JSON`.

## Reglas que ya estan implementadas

- El nombre y el correo son obligatorios.
- El correo se recorta antes de guardarse.
- La aplicacion comprueba si el correo ya existe ignorando mayusculas y minusculas.
- La entidad JPA tiene una restriccion unica para el correo como proteccion adicional.
- Cada usuario nuevo guarda el UUID de Supabase del administrador autenticado que lo creo.
- Los errores de validacion y duplicidad se convierten en respuestas HTTP claras.

## Seguridad del registro

El frontend no debe llamar directamente a `supabase.auth.signUp` para crear usuarios de la empresa.
La creacion pasa por `POST /api/usuarios`, que valida el JWT y comprueba que el correo autenticado tiene rol `ADMINISTRADOR` en la tabla local.

## Trabajo pendiente

La HU tambien indica enviar informacion de acceso por correo. Eso aun no esta implementado. Antes de hacerlo hay que decidir:

1. Si el acceso sera con contrasena temporal, enlace de activacion o Supabase Auth.
2. Que proveedor SMTP o servicio de correo utilizara el proyecto.
3. Como se almacenaran las credenciales sin guardar contrasenas en texto plano.

OAuth con Google/Supabase queda para una etapa posterior. El backend ya esta preparado para validar JWT de Supabase cuando se configure el proveedor.

## Siguiente paso recomendado

Agregar pruebas para:

- Crear un usuario valido.
- Rechazar un correo duplicado.
- Rechazar un rol invalido.
- Verificar que el rol se persista y se devuelva en la respuesta.
