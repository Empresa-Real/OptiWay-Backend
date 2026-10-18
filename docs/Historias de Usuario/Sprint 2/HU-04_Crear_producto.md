# HU-04 — Crear Producto

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de productos  

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** registrar un producto con identificador, nombre, categoría, precio y unidad de medida  
> **Para** contar con un catálogo de referencia completo que se use en ventas, inventario y reportes.  

---

## Criterios de Aceptación

1. **Campos y estado inicial:** Dado que indico identificador, nombre y categoría (obligatorios), junto con precio y unidad de medida, cuando guardo, entonces el producto queda creado con estado `"activo"` (`activo = true`) y disponible en el catálogo maestro.
2. **Unicidad de identificador:** El sistema no permite crear un producto con un identificador ya existente (comparación case-insensitive); muestra error (`409 Conflict`) y no lo crea.
3. **Restricción de acceso:** Un usuario con rol distinto a Administrador no puede crear productos; el sistema bloquea la acción y responde con error de permisos insuficientes (`403 Forbidden`).
4. **Requisito no funcional (seguridad):** Solo el Administrador del sistema puede crear o editar productos.
5. **Validación de precios:** El sistema rechaza precios negativos (`precio < 0`), respondiendo `400 Bad Request`.

---

## Especificación del Endpoint

* **Método:** `POST`
* **Ruta:** `/api/productos`
* **Seguridad:** Requiere header `Authorization: Bearer <token_jwt_supabase>` con rol `ADMINISTRADOR`.
* **Cuerpo de la petición (JSON):**

```json
{
  "identificador": "PROD-001",
  "nombre": "Aceite Vegetal 1L",
  "categoria": "Abarrotes",
  "precio": 12.50,
  "unidadMedida": "Litro"
}
```

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`201 Created`** | Producto registrado y persistido exitosamente. | JSON completo del producto creado con `id`, `activo: true`, etc. |
| **`400 Bad Request`** | Falta campo obligatorio (`identificador`, `nombre`, `categoria`) o precio negativo. | `{"error": "El nombre es obligatorio."}` |
| **`401 Unauthorized`** | Token JWT no suministrado o inválido. | Error de autenticación OAuth2. |
| **`403 Forbidden`** | El usuario no posee el rol `ADMINISTRADOR`. | `{"error": "Solo un administrador puede crear productos"}` |
| **`409 Conflict`** | El identificador ya está registrado en el catálogo. | `{"error": "Ya existe un producto con ese identificador."}` |

---

## Componentes Hexagonales Asociados

* **Puerto de Entrada:** [CrearProductoUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/CrearProductoUseCase.java)
* **Servicio de Aplicación:** [ProductoService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/ProductoService.java)
* **Adaptador REST:** [ProductoController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ProductoController.java)
* **DTO:** [CrearProductoRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CrearProductoRequest.java)
* **Puerto de Salida:** [ProductoRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/ProductoRepositoryPort.java)
* **Adaptador de Persistencia:** [ProductoRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoRepositoryAdapter.java)
