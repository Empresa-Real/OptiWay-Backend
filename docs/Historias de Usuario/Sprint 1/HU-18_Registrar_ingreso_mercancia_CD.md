# Guía de continuación: HU-18 — Registrar Ingreso de Mercancía al Centro de Distribución

## Historia de Usuario

> **Como** Encargado de centro de distribución  
> **Quiero** registrar el ingreso de mercancía nueva, proveniente de proveedores o de otros centros de distribución  
> **Para** mantener actualizado el inventario disponible para despachar hacia las tiendas.

---

## Criterios de Aceptación

1. **Actualización automática del inventario del CD:**
   * Al registrar el ingreso indicando `centroDistribucionId`, `productoId`, `cantidad` y `origen`, el sistema busca el inventario actual en ese CD.
   * Si no existía inventario previo del producto en dicho CD, crea el registro con la cantidad indicada.
   * Si ya existía inventario, suma la nueva cantidad a la existente (`cantidadActual = cantidadActual + cantidad`).
2. **Trazabilidad y auditoría:**
   * El sistema almacena un registro histórico en la tabla `ingresos_mercancia` con la fecha y hora (`fechaIngreso`), el centro de distribución, el producto, la cantidad, el origen y el ID del usuario responsable autenticado.
3. **Validación estricta de cantidad:**
   * La cantidad debe ser estrictamente mayor a cero (`cantidad > 0`). Cantidades en cero, negativas o nulas son rechazadas con error `400 Bad Request`.
4. **Validación de origen:**
   * El atributo `origen` es obligatorio (no puede ser nulo ni estar vacío). Indica el proveedor o CD de procedencia.
5. **Existencia de entidades referenciadas:**
   * El Centro de Distribución y el Producto deben existir previamente en el sistema. Si no existen, se rechaza la operación.
6. **Control de acceso y roles:**
   * Solo los usuarios con rol `ADMINISTRADOR` o `ENCARGADO_CD` tienen autorización para registrar ingresos de mercancía.

---

## Especificación del Endpoint

* **Método:** `POST`
* **Ruta:** `/api/ingresos-mercancia`
* **Headers requeridos:**
  ```http
  Authorization: Bearer <token_jwt_supabase>
  Content-Type: application/json
  ```
* **Cuerpo de la petición (JSON Body):**

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `centroDistribucionId` | `Long` | **Sí** | ID del centro de distribución receptor. |
| `productoId` | `Long` | **Sí** | ID del producto que ingresa. |
| `cantidad` | `Integer` | **Sí** | Cantidad de unidades a ingresar (debe ser > 0). |
| `origen` | `String` | **Sí** | Proveedor o CD emisor (ej. "Distribuidora Nacional S.A."). |

### Ejemplo de petición

```http
POST /api/ingresos-mercancia HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{
  "centroDistribucionId": 1,
  "productoId": 15,
  "cantidad": 500,
  "origen": "Proveedor AgroIndustrias del Norte"
}
```

---

## Respuestas Esperadas

| Código HTTP | Condición | Detalle |
| :--- | :--- | :--- |
| **`200 OK` / `201 Created`** | Ingreso registrado exitosamente y stock actualizado en el centro de distribución. | Operación completada. |
| **`400 Bad Request`** | Cantidad menor o igual a cero, origen vacío o datos inválidos. | `{"error": "La cantidad debe ser mayor que cero"}` o `{"error": "El origen es obligatorio"}` |
| **`401 Unauthorized`** | Falta el token JWT o es inválido. | Error de autenticación OAuth2/JWT. |
| **`403 Forbidden`** | El usuario autenticado no posee rol de `ADMINISTRADOR` ni `ENCARGADO_CD`. | `{"error": "No tiene permisos para registrar ingreso de mercancía"}` |
| **`404 Not Found`** | El centro de distribución o el producto no existen en el sistema. | `{"error": "Centro de distribución no encontrado"}` o `{"error": "Producto no encontrado"}` |

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Encargado de CD / Frontend)
  ↓ [POST /api/ingresos-mercancia] + JWT Bearer
SecurityFilterChain (Spring Security valida el JWT de Supabase)
  ↓
IngresoMercanciaController (infrastructure/adapter/in/rest/)
  ↓ authUserId (del claim sub del JWT) + RegistrarIngresoMercanciaRequest
RegistrarIngresoMercanciaUseCase (application/port/in/)
  ↓
IngresoMercanciaService (application/service/ - @Transactional)
  ├── 1. Valida cantidad > 0 y origen no vacío
  ├── 2. Obtiene usuario y valida rol (ADMINISTRADOR / ENCARGADO_CD)
  ├── 3. Verifica existencia del Centro de Distribución y del Producto
  ├── 4. Actualiza o crea el registro en InventarioCentroDistribucion
  └── 5. Registra el histórico en IngresoMercancia con timestamp y usuarioId
```
