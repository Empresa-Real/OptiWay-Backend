# Changelog

## 2026-09-22

### Changed

- **Unificación de Inventarios (`feat: unificar inventarios` - Commit `c1fda48`):**
  - **Fusión del Modelo de Dominio y Persistencia**: Eliminación de la entidad redundante `InventarioCentroDistribucion` y sus componentes técnicos asociados (`InventarioCentroDistribucionJpaEntity`, `InventarioCentroDistribucionJpaRepository`, `InventarioCentroDistribucionRepositoryAdapter` y el puerto `InventarioCentroDistribucionRepositoryPort`), consolidando todo el control de existencias en el modelo único `Inventario` y la entidad `InventarioJpaEntity`.
  - **Regla Invariante de Exclusividad de Ubicación (XOR)**:
    - En el dominio: Implementación del método `validarUbicacionExclusiva()` en `Inventario`, lanzando `IllegalArgumentException` si un registro intenta asociarse simultáneamente a una tienda y a un centro de distribución, o si no cuenta con ninguna de las dos ubicaciones.
    - En la base de datos: Validación con hooks `@PrePersist` y `@PreUpdate` en `InventarioJpaEntity` que garantizan la integridad relacional previa al guardado en PostgreSQL.
  - **Consolidación en Puertos y Adaptadores**: Incorporación del método `findByCentroDistribucionIdAndProductoId(...)` en `InventarioRepositoryPort` y su implementación en `InventarioRepositoryAdapter`.
  - **Refactorización Transaccional en `IngresoMercanciaService`**: Actualización de la lógica de negocio para buscar, inicializar o acumular existencias directamente sobre el puerto unificado de `Inventario`.
  - **Cobertura de Pruebas**:
    - Creación de la suite de pruebas `InventarioExclusividadTest` para validar los escenarios válidos e inválidos de ubicación.
    - Actualización integral de `IngresoMercanciaServiceTest` para verificar el flujo de recepción de mercancía sobre el repositorio unificado.

### Added

- **HU-29 (Iniciar Sesión según Rol Asignado y Control por Ubicación - Fix 5):**
  - **Redirección por Rol en Frontend**: Enrutamiento automático inmediato tras autenticación según el rol resuelto (`ADMINISTRADOR` $\rightarrow$ `admin.html`, `PLANIFICADOR` $\rightarrow$ `main.html`, `ENCARGADO_TIENDA` $\rightarrow$ `inventario.html`, `ENCARGADO_CD` $\rightarrow$ `ingreso-mercancia.html`).
  - **Componente Centralizado `nav.js`**: Abstracción de sesión con Supabase (`getToken()`), validación de acceso (`rolesPermitidos`), alertas de acceso denegado y renderizado dinámico de enlaces de navegación por rol.
  - **Aislamiento Estricto por Ubicación (Backend y Frontend)**:
    - En `TiendaService`: `GET /api/tiendas` filtra por `encargadoId == usuario.id` para `ENCARGADO_TIENDA`.
    - En `CentroDistribucionService`: `GET /api/centros-distribucion` filtra por `encargadoId == usuario.id` para `ENCARGADO_CD`.
    - En `InventarioService`: `GET /api/inventario` rechaza consultas a tiendas no asignadas (`403 Forbidden`).
    - En `IngresoMercanciaService`: `POST /api/ingresos-mercancia` rechaza ingresos a CDs no asignados (`403 Forbidden`).
  - **Protección y Restricciones para Planificador**:
    - Backend prohíbe `GET /api/usuarios`, `POST /api/usuarios`, `DELETE /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/rol`, `POST /api/tiendas`, `PUT /api/tiendas/{id}/encargado`, `POST /api/centros-distribucion` y `PUT /api/centros-distribucion/{id}/encargado` con `403 Forbidden`.
    - Frontend oculta los formularios de creación de tiendas y CDs para usuarios no administradores.
  - **Gestión del Administrador**:
    - Casos de uso `AsignarEncargadoTiendaUseCase` (`PUT /api/tiendas/{id}/encargado`) y `AsignarEncargadoCDUseCase` (`PUT /api/centros-distribucion/{id}/encargado`).
    - Caso de uso `ActualizarRolUsuarioUseCase` (`PATCH /api/usuarios/{id}/rol`).
    - Panel interactivo en `admin.html` con visualización de múltiples sedes asignadas con viñetas y asignación interactiva.
  - **Resiliencia en Supabase Auth**:
    - Corrección del parámetro query `redirect_to` en `/auth/v1/invite`.
    - Fallback en `SupabaseAuthAdapter` para reutilizar cuentas preexistentes en Supabase Auth ante códigos `429 (over_email_send_rate_limit)` o `422`.

### Fixed

- **Cierre de Sesión en Admin**: Identificador `<p id="nav-bar">` restaurado y escucha incondicional del botón `#logout` en `nav.js`.
- **Visibilidad Múltiple de Ubicaciones**: Reemplazo de `.find()` por `.filter()` en `admin.html` permitiendo listar y gestionar usuarios con 2 o más sedes asignadas.

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
