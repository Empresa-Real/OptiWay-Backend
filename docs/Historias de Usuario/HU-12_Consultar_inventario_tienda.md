# Guía de continuación: HU-12 — Consultar Inventario de mi Tienda

## Historia de Usuario

> **Como** Encargado de tienda  
> **Quiero** consultar el nivel de inventario actual de los productos de mi tienda  
> **Para** identificar stock bajo o excedente y anticipar necesidades de reposición.

---

## Criterios de Aceptación

1. **Consulta autorizada:** El usuario solo puede consultar el inventario de la(s) tienda(s) donde esté asignado como encargado (`encargadoId == usuario.id`). Si intenta consultar otra tienda, el sistema bloquea el acceso con mensaje de permisos (`403 Forbidden`). El usuario con rol `ADMINISTRADOR` tiene acceso para auditar cualquier tienda.
2. **Identificación de stock crítico:** El sistema calcula y expone la bandera `stockBajo: true` cuando la `cantidadActual < stockMinimo`, facilitando el resaltado visual en el frontend.
3. **Filtros de búsqueda disponibles:**
   * Por identificador de producto (`productoId`).
   * Por nombre o coincidencia parcial del producto (`nombre`, case-insensitive).
   * Por categoría del producto (`categoria`, case-insensitive).
4. **Respuesta enriquecida:** Cada ítem de inventario incluye el detalle del producto (`nombreProducto`, `categoriaProducto`, `cantidadActual`, `stockMinimo`, `stockBajo`).

---

## Especificación del Endpoint

* **Método:** `GET`
* **Ruta:** `/api/inventario`
* **Headers requeridos:**
  ```http
  Authorization: Bearer <token_jwt_supabase>
  ```
* **Query Parameters:**

| Parámetro | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `tiendaId` | `Long` | **Sí** | ID de la tienda a consultar. |
| `nombre` | `String` | No | Filtro de búsqueda por nombre del producto. |
| `categoria` | `String` | No | Filtro de búsqueda por categoría. |
| `productoId` | `Long` | No | Filtro exacto por ID de producto. |

### Ejemplo de petición

```http
GET /api/inventario?tiendaId=1&categoria=Bebidas HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
```

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Consulta exitosa. Retorna la lista de ítems de inventario con stock calculado y datos del producto. | Array JSON de inventario. |
| **`400 Bad Request`** | Parámetro `tiendaId` faltante o formato numérico inválido. | Objeto de error con detalle. |
| **`401 Unauthorized`** | Token JWT no suministrado, vencido o con firma inválida. | Error de autenticación OAuth2/JWT. |
| **`403 Forbidden`** | El usuario autenticado no es el encargado asignado a la tienda especificada. | `{"error": "No tiene permisos para consultar esta tienda"}` |
| **`404 Not Found`** | La tienda indicada con `tiendaId` no existe en la base de datos. | `{"error": "La tienda no existe"}` |

### Ejemplo de respuesta exitosa (`200 OK`)

```json
[
  {
    "id": 101,
    "tiendaId": 1,
    "productoId": 15,
    "nombreProducto": "Arroz Blanco Premium 1kg",
    "categoriaProducto": "Granos",
    "cantidadActual": 8,
    "stockMinimo": 20,
    "stockBajo": true
  },
  {
    "id": 102,
    "tiendaId": 1,
    "productoId": 22,
    "nombreProducto": "Aceite Vegetal 900ml",
    "categoriaProducto": "Abarrotes",
    "cantidadActual": 45,
    "stockMinimo": 15,
    "stockBajo": false
  }
]
```

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Frontend / Encargado de Tienda)
  ↓ [GET /api/inventario?tiendaId=1] + Bearer JWT
SecurityFilterChain (Spring Security valida el JWT de Supabase)
  ↓
InventarioController (infrastructure/adapter/in/rest/)
  ↓ jwt.getSubject() (UUID authUserId) + query params
ConsultarInventarioUseCase (application/port/in/)
  ↓
InventarioService (application/service/)
  ├── Valida identidad del usuario (UsuarioRepositoryPort)
  ├── Comprueba asignación de encargado en la tienda (TiendaRepositoryPort)
  ├── Obtiene inventarios asociados a la tienda (InventarioRepositoryPort)
  └── Enriquece ítems con datos del producto y evalúa stockBajo (ProductoRepositoryPort)
```
