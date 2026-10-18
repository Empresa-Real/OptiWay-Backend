# HU-06 — Desactivar o Reactivar Producto

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de productos  

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** desactivar un producto sin eliminarlo, y poder reactivarlo más adelante  
> **Para** sacarlo de circulación sin perder su historial cuando vuelva a estar disponible.  

---

## Criterios de Aceptación

1. **Suspensión comercial:** Al desactivar un producto (`activo = false`), deja de estar disponible para nuevas ventas o recepciones, y su historial de movimientos e inventarios se conserva intacto en el sistema.
2. **Verificación de existencias previas:** Si el producto tiene stock activo en alguna tienda o CD, el sistema provee un mecanismo (`GET /api/productos/{id}/tiene-stock`) para que el **Frontend** detecte existencias y presente un diálogo modal de confirmación antes de enviar la orden de desactivación.
3. **Reactivación:** Al reactivar un producto (`activo = true`), vuelve a estar disponible para ventas y órdenes de reposición.
4. **Control de acceso:** Solo el Administrador del sistema puede cambiar el estado de un producto (`403 Forbidden` para otros roles).

---

## Especificación de Endpoints

### 1. Cambiar Estado del Producto
* **Método:** `PATCH`
* **Ruta:** `/api/productos/{id}/estado`
* **Seguridad:** Requiere header `Authorization: Bearer <token_jwt_supabase>` con rol `ADMINISTRADOR`.
* **Cuerpo de la petición (JSON):**

```json
{
  "activo": false
}
```

#### Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Estado modificado exitosamente. | JSON del producto con su nuevo estado `activo: false/true`. |
| **`400 Bad Request`** | Campo `activo` ausente o nulo. | `{"error": "Debe indicar si el producto estará activo o inactivo"}` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | Error de autenticación OAuth2. |
| **`403 Forbidden`** | Usuario sin rol `ADMINISTRADOR`. | `{"error": "Solo un administrador puede activar o desactivar productos"}` |
| **`404 Not Found`** | ID de producto no encontrado. | `{"error": "El producto no existe con ID: ..."}` |

---

### 2. Verificar Stock Activo (Soporte para Confirmación en Frontend)
* **Método:** `GET`
* **Ruta:** `/api/productos/{id}/tiene-stock`
* **Respuesta (`200 OK`):**
```json
{
  "tieneStock": true
}
```

---

## Componentes Hexagonales Asociados

* **Puerto de Entrada:** [CambiarEstadoProductoUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/CambiarEstadoProductoUseCase.java)
* **Servicio de Aplicación:** [ProductoService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/ProductoService.java)
* **Adaptador REST:** [ProductoController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ProductoController.java)
* **DTO:** [CambiarEstadoProductoRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CambiarEstadoProductoRequest.java)
* **Puerto de Salida de Inventario:** [InventarioRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/InventarioRepositoryPort.java)
* **Adaptador de Persistencia de Inventario:** [InventarioRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/InventarioRepositoryAdapter.java)
