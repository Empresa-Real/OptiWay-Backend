# DTOs y Contratos de API para Frontend & Mock API

Esta carpeta contiene los esquemas y ejemplos JSON de **Request** y **Response** para que el equipo de frontend configure sus Mock APIs (MSW, MirageJS, json-server, Postman Mock Server) mientras los servicios del backend se integran.

> Los contratos reflejan los DTOs actuales del backend. La fuente de verdad de los endpoints es Swagger/OpenAPI (`/swagger-ui.html`).

---

## Estructura de carpetas

```text
docs/dto/
├── README.md
├── request/
│   ├── actualizar-rol.json
│   ├── asignar-encargado.json
│   ├── cambiar-estado-producto.json
│   ├── crear-centro-distribucion.json
│   ├── crear-producto.json
│   ├── crear-tienda.json
│   ├── crear-usuario.json
│   ├── editar-producto.json
│   ├── registrar-ingreso-mercancia.json
│   └── registrar-venta.json
└── response/
    ├── consultar-inventario-200.json
    ├── consultar-inventario-403.json
    ├── consultar-inventario-cd-200.json
    ├── crear-centro-distribucion-201.json
    ├── crear-producto-201.json
    ├── crear-tienda-201.json
    ├── crear-tienda-400.json
    ├── crear-usuario-201.json
    ├── editar-producto-200.json
    ├── listar-productos-200.json
    ├── listar-tiendas-200.json
    ├── listar-usuarios-200.json
    ├── registrar-ingreso-mercancia-201.json
    ├── registrar-venta-201.json
    └── tiene-stock-200.json
```

---

## 1. Usuarios (`/api/usuarios`) — HU-01

### Request Crear Usuario (`POST /api/usuarios`)
* **Archivo:** [`docs/dto/request/crear-usuario.json`](./request/crear-usuario.json)
* **Roles:** `ADMINISTRADOR`.

```json
{
  "nombre": "Ana Gonzalez",
  "email": "ana@empresa.com",
  "rol": "ADMINISTRADOR"
}
```

### Response 201 Created / Detalle (`GET /api/usuarios/{id}`, `GET /api/usuarios/me`)
* **Archivo:** [`docs/dto/response/crear-usuario-201.json`](./response/crear-usuario-201.json)

```json
{
  "id": 1,
  "authUserId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "nombre": "Ana Gonzalez",
  "email": "ana@empresa.com",
  "rol": "ADMINISTRADOR",
  "creadoPor": "a1b2c3d4-e5f6-7890-abcd-1234567890ab"
}
```

> `creadoPor` es el `authUserId` del administrador que creó al usuario y puede ser `null`.

### Request Actualizar Rol (`PATCH /api/usuarios/{id}/rol`)
* **Archivo:** [`docs/dto/request/actualizar-rol.json`](./request/actualizar-rol.json)
* **Roles:** `ADMINISTRADOR`. No se puede cambiar el rol de otro administrador ni otorgar `ADMINISTRADOR`.

```json
{
  "rol": "ENCARGADO_CD"
}
```

La respuesta usa el mismo esquema que `crear-usuario-201.json`.

### Response Listar Usuarios (`GET /api/usuarios`)
* **Archivo:** [`docs/dto/response/listar-usuarios-200.json`](./response/listar-usuarios-200.json)
* **Roles:** `ADMINISTRADOR`.

### Otros endpoints
* `GET /api/usuarios/{id}`: un `ADMINISTRADOR` puede consultar a cualquiera; el resto solo su propio perfil.
* `GET /api/usuarios/me`: perfil del usuario autenticado.
* `DELETE /api/usuarios/{id}`: `204 No Content`. Requiere `ADMINISTRADOR`; no permite eliminarse a sí mismo ni a otro administrador.

### TypeScript Interfaces
```typescript
export type RolUsuario =
  | "ADMINISTRADOR"
  | "ENCARGADO_TIENDA"
  | "ENCARGADO_CD"
  | "PLANIFICADOR";

export interface CrearUsuarioRequest {
  nombre: string;
  email: string;
  rol: RolUsuario;
}

export interface ActualizarRolRequest {
  rol: RolUsuario;
}

export interface AsignacionUbicacion {
  tipo: "TIENDA" | "CD";
  id: number; // id de la tienda o del CD
  nombre: string; // nombre de la ubicación
}

export interface UsuarioResponse {
  id: number;
  authUserId: string; // UUID de Supabase Auth
  nombre: string;
  email: string;
  rol: RolUsuario;
  creadoPor: string | null; // authUserId del administrador creador
  asignacion: AsignacionUbicacion | null; // siempre presente: objeto o null
}
```

---

## 2. Tiendas (`/api/tiendas`) — HU-07 y HU-11

### Request Crear Tienda (`POST /api/tiendas`)
* **Archivo:** [`docs/dto/request/crear-tienda.json`](./request/crear-tienda.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.
* `encargadoId` es opcional y debe corresponder a un usuario `ENCARGADO_TIENDA`.

```json
{
  "nombre": "Tienda Central Medellín",
  "direccion": "Calle 50 # 45-10",
  "ciudad": "Medellín",
  "encargadoId": 3
}
```

### Response 201 Created (`POST /api/tiendas`)
* **Archivo:** [`docs/dto/response/crear-tienda-201.json`](./response/crear-tienda-201.json)

```json
{
  "id": 1,
  "codigo": "TND-A1B2C3D4",
  "nombre": "Tienda Central Medellín",
  "direccion": "Calle 50 # 45-10",
  "ciudad": "Medellín",
  "activo": true,
  "estado": "Activa",
  "encargadoId": 3,
  "centrosAbastecedoresIds": [1, 2]
}
```

### Response Listar Tiendas (`GET /api/tiendas`)
* **Archivo:** [`docs/dto/response/listar-tiendas-200.json`](./response/listar-tiendas-200.json)
* Cualquier usuario autenticado. Los encargados de tienda solo ven su sede.

### Request Asignar Encargado (`PUT /api/tiendas/{id}/encargado`)
* **Archivo:** [`docs/dto/request/asignar-encargado.json`](./request/asignar-encargado.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.
* El body es opcional: sin body o con `encargadoId: null` se desasigna el encargado.
* El usuario asignado debe tener rol `ENCARGADO_TIENDA`.

```json
{
  "encargadoId": 3
}
```

La respuesta usa el esquema de `crear-tienda-201.json`.

### Request Registrar Venta (`POST /api/tiendas/{id}/ventas`)
* **Archivo:** [`docs/dto/request/registrar-venta.json`](./request/registrar-venta.json)
* **Roles:** `ADMINISTRADOR`, `PLANIFICADOR` o el encargado asignado a la tienda.
* **Parámetro de ruta:** `id` — tienda donde ocurre la venta (no viaja en el body).

```json
{
  "productoId": 5,
  "cantidad": 3
}
```

`fechaHora` es opcional; si se omite, se usa la hora del servidor.

### Response 201 Created (`POST /api/tiendas/{id}/ventas`)
* **Archivo:** [`docs/dto/response/registrar-venta-201.json`](./response/registrar-venta-201.json)

```json
{
  "id": 1,
  "tiendaId": 1,
  "productoId": 5,
  "cantidad": 3,
  "fechaHora": "2026-10-18T10:30:00"
}
```

### Errores frecuentes
* `403 Forbidden`: el encargado autenticado no está asignado a esa tienda.
* `400 Bad Request`: producto inactivo, sin existencias en la tienda o stock insuficiente.

### TypeScript Interfaces
```typescript
export interface CrearTiendaRequest {
  nombre: string;
  direccion: string;
  ciudad: string;
  encargadoId?: number | null;
}

export interface AsignarEncargadoRequest {
  encargadoId?: number | null;
}

export interface TiendaResponse {
  id: number;
  codigo: string;
  nombre: string;
  direccion: string;
  ciudad: string;
  activo: boolean;
  estado: "Activa" | "Inactiva";
  encargadoId: number | null;
  centrosAbastecedoresIds: number[];
}

export interface RegistrarVentaRequest {
  productoId: number;
  cantidad: number;
  fechaHora?: string; // ISO-8601; si se omite, usa la hora del servidor
}

export interface VentaResponse {
  id: number;
  tiendaId: number;
  productoId: number;
  cantidad: number;
  fechaHora: string;
}
```

---

## 3. Centros de Distribución (`/api/centros-distribucion`) — HU-09 y HU-18

### Request Crear CD (`POST /api/centros-distribucion`)
* **Archivo:** [`docs/dto/request/crear-centro-distribucion.json`](./request/crear-centro-distribucion.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.
* `encargadoId` es opcional y debe corresponder a un usuario `ENCARGADO_CD`.

```json
{
  "nombre": "CD Principal Valle de Aburrá",
  "direccion": "Autopista Sur # 65-120, Itagüí",
  "capacidad": 15000,
  "encargadoId": 4,
  "tiendasAbastecidasIds": [1, 2, 3]
}
```

### Response 201 Created
* **Archivo:** [`docs/dto/response/crear-centro-distribucion-201.json`](./response/crear-centro-distribucion-201.json)

```json
{
  "id": 1,
  "codigo": "CD-001",
  "nombre": "CD Principal Valle de Aburrá",
  "direccion": "Autopista Sur # 65-120, Itagüí",
  "capacidad": 15000,
  "encargadoId": 4,
  "tiendasAbastecidasIds": [1, 2, 3]
}
```

### Response Listar CDs (`GET /api/centros-distribucion`)
* Cualquier usuario autenticado. Los encargados de CD solo ven su sede.

### Request Asignar Encargado (`PUT /api/centros-distribucion/{id}/encargado`)
* **Archivo:** [`docs/dto/request/asignar-encargado.json`](./request/asignar-encargado.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.
* El body es opcional: sin body o con `encargadoId: null` se desasigna el encargado.
* El usuario asignado debe tener rol `ENCARGADO_CD`.

```json
{
  "encargadoId": 4
}
```

La respuesta usa el esquema de `crear-centro-distribucion-201.json`.

### Request Registrar Ingreso (`POST /api/centros-distribucion/{id}/ingresos`)
* **Alias corto:** `POST /api/cd/{id}/ingresos` (misma operación y respuesta).
* **Archivo:** [`docs/dto/request/registrar-ingreso-mercancia.json`](./request/registrar-ingreso-mercancia.json)
* **Roles:** `ADMINISTRADOR`, `PLANIFICADOR` o el encargado asignado al CD.
* **Parámetro de ruta:** `id` — centro de distribución receptor (no viaja en el body).

```json
{
  "productoId": 10,
  "cantidad": 250,
  "origen": "Proveedor Alimentos del Valle S.A."
}
```

### Response 201 Created
* **Archivo:** [`docs/dto/response/registrar-ingreso-mercancia-201.json`](./response/registrar-ingreso-mercancia-201.json)

```json
{
  "id": 101,
  "centroDistribucionId": 1,
  "productoId": 10,
  "cantidad": 250,
  "origen": "Proveedor Alimentos del Valle S.A.",
  "usuarioId": 3,
  "fechaIngreso": "2026-10-18T10:30:00"
}
```

> `usuarioId` es el id local del usuario que registró el ingreso. Puede ser `null` si el usuario fue eliminado (`ON DELETE SET NULL`).

### TypeScript Interfaces
```typescript
export interface CrearCentroDistribucionRequest {
  nombre: string;
  direccion: string;
  capacidad: number;
  encargadoId?: number | null;
  tiendasAbastecidasIds: number[];
}

export interface CentroDistribucionResponse {
  id: number;
  codigo: string;
  nombre: string;
  direccion: string;
  capacidad: number;
  encargadoId: number | null;
  tiendasAbastecidasIds: number[];
}

export interface RegistrarIngresoMercanciaRequest {
  productoId: number;
  cantidad: number;
  origen: string;
}

export interface IngresoMercanciaResponse {
  id: number;
  centroDistribucionId: number;
  productoId: number;
  cantidad: number;
  origen: string;
  usuarioId: number | null;
  fechaIngreso: string;
}
```

---

## 4. Inventario (`/api/inventario`) — HU-12 y HU-16

### Consulta de Inventario de Tienda
`GET /api/inventario?tiendaId={tiendaId}&nombre={nombre}&categoria={categoria}&productoId={id}`

* **Roles:** `ADMINISTRADOR`, `PLANIFICADOR` o el encargado asignado a la tienda.
* **Query Params:**
  * `tiendaId` (requerido): ID de la tienda.
  * `nombre` (opcional): filtro por nombre de producto.
  * `categoria` (opcional): filtro por categoría.
  * `productoId` (opcional): filtro por ID de producto.

### Consulta de Inventario de CD
`GET /api/inventario/centro-distribucion?centroDistribucionId={centroDistribucionId}&nombre={nombre}&categoria={categoria}&productoId={id}`

* **Roles:** `ADMINISTRADOR`, `PLANIFICADOR` o el encargado asignado al CD.
* `centroDistribucionId` es requerido; los filtros son los mismos.

### Response 200 OK (Lista de Inventario)
* Tienda: [`docs/dto/response/consultar-inventario-200.json`](./response/consultar-inventario-200.json)
* CD: [`docs/dto/response/consultar-inventario-cd-200.json`](./response/consultar-inventario-cd-200.json)

```json
[
  {
    "id": 1,
    "tiendaId": 2,
    "centroDistribucionId": null,
    "productoId": 10,
    "nombreProducto": "Arroz Blanco Premium 1kg",
    "categoriaProducto": "Granos",
    "cantidadActual": 8,
    "stockMinimo": 20,
    "stockBajo": true
  }
]
```

> En una consulta de tienda, `tiendaId` tiene valor y `centroDistribucionId` es `null`. En una consulta de CD ocurre lo contrario.

### Response 403 Forbidden (Sin acceso a la ubicación)
* **Archivo:** [`docs/dto/response/consultar-inventario-403.json`](./response/consultar-inventario-403.json)

```json
{
  "error": "No tiene permisos para consultar el inventario de esta tienda",
  "message": "No tiene permisos para consultar el inventario de esta tienda"
}
```

### TypeScript Interfaces
```typescript
export interface ItemInventarioResponse {
  id: number;
  tiendaId: number | null;
  centroDistribucionId: number | null;
  productoId: number;
  nombreProducto: string;
  categoriaProducto: string;
  cantidadActual: number;
  stockMinimo: number;
  stockBajo: boolean;
}

export interface ConsultarInventarioParams {
  tiendaId: number;
  nombre?: string;
  categoria?: string;
  productoId?: number;
}

export interface ConsultarInventarioCdParams {
  centroDistribucionId: number;
  nombre?: string;
  categoria?: string;
  productoId?: number;
}
```

---

## 5. Productos (`/api/productos`) — HU-04, HU-05 y HU-06

### Request Crear Producto (`POST /api/productos`)
* **Archivo:** [`docs/dto/request/crear-producto.json`](./request/crear-producto.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.

```json
{
  "identificador": "PROD-001",
  "nombre": "Aceite Vegetal 1L",
  "categoria": "Abarrotes",
  "precio": 12.50,
  "unidadMedida": "Litro"
}
```

### Response 201 Created
* **Archivo:** [`docs/dto/response/crear-producto-201.json`](./response/crear-producto-201.json)

```json
{
  "id": 1,
  "identificador": "PROD-001",
  "nombre": "Aceite Vegetal 1L",
  "categoria": "Abarrotes",
  "precio": 12.50,
  "unidadMedida": "Litro",
  "activo": true,
  "estado": "activo"
}
```

### Response Listar Productos (`GET /api/productos`)
* **Archivo:** [`docs/dto/response/listar-productos-200.json`](./response/listar-productos-200.json)
* Cualquier usuario autenticado.

### Request Editar Producto (`PATCH /api/productos/{id}`)
* **Archivo:** [`docs/dto/request/editar-producto.json`](./request/editar-producto.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.
* Todos los campos son opcionales; se actualizan solo los enviados.

```json
{
  "nombre": "Aceite Vegetal Premium 1L",
  "categoria": "Abarrotes",
  "precio": 13.90,
  "unidadMedida": "Litro"
}
```

### Response 200 OK (Editar)
* **Archivo:** [`docs/dto/response/editar-producto-200.json`](./response/editar-producto-200.json)

### Request Cambiar Estado (`PATCH /api/productos/{id}/estado`)
* **Archivo:** [`docs/dto/request/cambiar-estado-producto.json`](./request/cambiar-estado-producto.json)
* **Roles:** `ADMINISTRADOR` o `PLANIFICADOR`.

```json
{
  "activo": false
}
```

La respuesta usa el mismo esquema que `editar-producto-200.json`.

### Verificar Existencias (`GET /api/productos/{id}/tiene-stock`)
* **Archivo:** [`docs/dto/response/tiene-stock-200.json`](./response/tiene-stock-200.json)
* Cualquier usuario autenticado.

```json
{
  "tieneStock": true
}
```

### TypeScript Interfaces
```typescript
export interface CrearProductoRequest {
  identificador: string;
  nombre: string;
  categoria: string;
  precio?: number | null;
  unidadMedida?: string | null;
}

export interface EditarProductoRequest {
  nombre?: string;
  categoria?: string;
  precio?: number | null;
  unidadMedida?: string | null;
}

export interface CambiarEstadoProductoRequest {
  activo: boolean;
}

export interface ProductoResponse {
  id: number;
  identificador: string;
  nombre: string;
  categoria: string;
  precio: number | null;
  unidadMedida: string | null;
  activo: boolean;
  estado: "activo" | "inactivo";
}

export interface ProductoStockResponse {
  tieneStock: boolean;
}
```

---

## 6. Errores

Todos los errores usan `ErrorResponse`:

```typescript
export interface ErrorResponse {
  error: string;     // mensaje natural del proveedor o descripción técnica
  message: string;   // mensaje amigable en español
  code?: number;     // código del proveedor, solo cuando existe (p. ej. 429 de Supabase)
}
```

* **400 Bad Request (validación):** [`docs/dto/response/crear-tienda-400.json`](./response/crear-tienda-400.json)

```json
{
  "error": "El nombre es obligatorio",
  "message": "El nombre es obligatorio"
}
```

* **403 Forbidden:** `error` y `message` contienen el motivo; `code` se omite.
* **429 / 502 en auth:** el `error` conserva el mensaje de Supabase y `code` su código (p. ej. `429` para rate limit).
