# HU-05 — Editar Producto

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de productos  

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** editar el nombre, la categoría, el precio o la unidad de medida de un producto  
> **Para** mantener actualizada su información cuando cambien esos datos.  

---

## Criterios de Aceptación

1. **Modificación parcial:** Puedo modificar nombre, categoría, precio y unidad de medida; el sistema guarda los cambios y el catálogo los refleja de inmediato.
2. **Validación de precio:** El sistema rechaza un precio negativo o en cero (`precio <= 0`), mostrando error (`400 Bad Request`) y sin guardar el cambio.
3. **Inmutabilidad:** El identificador único (`identificador`), el `id` y el estado del producto no son editables a través de este caso de uso.
4. **Auditoría obligatoria:** Cada edición queda registrada automáticamente en la base de datos (tabla `producto_ediciones`) con la fecha exacta (`Instant.now()`) y el identificador del usuario responsable (`usuario_auth_id`).
5. **Control de acceso:** Solo un usuario con rol `ADMINISTRADOR` puede editar productos; otros roles son rechazados con `403 Forbidden`.

---

## Especificación del Endpoint

* **Método:** `PATCH`
* **Ruta:** `/api/productos/{id}`
* **Seguridad:** Requiere header `Authorization: Bearer <token_jwt_supabase>` con rol `ADMINISTRADOR`.
* **Cuerpo de la petición (JSON) — Campos opcionales:**

```json
{
  "nombre": "Aceite Vegetal Premium 1L",
  "categoria": "Abarrotes",
  "precio": 13.90,
  "unidadMedida": "Litro"
}
```

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Producto actualizado exitosamente. | JSON completo del producto con los cambios aplicados. |
| **`400 Bad Request`** | Precio menor o igual a cero, o cuerpo sin ningún campo para modificar. | `{"error": "El precio debe ser mayor que cero."}` |
| **`401 Unauthorized`** | Token JWT no suministrado o inválido. | Error de autenticación OAuth2. |
| **`403 Forbidden`** | El usuario no posee el rol `ADMINISTRADOR`. | `{"error": "Solo un administrador puede editar productos"}` |
| **`404 Not Found`** | No existe producto con el ID especificado en la ruta. | `{"error": "Producto no encontrado con ID: ..."}` |

---

## Tabla de Auditoría (`producto_ediciones`)

Cada ejecución exitosa inserta una fila en `producto_ediciones`:
* `id`: BIGSERIAL (Primary Key)
* `producto_id`: ID del producto modificado
* `fecha_edicion`: Timestamp UTC
* `usuario_auth_id`: UUID de Supabase Auth

---

## Componentes Hexagonales Asociados

* **Puerto de Entrada:** [EditarProductoUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/EditarProductoUseCase.java)
* **Servicio de Aplicación:** [ProductoService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/ProductoService.java)
* **Adaptador REST:** [ProductoController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ProductoController.java)
* **DTO:** [EditarProductoRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/EditarProductoRequest.java)
* **Entidad JPA de Auditoría:** [ProductoEdicionJpaEntity.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoEdicionJpaEntity.java)
* **Repositorio JPA de Auditoría:** [ProductoEdicionJpaRepository.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoEdicionJpaRepository.java)
