# Changelog

## 2026-09-21

### Added

- **HU-12 (Consultar Inventario de Mi Tienda):**
  - Caso de uso `ConsultarInventarioUseCase` y servicio `InventarioService`.
  - Endpoint `GET /api/inventario` con filtros por `tiendaId`, `nombre`, `categoria` y `productoId`.
  - Validación de acceso para encargados de tienda asignados (`encargadoId == usuario.id`) y administradores.
  - Cálculo dinámico de stock bajo (`stockBajo: true` cuando `cantidadActual < stockMinimo`).
  - Modelos de dominio `Inventario`, `Producto` y adaptadores de persistencia JPA.
  - Documentación de historia de usuario en `docs/Historias de Usuario/HU-12_Consultar_inventario_tienda.md`.
- **HU-18 (Registrar Ingreso de Mercancía a Centro de Distribución):**
  - Caso de uso `RegistrarIngresoMercanciaUseCase` y servicio transaccional `IngresoMercanciaService`.
  - Endpoint `POST /api/ingresos-mercancia` para ingreso de mercancía desde proveedores u otros CDs.
  - Acumulación de inventario en `InventarioCentroDistribucion` y registro histórico en `IngresoMercancia`.
  - Validaciones de negocio: cantidad > 0, origen obligatorio y existencia de CD y producto.
  - Pruebas unitarias para validaciones en `IngresoMercanciaServiceTest`.
  - Documentación de historia de usuario en `docs/Historias de Usuario/HU-18_Registrar_ingreso_mercancia_CD.md`.
- **Contratos DTO y Auditoría:**
  - Nuevos contratos JSON y tipos TypeScript en `docs/dto/` para inventario e ingresos de mercancía.
  - Informe técnico de auditoría y análisis de rendimiento N+1 en `docs/Fix´s/fix3-hu-12-18.md`.

### Fixed

- **Integración y Merge con Main:**
  - Resolución de colisiones críticas de fusión preservando la relación `@ManyToMany` en `CentroDistribucionJpaEntity` y `CentroDistribucionRepositoryAdapter`.
  - Unificación de métodos en `TiendaRepositoryPort` y `TiendaRepositoryAdapter` (`findAll()`, `obtenerPorEncargadoId()`, `obtenerPorId()`).
  - Mantenimiento de rutas estáticas públicas en `SecurityConfig.java`.

## 2026-09-20


### Added

- **HU-07 (Crear y Listar Tiendas):**
  - Implementación del puerto de entrada `ObtenerTiendasUseCase` y endpoint `GET /api/tiendas` para listar tiendas existentes.
  - Generación automática de código de negocio único `TND-XXXXXXXX` en `TiendaService`.
  - Pantalla web estática `tiendas.html` para registro de tiendas y visualización en tiempo real mediante tabla HTML.
- **HU-09 (Gestión de Centros de Distribución):**
  - Mapeo relacional `@ManyToMany` real entre `CentroDistribucionJpaEntity` y `TiendaJpaEntity` mediante la tabla intermedia `centro_distribucion_tiendas`.
  - Claves foráneas reales (`FOREIGN KEY`) en PostgreSQL (Supabase) garantizando integridad referencial entre centros y tiendas abastecidas.
  - Pantalla web estática `centros-distribucion.html` con carga dinámica de tiendas disponibles (`GET /api/tiendas`) para selección múltiple (zona de cobertura) y tabla de CDs registrados.
- **HU-01 & HU-29 (Usuarios y Autenticación):**
  - Tabla interactiva de usuarios en `admin.html` con consumo de `GET /api/usuarios` y funcionalidad de eliminación (`DELETE /api/usuarios/{id}`).
  - Guía de HU-29 para login por rol, redirección y restricciones futuras por ubicación.
  - Enum `Rol` (`ADMINISTRADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`, `PLANIFICADOR`).
  - Autenticación JWT con Supabase Auth para la API con validación de emisor, firma y expiración.
  - Restricción de creación de usuarios exclusivamente a administradores autenticados (`sub` claim).
  - Registro de auditoría del administrador que crea cada usuario (`creado_por`).
- **Documentación & Contratos:**
  - Informes técnicos y auditorías de código: `docs/Fix´s/fix1-hu-07-09.md` y `docs/Fix´s/fix2-hu-07-09.md`.
  - Contratos de API estructurados en `docs/dto/` con especificaciones JSON (request/response) e interfaces en TypeScript para integración frontend y Mock APIs.
  - Reorganización de historias de usuario en `docs/Historias de Usuario/`.

### Fixed

- **Arquitectura Hexagonal:**
  - Unificación de paquetes eliminando `com.example.optiway.infraestructure` (error tipográfico con `ae`) en favor del estándar `com.example.optiway.infrastructure`.
  - Reubicación y estandarización de `CentroDistribucionController` en `infrastructure/adapter/in/rest/`.
  - Reubicación de `CentroDistribucionJpaRepository` y `CentroDistribucionRepositoryAdapter` en `infrastructure/adapter/out/persistence/`.
  - Corrección de declaraciones de paquetes e imports en `TiendaJpaEntity`, `TiendaJpaRepository` y `TiendaRepositoryAdapter`.
- **Persistencia & Datos:**
  - Corrección en `TiendaRepositoryAdapter.save()` para mapear y retornar el `id` numérico autogenerado (`int8` de Supabase/PostgreSQL).
  - Conversión del almacenamiento de tiendas abastecidas de `@ElementCollection` a `@ManyToMany` con Foreign Keys reales.
- **Seguridad & Reglas de Negocio en Usuarios (HU-01):**
  - Protección contra autoeliminación: un administrador autenticado no puede eliminarse a sí mismo (`adminAuthUserId.equals(usuarioAEliminar.getAuthUserId())`).
  - Protección jerárquica: se prohíbe eliminar a cualquier usuario con rol `ADMINISTRADOR`.
  - Sincronización con Supabase Auth: al eliminar un usuario se elimina también su cuenta en `auth.users` mediante la Admin API (`DELETE /auth/v1/admin/users/{userId}`) antes de removerlo de la base de datos local.
  - Refactorización a `AuthPort`: reemplazo de `InvitarUsuarioPort` por [AuthPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/AuthPort.java) para desacoplar y tipar formalmente las operaciones administrativas en Supabase Auth (invitación y borrado), diferenciándolo del login de clientes.
  - En `admin.html`, los usuarios con rol `ADMINISTRADOR` muestran el botón eliminar deshabilitado y se capturan los mensajes de error retornados por el backend.
- **Git & Configuración:**
  - Resolución de marcadores de conflicto Git residuales en `src/main/resources/application.properties`.
  - Centralización y protección de credenciales sensibles mediante variables de entorno (`${DB_PASSWORD}`, `${SUPABASE_SERVICE_ROLE_KEY}`).
  - Eliminación de archivos comprimidos binarios del repositorio e inclusión de regla `*.zip` en `.gitignore`.

### Changed

- `SecurityConfig.java` actualizado para permitir el acceso público a las páginas HTML de prueba estáticas (`/tiendas.html`, `/centros-distribucion.html`, `/admin.html`).
- Las vistas estáticas envían el token Bearer y usan selectores dinámicos en lugar de campos de IDs manuales.

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
