Actualmente no existe un diseño formal de base de datos como:

- Diagrama entidad-relación.
- Scripts SQL versionados.
- Migraciones con Flyway o Liquibase.
- Documento con tablas, columnas y relaciones.

La tabla `usuarios` se crea o actualiza automáticamente mediante JPA porque tienes:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Y la estructura se infiere desde:

```java
@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity
```

Actualmente la tabla representa aproximadamente:

```text
usuarios
---------
id       BIGINT PRIMARY KEY
nombre   VARCHAR
email    VARCHAR UNIQUE NOT NULL
rol      VARCHAR NOT NULL (`ADMINISTRADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`, `PLANIFICADOR`)
creado_por UUID  -- auth.users.id del administrador autenticado que lo creo
```

Esto sirve para desarrollo, pero no es recomendable como diseño definitivo. Para un equipo convendría agregar:

```text
docs/database.md
docs/database-schema.sql
```

Y más adelante usar migraciones con Flyway o Liquibase. También deberías documentar decisiones como:

- Roles permitidos y su persistencia como texto mediante un enum Java.
- Si el correo pertenece a Supabase Auth.
- Relaciones futuras con tiendas, centros o productos.
- Qué ocurre al eliminar un usuario.
- Índices y restricciones.
- Quién administra los roles.

El administrador inicial se registra mediante `INITIAL_ADMIN_EMAIL` y `INITIAL_ADMIN_AUTH_USER_ID`. Ambos deben existir primero en Supabase Auth. El bootstrap solo crea la fila local con rol `ADMINISTRADOR`; no guarda contraseñas ni crea credenciales.

`creado_por` no se acepta desde el JSON del cliente. El backend lo obtiene del claim `sub` del JWT validado de Supabase, que corresponde al `auth.users.id`, por lo que un cliente no puede atribuir la creación a otro usuario.

En resumen: ahora existe un **modelo implícito generado por JPA**, pero todavía no un diseño de base de datos formal y versionado.