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
rol      VARCHAR
```

Esto sirve para desarrollo, pero no es recomendable como diseño definitivo. Para un equipo convendría agregar:

```text
docs/database.md
docs/database-schema.sql
```

Y más adelante usar migraciones con Flyway o Liquibase. También deberías documentar decisiones como:

- Roles permitidos.
- Si el correo pertenece a Supabase Auth.
- Relaciones futuras con tiendas, centros o productos.
- Qué ocurre al eliminar un usuario.
- Índices y restricciones.
- Quién administra los roles.

En resumen: ahora existe un **modelo implícito generado por JPA**, pero todavía no un diseño de base de datos formal y versionado.