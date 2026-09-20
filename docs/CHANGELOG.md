# Changelog

## 2026-09-18

### Added

- Guia de HU-29 para login por rol, redireccion y restricciones futuras por ubicacion.
- Enum `Rol` con `ADMINISTRADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD` y `PLANIFICADOR`.
- Autenticacion JWT con Supabase para la API.
- Restriccion de creacion de usuarios al administrador autenticado.
- Registro del administrador que crea cada usuario.

### Changed

- La vista estatica envia el token Bearer y usa un selector de roles validos.

## 2026-09-18

### Added

- CRUD HTTP para usuarios en `/api/usuarios`.
- Persistencia de usuarios con PostgreSQL mediante Spring Data JPA.
- Campo `rol` en el usuario y en la entidad de persistencia.
- Validacion de roles permitidos al crear usuarios.
- Comprobacion de correos duplicados con respuesta `409 Conflict`.
- Restriccion unica para el correo en la entidad JPA.
- Pagina HTML sencilla para probar la creacion de usuarios.
- Guia de continuacion para HU-01.

### Changed

- La configuracion de base de datos usa `DB_PASSWORD` desde una variable de entorno.
- El ID de usuario usa `Long` para permitir que JPA lo genere antes de guardar.

### Pending

- Definir los roles definitivos y reemplazar el texto por un enum.
- Implementar el envio de informacion de acceso por correo.
- Integrar OAuth con Google mediante Supabase en una etapa posterior.

## YYYY-MM-DD

### Added

- Describe aqui las funcionalidades nuevas.

### Changed

- Describe aqui los cambios sobre funcionalidades existentes.

### Fixed

- Describe aqui los errores corregidos.

### Pending

- Describe aqui el trabajo pendiente relacionado con este bloque.
