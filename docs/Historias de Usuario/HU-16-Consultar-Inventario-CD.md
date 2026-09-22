# Guía de continuación: HU-16 — Consultar Inventario de mi Centro de Distribución

## Historia de Usuario

> **Como** Encargado de centro de distribución
> **Quiero** consultar el nivel de inventario actual de mi centro
> **Para** saber qué tengo disponible para despachar.

---

## Criterios de Aceptación

1. **Consulta autorizada:** El usuario solo puede consultar el inventario del/los centro(s) de distribución donde esté asignado como encargado (`encargadoId == usuario.id` sobre una asignación `ACTIVA` de tipo `CENTRO_DISTRIBUCION`). Si intenta consultar un CD distinto al suyo, el sistema bloquea el acceso con mensaje de permisos (`403 Forbidden`). El usuario con rol `ADMINISTRADOR` tiene acceso para auditar cualquier CD.
2. **Identificación de stock crítico:** El sistema calcula y expone la bandera `stockBajo: true` cuando la `cantidadActual < stockMinimo`, facilitando el resaltado visual en el frontend.
3. **Filtros de búsqueda disponibles:**
   * Por identificador de producto (`productoId`).
   * Por nombre o coincidencia parcial del producto (`nombre`, case-insensitive).
   * Por categoría del producto (`categoria`, case-insensitive).
4. **Respuesta enriquecida:** Cada ítem de inventario incluye el detalle del producto (`nombreProducto`, `categoriaProducto`, `cantidadActual`, `stockMinimo`, `stockBajo`).

---

## Especificación del Endpoint

* **Método:** `GET`
* **Ruta:** `/api/inventario/cd`
* **Headers requeridos:**
  ```http
  Authorization: Bearer <token_jwt_supabase>
  ```
* **Query Parameters:**

| Parámetro | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `centroDistribucionId` | `Long` | **Sí** | ID del centro de distribución a consultar. |
| `nombre` | `String` | No | Filtro de búsqueda por nombre del producto. |
| `categoria` | `String` | No | Filtro de búsqueda por categoría. |
| `productoId` | `Long` | No | Filtro exacto por ID de producto. |

### Ejemplo de petición

```http
GET /api/inventario/cd?centroDistribucionId=3&categoria=Bebidas HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
```

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Consulta exitosa. Retorna la lista de ítems de inventario del CD con stock calculado y datos del producto. | Array JSON de inventario. |
| **`400 Bad Request`** | Parámetro `centroDistribucionId` faltante o formato numérico inválido. | Objeto de error con detalle. |
| **`401 Unauthorized`** | Token JWT no suministrado, vencido o con firma inválida. | Error de autenticación OAuth2/JWT. |
| **`403 Forbidden`** | El usuario autenticado no es el encargado asignado al centro de distribución especificado. | `{"error": "No tiene permisos para consultar este centro de distribución"}` |
| **`404 Not Found`** | El centro de distribución indicado con `centroDistribucionId` no existe en la base de datos. | `{"error": "El centro de distribución no existe"}` |

### Ejemplo de respuesta exitosa (`200 OK`)

```json
[
  {
    "id": 210,
    "centroDistribucionId": 3,
    "productoId": 15,
    "nombreProducto": "Arroz Blanco Premium 1kg",
    "categoriaProducto": "Granos",
    "cantidadActual": 120,
    "stockMinimo": 200,
    "stockBajo": true
  },
  {
    "id": 211,
    "centroDistribucionId": 3,
    "productoId": 22,
    "nombreProducto": "Aceite Vegetal 900ml",
    "categoriaProducto": "Abarrotes",
    "cantidadActual": 480,
    "stockMinimo": 150,
    "stockBajo": false
  }
]
```

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Frontend / Encargado de Centro de Distribución)
  ↓ [GET /api/inventario/cd?centroDistribucionId=3] + Bearer JWT
SecurityFilterChain (Spring Security valida el JWT de Supabase)
  ↓
InventarioCdController (infrastructure/adapter/in/rest/)
  ↓ jwt.getSubject() (UUID authUserId) + query params
ConsultarInventarioCdUseCase (application/port/in/)
  ↓
InventarioService (application/service/)
  ├── Valida identidad del usuario (UsuarioRepositoryPort)
  ├── Comprueba asignación ACTIVA de tipo CENTRO_DISTRIBUCION del encargado (AsignacionRepositoryPort)
  ├── Obtiene inventarios asociados al centro de distribución (InventarioRepositoryPort)
  └── Enriquece ítems con datos del producto y evalúa stockBajo (ProductoRepositoryPort)
```
