# Guía de continuación: HU-02 — Asignar (o Reasignar) Tienda o Centro de Distribución a un Usuario

## Historia de Usuario

> **Como** Administrador del sistema
> **Quiero** asignar una tienda o un centro de distribución a un usuario con rol de Encargado, y poder reasignarlo más adelante a otra ubicación
> **Para** que esa persona siempre opere solo sobre la(s) ubicación(es) que le corresponde(n).


---

## Criterios de Aceptación

1. **Autorización de la operación:** Solo un usuario con rol `ADMINISTRADOR` puede asignar o reasignar ubicaciones. Cualquier otro rol que intente la operación recibe `403 Forbidden`.
2. **Coincidencia de tipo (rol ↔ ubicación):** El sistema valida que el `tipoPuntoOperacion` de la ubicación coincida con el rol del usuario (`ENCARGADO_TIENDA` → `TIENDA`; `ENCARGADO_CD` → `CENTRO_DISTRIBUCION`). Si no coincide, se rechaza con error explícito (`400 Bad Request`).
3. **Asignación múltiple permitida:** Un usuario puede tener **más de una** ubicación activa asignada simultáneamente (siempre que sean del tipo correcto para su rol). La operación de asignar **agrega** una nueva ubicación sin afectar las existentes.
4. **Reasignación explícita:** La reasignación es una operación distinta a la asignación: mueve al usuario de **una ubicación específica** ya asignada hacia **otra nueva**. Al ejecutarse:
   * Se **revoca de inmediato** el acceso a la ubicación anterior (se marca como `REVOCADA`, con `fechaRevocacion`).
   * Se crea la nueva asignación en estado `ACTIVA`.
   * El **historial de acciones** del usuario en la ubicación anterior **se conserva**, asociado a su nombre (no se elimina ni se anonimiza).
5. **Visibilidad automática en login:** El conjunto de ubicaciones activas de un usuario debe quedar disponible para ser consultado y mostrado automáticamente al iniciar sesión, sin requerir selección manual por parte del usuario.
6. **Validaciones de existencia:** El sistema valida que el `usuarioId` exista y esté activo, y que el `puntoOperacionId` exista, antes de asignar o reasignar.
7. **Registro de auditoría:** Cada asignación y cada revocación queda registrada con `asignadoPor` (ID del administrador que ejecuta la acción), `fechaAsignacion` y, en caso de reasignación, `fechaRevocacion` de la asignación anterior.
8. **Respuesta enriquecida:** La respuesta incluye datos del usuario (`nombreUsuario`, `rol`) y de la ubicación (`nombrePuntoOperacion`, `tipoPuntoOperacion`, `ciudad`).

---

## Especificación de Endpoints

Se definen dos operaciones diferenciadas, alineadas con las reglas de negocio: **asignar** (agrega una ubicación nueva, permite múltiples) y **reasignar** (mueve de una ubicación específica a otra, revocando la anterior).

### 1. Asignar una nueva ubicación a un usuario

* **Método:** `POST`
* **Ruta:** `/api/usuarios/{usuarioId}/asignaciones`
* **Headers requeridos:**
  ```http
  Authorization: Bearer <token_jwt_supabase>
  Content-Type: application/json
  ```
* **Path Parameters:**

| Parámetro | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `usuarioId` | `Long` | **Sí** | ID del usuario Encargado al que se asignará la ubicación. |

* **Cuerpo de la petición (Request Body):**

```json
{
  "puntoOperacionId": 4,
  "tipoPuntoOperacion": "TIENDA"
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `puntoOperacionId` | `Long` | **Sí** | ID de la tienda o centro de distribución. |
| `tipoPuntoOperacion` | `String` (`TIENDA` \| `CENTRO_DISTRIBUCION`) | **Sí** | Tipo de ubicación a asignar. |

#### Ejemplo de petición

```http
POST /api/usuarios/57/asignaciones HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{
  "puntoOperacionId": 4,
  "tipoPuntoOperacion": "TIENDA"
}
```

#### Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`201 Created`** | Asignación creada correctamente (se agrega a las ubicaciones activas del usuario). | Objeto JSON con la nueva asignación. |
| **`400 Bad Request`** | `tipoPuntoOperacion` no coincide con el rol del usuario (p. ej. se intenta asignar un CD a un `ENCARGADO_TIENDA`), campos faltantes o formato inválido. | `{"error": "El tipo de ubicación no corresponde al rol del usuario"}` |
| **`401 Unauthorized`** | Token JWT no suministrado, vencido o con firma inválida. | Error de autenticación OAuth2/JWT. |
| **`403 Forbidden`** | El usuario autenticado no tiene rol `ADMINISTRADOR`. | `{"error": "No tiene permisos para asignar ubicaciones"}` |
| **`404 Not Found`** | El `usuarioId` o el `puntoOperacionId` no existen. | `{"error": "El usuario no existe"}` o `{"error": "El punto de operación no existe"}` |
| **`409 Conflict`** | El usuario ya tiene esa misma ubicación asignada y activa. | `{"error": "El usuario ya tiene asignada esta ubicación"}` |

#### Ejemplo de respuesta exitosa (`201 Created`)

```json
{
  "id": 301,
  "usuarioId": 57,
  "nombreUsuario": "Laura Gómez",
  "rol": "ENCARGADO_TIENDA",
  "puntoOperacionId": 4,
  "nombrePuntoOperacion": "Tienda Norte",
  "tipoPuntoOperacion": "TIENDA",
  "ciudad": "Medellín",
  "estado": "ACTIVA",
  "asignadoPor": 1,
  "fechaAsignacion": "2026-09-21T14:32:00Z",
  "fechaRevocacion": null
}
```

---

### 2. Reasignar una ubicación existente a otra

* **Método:** `PUT`
* **Ruta:** `/api/usuarios/{usuarioId}/asignaciones/{asignacionId}/reasignar`
* **Headers requeridos:**
  ```http
  Authorization: Bearer <token_jwt_supabase>
  Content-Type: application/json
  ```
* **Path Parameters:**

| Parámetro | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `usuarioId` | `Long` | **Sí** | ID del usuario Encargado. |
| `asignacionId` | `Long` | **Sí** | ID de la asignación activa que se va a mover a otra ubicación. |

* **Cuerpo de la petición (Request Body):**

```json
{
  "nuevoPuntoOperacionId": 9
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `nuevoPuntoOperacionId` | `Long` | **Sí** | ID de la nueva ubicación (debe ser del mismo tipo que la asignación original). |

#### Ejemplo de petición

```http
PUT /api/usuarios/57/asignaciones/301/reasignar HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{
  "nuevoPuntoOperacionId": 9
}
```

#### Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Reasignación exitosa: la asignación anterior queda `REVOCADA` (acceso revocado de inmediato, historial conservado) y se crea una nueva `ACTIVA`. | Objeto JSON con la nueva asignación. |
| **`400 Bad Request`** | La nueva ubicación es de un tipo distinto al de la asignación original, o el `nuevoPuntoOperacionId` es inválido. | `{"error": "La nueva ubicación debe ser del mismo tipo que la asignación actual"}` |
| **`401 Unauthorized`** | Token JWT no suministrado, vencido o con firma inválida. | Error de autenticación OAuth2/JWT. |
| **`403 Forbidden`** | El usuario autenticado no tiene rol `ADMINISTRADOR`. | `{"error": "No tiene permisos para reasignar ubicaciones"}` |
| **`404 Not Found`** | El `usuarioId`, la `asignacionId` o el `nuevoPuntoOperacionId` no existen. | `{"error": "La asignación no existe"}` o `{"error": "El punto de operación no existe"}` |
| **`409 Conflict`** | La `asignacionId` indicada ya está `REVOCADA` (no se puede reasignar algo ya inactivo). | `{"error": "La asignación ya no está activa"}` |

#### Ejemplo de respuesta exitosa (`200 OK`)

```json
{
  "id": 305,
  "usuarioId": 57,
  "nombreUsuario": "Laura Gómez",
  "rol": "ENCARGADO_TIENDA",
  "puntoOperacionId": 9,
  "nombrePuntoOperacion": "Tienda Sur",
  "tipoPuntoOperacion": "TIENDA",
  "ciudad": "Medellín",
  "estado": "ACTIVA",
  "asignadoPor": 1,
  "fechaAsignacion": "2026-09-21T15:10:00Z",
  "fechaRevocacion": null,
  "asignacionAnteriorId": 301
}
```

> La asignación `301` queda persistida con `estado: "REVOCADA"` y `fechaRevocacion: "2026-09-21T15:10:00Z"`, conservando el historial de acciones que Laura ejecutó en "Tienda Norte" asociado a su nombre.

---

### Nota: Visibilidad en login (criterio 5)

Este criterio no requiere un endpoint nuevo dentro del alcance de HU-02, sino que las asignaciones creadas aquí deben quedar disponibles para el flujo de autenticación/login (p. ej. `GET /api/usuarios/{usuarioId}/asignaciones?estado=ACTIVA`), de modo que el frontend cargue automáticamente todas las ubicaciones activas del usuario sin pedirle que seleccione una.

---

## Flujo en Arquitectura Hexagonal

### Asignar

```text
Cliente (Frontend / Administrador)
  ↓ [POST /api/usuarios/{usuarioId}/asignaciones] + Bearer JWT + Body
SecurityFilterChain (Spring Security valida el JWT de Supabase)
  ↓
AsignacionController (infrastructure/adapter/in/rest/)
  ↓ jwt.getSubject() (UUID authUserId) + usuarioId (path) + body (puntoOperacionId, tipoPuntoOperacion)
AsignarUbicacionUseCase (application/port/in/)
  ↓
AsignacionService (application/service/)
  ├── Valida rol ADMINISTRADOR del solicitante (UsuarioRepositoryPort)
  ├── Valida existencia y estado activo del usuario destino (UsuarioRepositoryPort)
  ├── Valida existencia del punto de operación (PuntoOperacionRepositoryPort)
  ├── Valida coincidencia rol del usuario ↔ tipoPuntoOperacion
  ├── Verifica que no exista ya esa misma asignación activa (AsignacionRepositoryPort)
  ├── Crea nueva asignación (estado = ACTIVA, asignadoPor, fechaAsignacion)
  └── Enriquece respuesta con datos de usuario y ubicación (UsuarioRepositoryPort, PuntoOperacionRepositoryPort)
```

### Reasignar

```text
Cliente (Frontend / Administrador)
  ↓ [PUT /api/usuarios/{usuarioId}/asignaciones/{asignacionId}/reasignar] + Bearer JWT + Body
SecurityFilterChain (Spring Security valida el JWT de Supabase)
  ↓
AsignacionController (infrastructure/adapter/in/rest/)
  ↓ jwt.getSubject() (UUID authUserId) + usuarioId, asignacionId (path) + nuevoPuntoOperacionId (body)
ReasignarUbicacionUseCase (application/port/in/)
  ↓
AsignacionService (application/service/)
  ├── Valida rol ADMINISTRADOR del solicitante (UsuarioRepositoryPort)
  ├── Obtiene la asignación actual y valida que esté ACTIVA (AsignacionRepositoryPort)
  ├── Valida existencia del nuevo punto de operación (PuntoOperacionRepositoryPort)
  ├── Valida que el tipo del nuevo punto coincida con el de la asignación original
  ├── Revoca la asignación actual (estado = REVOCADA, fechaRevocacion) — sin borrar historial de acciones
  ├── Crea nueva asignación (estado = ACTIVA, asignadoPor, fechaAsignacion, asignacionAnteriorId)
  └── Enriquece respuesta con datos de usuario y ubicación (UsuarioRepositoryPort, PuntoOperacionRepositoryPort)
```
