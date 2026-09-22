# DTOs y Contratos de API para Frontend & Mock API

Esta carpeta contiene los esquemas y ejemplos JSON de **Request** y **Response** para que el equipo de frontend configure sus Mock APIs (MSW, MirageJS, json-server, Postman Mock Server) mientras los servicios del backend se integran.

---

## Estructura de carpetas

```text
docs/dto/
├── README.md
├── request/
│   ├── crear-usuario.json
│   ├── crear-tienda.json
│   ├── crear-centro-distribucion.json
│   └── registrar-ingreso-mercancia.json
└── response/
    ├── crear-usuario-201.json
    ├── listar-usuarios-200.json
    ├── crear-tienda-201.json
    ├── listar-tiendas-200.json
    ├── crear-tienda-400.json
    ├── crear-centro-distribucion-201.json
    ├── consultar-inventario-200.json
    ├── consultar-inventario-403.json
    └── registrar-ingreso-mercancia-200.json
```

---

## 1. HU-01: Usuarios (`/api/usuarios`)

### Request Crear Usuario (`POST /api/usuarios`)
* **Archivo:** [`docs/dto/request/crear-usuario.json`](./request/crear-usuario.json)
* **Headers:** `Authorization: Bearer <jwt_token>` (Usuario con rol `ADMINISTRADOR`)

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

### Response Listar Usuarios (`GET /api/usuarios`)
* **Archivo:** [`docs/dto/response/listar-usuarios-200.json`](./response/listar-usuarios-200.json)

### TypeScript Interfaces:
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

export interface UsuarioResponse {
  id: number;
  authUserId: string; // UUID de Supabase Auth
  nombre: string;
  email: string;
  rol: RolUsuario;
  creadoPor: string;  // UUID del administrador que lo creó
}
```

---

## 2. HU-07: Tiendas (`/api/tiendas`)

### Request Crear Tienda (`POST /api/tiendas`)
* **Archivo:** [`docs/dto/request/crear-tienda.json`](./request/crear-tienda.json)

```json
{
  "nombre": "Tienda Central Medellín",
  "direccion": "Calle 50 # 45-10",
  "ciudad": "Medellín"
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
  "estado": "Activa"
}
```

### Response Listar Tiendas (`GET /api/tiendas`)
* **Archivo:** [`docs/dto/response/listar-tiendas-200.json`](./response/listar-tiendas-200.json)

```json
[
  {
    "id": 1,
    "codigo": "TND-A1B2C3D4",
    "nombre": "Tienda Central Medellín",
    "direccion": "Calle 50 # 45-10",
    "ciudad": "Medellín",
    "estado": "Activa"
  }
]
```

### TypeScript Interfaces:
```typescript
export interface CrearTiendaRequest {
  nombre: string;
  direccion: string;
  ciudad: string;
}

export interface TiendaResponse {
  id: number;
  codigo: string;
  nombre: string;
  direccion: string;
  ciudad: string;
  estado: string; // "Activa"
}
```

---

## 3. HU-09: Centros de Distribución (`/api/centros-distribucion`)

### Request Crear CD (`POST /api/centros-distribucion`)
* **Archivo:** [`docs/dto/request/crear-centro-distribucion.json`](./request/crear-centro-distribucion.json)

```json
{
  "nombre": "CD Principal Valle de Aburrá",
  "direccion": "Autopista Sur # 65-120, Itagüí",
  "capacidad": 15000,
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
  "tiendasAbastecidasIds": [1, 2, 3]
}
```

### TypeScript Interfaces:
```typescript
export interface CrearCentroDistribucionRequest {
  nombre: string;
  direccion: string;
  capacidad: number;
  tiendasAbastecidasIds: number[];
}

export interface CentroDistribucionResponse {
  id: number;
  codigo: string;
  nombre: string;
  direccion: string;
  capacidad: number;
  tiendasAbastecidasIds: number[];
}
```

---

## 4. HU-12: Inventario de Tienda (`/api/inventario`)

### Consulta de Inventario (`GET /api/inventario?tiendaId={tiendaId}&nombre={nombre}&categoria={categoria}&productoId={id}`)
* **Headers:** `Authorization: Bearer <jwt_token>` (Encargado de la tienda o Administrador)
* **Query Params:**
  * `tiendaId` (requerido): ID de la tienda.
  * `nombre` (opcional): Filtro por nombre de producto.
  * `categoria` (opcional): Filtro por categoría.
  * `productoId` (opcional): Filtro por ID de producto.

### Response 200 OK (Lista de Inventario)
* **Archivo:** [`docs/dto/response/consultar-inventario-200.json`](./response/consultar-inventario-200.json)

```json
[
  {
    "id": 1,
    "tiendaId": 2,
    "productoId": 10,
    "nombreProducto": "Arroz Blanco Premium 1kg",
    "categoriaProducto": "Granos",
    "cantidadActual": 8,
    "stockMinimo": 20,
    "stockBajo": true
  }
]
```

### Response 403 Forbidden (Tienda no asignada)
* **Archivo:** [`docs/dto/response/consultar-inventario-403.json`](./response/consultar-inventario-403.json)

```json
{
  "error": "No tiene permisos para consultar esta tienda"
}
```

### TypeScript Interfaces:
```typescript
export interface ItemInventarioResponse {
  id: number;
  tiendaId: number;
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
```

---

## 5. HU-18: Ingreso de Mercancía a CD (`/api/ingresos-mercancia`)

### Request Registrar Ingreso (`POST /api/ingresos-mercancia`)
* **Archivo:** [`docs/dto/request/registrar-ingreso-mercancia.json`](./request/registrar-ingreso-mercancia.json)
* **Headers:** `Authorization: Bearer <jwt_token>` (Encargado de CD o Administrador)

```json
{
  "centroDistribucionId": 1,
  "productoId": 10,
  "cantidad": 250,
  "origen": "Proveedor Alimentos del Valle S.A."
}
```

### Response 200 OK / 201 Created
* **Archivo:** [`docs/dto/response/registrar-ingreso-mercancia-200.json`](./response/registrar-ingreso-mercancia-200.json)

```json
{
  "mensaje": "Ingreso de mercancia registrado con exito",
  "centroDistribucionId": 1,
  "productoId": 10,
  "cantidad": 250,
  "origen": "Proveedor Alimentos del Valle S.A."
}
```

### TypeScript Interfaces:
```typescript
export interface RegistrarIngresoMercanciaRequest {
  centroDistribucionId: number;
  productoId: number;
  cantidad: number;
  origen: string;
}

export interface RegistrarIngresoMercanciaResponse {
  mensaje: string;
  centroDistribucionId: number;
  productoId: number;
  cantidad: number;
  origen: string;
}
```

