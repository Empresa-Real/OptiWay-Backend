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
- `ENCARGADO_CENTRO_DISTRIBUCION`
- `CONDUCTOR`
- `CLIENTE`

La lista esta temporalmente definida como textos en `UsuarioService`. Cuando el equipo confirme los roles definitivos, reemplazar `String rol` por un enum `Rol` y usar `@Enumerated(EnumType.STRING)` en `UsuarioJpaEntity`.

## Respuestas esperadas

- `201 Created`: usuario creado y guardado.
- `400 Bad Request`: nombre, correo o rol invalido.
- `409 Conflict`: el correo ya existe.

Para consultar los usuarios:

```text
GET /api/usuarios
```

Para consultar uno:

```text
GET /api/usuarios/{id}
```

## Prueba local

1. Define `DB_PASSWORD` en la configuracion de ejecucion de IntelliJ.
2. Inicia `OptiwayApplication`.
3. Abre `http://localhost:8080/`.
4. Completa nombre, correo y uno de los roles validos.
5. Pulsa `Crear usuario`.

Tambien se puede probar desde Postman con `POST http://localhost:8080/api/usuarios`, seleccionando `Body > raw > JSON`.

## Reglas que ya estan implementadas

- El nombre y el correo son obligatorios.
- El correo se recorta antes de guardarse.
- La aplicacion comprueba si el correo ya existe ignorando mayusculas y minusculas.
- La entidad JPA tiene una restriccion unica para el correo como proteccion adicional.
- Los errores de validacion y duplicidad se convierten en respuestas HTTP claras.

## Trabajo pendiente

La HU tambien indica enviar informacion de acceso por correo. Eso aun no esta implementado. Antes de hacerlo hay que decidir:

1. Si el acceso sera con contrasena temporal, enlace de activacion o Supabase Auth.
2. Que proveedor SMTP o servicio de correo utilizara el proyecto.
3. Como se almacenaran las credenciales sin guardar contrasenas en texto plano.

OAuth con Google/Supabase queda para una etapa posterior. Cuando se implemente, el backend debera validar el JWT y asociar el usuario autenticado con su rol local.

## Siguiente paso recomendado

Definir formalmente los roles y convertirlos en enum. Luego agregar pruebas para:

- Crear un usuario valido.
- Rechazar un correo duplicado.
- Rechazar un rol invalido.
- Verificar que el rol se persista y se devuelva en la respuesta.
