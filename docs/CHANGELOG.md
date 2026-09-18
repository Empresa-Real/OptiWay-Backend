# Changelog

## [Unreleased]

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
