# Informe de Correcciones e Implementación: HU-29 — Iniciar Sesión y Navegación según Rol Asignado (Fix 5)

Este documento detalla las soluciones técnicas, adaptaciones en la arquitectura hexagonal del backend y centralización del frontend implementadas para dar cumplimiento integral a la **HU-29** y garantizar el aislamiento por rol y ubicación en el sistema OptiWay.

---

## 1. Resumen Ejecutivo y Alcance

La **HU-29** establece que cualquier usuario, al iniciar sesión con credenciales válidas:
1. Debe ser redirigido inmediatamente al panel operativo correspondiente a su rol.
2. Un usuario con rol Encargado (`ENCARGADO_TIENDA` o `ENCARGADO_CD`) no debe acceder ni visualizar información de ubicaciones que no tenga asignadas.
3. El `ADMINISTRADOR` debe contar con la capacidad de asignar, reasignar o desasignar encargados de Tiendas y Centros de Distribución, así como modificar roles de usuario desde su panel.
4. El `PLANIFICADOR` puede acceder y visualizar la información operativa de la red logística (inventarios, ingresos, tiendas y CDs), pero tiene restringida la administración de usuarios.

---

## 2. Brechas Detectadas Previas al Fix

| Componente | Brecha Identificada | Impacto |
| :--- | :--- | :--- |
| **Modelo CD** | `CentroDistribucion` carecía del atributo `encargadoId` tanto en dominio como en persistencia JPA. | Imposible asociar un responsable a un Centro de Distribución. |
| **Casos de Uso** | No existían casos de uso ni endpoints para asignar o reasignar encargados a Tiendas o CDs. | El administrador no podía gestionar la asignación de responsables. |
| **Gestión de Roles** | No existía endpoint para modificar el rol de un usuario existente (`PATCH /api/usuarios/{id}/rol`). | Cambiar el rol requería intervención manual directa en la base de datos. |
| **Frontend / Navbar** | Código de sesión y lógica de manipulación de DOM disperso y repetido en cada vista HTML. | Riesgo de inconsistencias de navegación y fuga de enlaces restringidos. |
| **Aislamiento por Ubicación** | Los paneles de inventario e ingreso mostraban desplegables con todas las ubicaciones sin filtrar por usuario. | Los encargados podían consultar y alterar inventarios de sedes ajenas. |
| **Invitación Supabase** | `redirect_to` se enviaba en el body JSON en vez de query param a la API de GoTrue (`/auth/v1/invite`). | El enlace del correo redirigía a la raíz (`SITE_URL`) sin abrir `set-password.html`. |

---

## 3. Remediación en el Backend (Arquitectura Hexagonal)

### A. Modelo de Dominio y Persistencia de Centro de Distribución
Se incorporó el atributo `encargadoId` (referencia a `usuarios.id`):
- **Dominio**: [CentroDistribucion.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/CentroDistribucion.java).
- **Entidad JPA**: [CentroDistribucionJpaEntity.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/CentroDistribucionJpaEntity.java) con columna `encargado_id`.
- **Adaptador de Persistencia**: [CentroDistribucionRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/CentroDistribucionRepositoryAdapter.java).

### B. Nuevos Casos de Uso y Servicios
1. **Asignación de Encargado de Tienda**:
   - Puerto de Entrada: `AsignarEncargadoTiendaUseCase.java`.
   - Servicio: [TiendaService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/TiendaService.java).
   - Valida que el usuario exista y posea el rol `ENCARGADO_TIENDA` (o permite desasignar con `null`).
2. **Asignación de Encargado de Centro de Distribución**:
   - Puerto de Entrada: `AsignarEncargadoCDUseCase.java`.
   - Servicio: [CentroDistribucionService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/CentroDistribucionService.java).
   - Valida que el usuario exista y posea el rol `ENCARGADO_CD` (o permite desasignar con `null`).
3. **Actualización de Rol de Usuario**:
   - Puerto de Entrada: `ActualizarRolUsuarioUseCase.java`.
   - Servicio: [UsuarioService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/UsuarioService.java).
   - Valida que quien ejecuta la acción sea `ADMINISTRADOR` y no permita degradarse a sí mismo si es el único admin.

### C. Endpoints REST Implementados
- `PUT /api/tiendas/{id}/encargado` en [TiendaController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/TiendaController.java)
- `PUT /api/centros-distribucion/{id}/encargado` en [CentroDistribucionController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/CentroDistribucionController.java)
- `PATCH /api/usuarios/{id}/rol` en [UsuarioController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/UsuarioController.java)

### D. Control de Acceso y Aislamiento de Datos en el Backend (Capa de Servicio)
Para garantizar que las reglas no dependan exclusivamente del frontend, se incorporó validación basada en la identidad del JWT (`authUserId`):
1. **Consulta de Tiendas (`GET /api/tiendas`)**:
   - `ADMINISTRADOR` y `PLANIFICADOR`: reciben el listado global.
   - `ENCARGADO_TIENDA`: el backend filtra en memoria/BD retornando **únicamente** las tiendas donde `encargadoId == usuario.getId()`.
2. **Consulta de Centros de Distribución (`GET /api/centros-distribucion`)**:
   - `ADMINISTRADOR` y `PLANIFICADOR`: reciben el listado global.
   - `ENCARGADO_CD`: el backend filtra retornando **únicamente** los CDs donde `encargadoId == usuario.getId()`.
3. **Consulta de Usuarios (`GET /api/usuarios`)**:
   - Solo permitido para `ADMINISTRADOR`. Si un `PLANIFICADOR` u otro rol envía una petición a `/api/usuarios`, el backend responde **`403 Forbidden` (`AccesoDenegadoException`)**.
4. **Creación y Asignación de Tiendas y CDs**:
   - `POST /api/tiendas`, `PUT /api/tiendas/{id}/encargado`, `POST /api/centros-distribucion` y `PUT /api/centros-distribucion/{id}/encargado` validan estrictamente que el rol sea `ADMINISTRADOR`. El `PLANIFICADOR` tiene prohibida la creación y alteración de sedes en backend.
5. **Consulta de Inventario de Tienda (`GET /api/inventario`)**:
   - Permite acceso a `ADMINISTRADOR` y `PLANIFICADOR`.
   - Para `ENCARGADO_TIENDA`, valida `usuario.getId().equals(tienda.getEncargadoId())`. Si intenta consultar otra tienda, responde `403 Forbidden`.
6. **Ingreso de Mercancía en CD (`POST /api/ingresos-mercancia`)**:
   - Para `ENCARGADO_CD`, valida `usuario.getId().equals(centro.getEncargadoId())`. Si intenta ingresar mercancía en un CD no asignado, responde `403 Forbidden`.

### E. Corrección en Invitaciones y Fallback de Supabase
En [SupabaseAuthAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/auth/SupabaseAuthAdapter.java):
- Se adaptó la URL agregando el query parameter `redirect_to`.
- Se implementó un fallback que consulta a la Admin API de Supabase (`GET /auth/v1/admin/users`) si el envío de correo arroja `429` (rate limit) o `422`, asociando de inmediato al usuario preexistente a la BD local.

---

## 4. Remediación en el Frontend

### A. Componente Compartido `nav.js`
Se creó [nav.js](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/nav.js) exponiendo `window.OptiWay`:
- **`getToken()`**: Centraliza la extracción del token JWT de sesión con redirección inmediata al login si expiró.
- **`initNav({ rolesPermitidos })`**:
  - Consulta `/api/usuarios/me`.
  - Verifica si el rol actual está autorizado para la página. En caso negativo, emite alerta `[Acceso Denegado]` y redirige al panel natural del usuario.
  - Renderiza dinámicamente en el `<p id="nav-bar">` los enlaces autorizados para ese rol.
  - Conecta el evento de cierre de sesión (`signOut`).

### B. Matriz de Navegación por Rol

| Rol | Panel de Inicio | Enlaces Visibles en Navbar | Vistas Permitidas |
| :--- | :--- | :--- | :--- |
| **`ADMINISTRADOR`** | `/admin.html` | Inicio, Usuarios, Tiendas, Centros, Inventario, Ingreso CD | Todas |
| **`PLANIFICADOR`** | `/main.html` | Inicio, Tiendas, Centros, Inventario, Ingreso CD | Todas excepto `admin.html` |
| **`ENCARGADO_TIENDA`**| `/inventario.html` | Inicio, Inventario Tienda | `/main.html`, `/inventario.html` |
| **`ENCARGADO_CD`** | `/ingreso-mercancia.html` | Inicio, Centros, Ingreso Mercancía CD | `/main.html`, `/centros-distribucion.html`, `/ingreso-mercancia.html` |

### C. Aislamiento Estricto y Soporte de Múltiples Ubicaciones en Vistas Operativas
1. **[admin.html](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/admin.html)**:
   - Identifica y lista **todas** las tiendas o Centros de Distribución asignados a un usuario (`filter` en lugar de `find`), presentándolos con viñetas claras por línea (`• Tienda: ...` o `• CD: ...`).
   - Botón interactivo de gestión con contador `Gestionar Tiendas (N)` / `Gestionar CD (N)`.
   - Diálogo interactivo que muestra las sedes asignadas, disponibles y asignadas a otros, permitiendo asignar una nueva sede, desasignar una existente (toggle) o ingresar `0` para desasignar todas en lote.
2. **[inventario.html](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/inventario.html)**:
   - Si el usuario tiene 0 tiendas asignadas: bloquea el selector, inhabilita la creación de productos y alerta en la tabla.
   - Si tiene 1 tienda asignada: la selecciona automáticamente y bloquea el selector.
   - Si tiene 2 o más tiendas asignadas: puebla el selector exclusivamente con sus tiendas autorizadas, permitiéndole alternar únicamente entre ellas.
3. **[ingreso-mercancia.html](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/ingreso-mercancia.html)**:
   - Si el usuario tiene 0 CDs asignados: bloquea el formulario y selector notificando la falta de asignación.
   - Si tiene 1 CD asignado: lo selecciona por defecto y bloquea el selector.
   - Si tiene 2 o más CDs asignados: puebla el selector únicamente con los CDs autorizados para su usuario.

---

## 5. Verificación de Compilación y Estado
- **Compilación Maven**: `mvnw.cmd test-compile` finalizado exitosamente (`BUILD SUCCESS`).
- **Compatibilidad**: Se mantuvo la restricción de diseño estricta: HTML plano sin librerías de estilos adicionales, etiquetas monocromáticas (`[OK]`, `[Acceso Denegado]`, `[Sin Asignar]`).
