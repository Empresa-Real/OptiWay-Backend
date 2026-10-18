# HU-11 — Registrar Venta

**Épica:** Operación y Abastecimiento  
**Feature:** Registro de ventas  
**Sprint:** Sprint 2  
**Estado:** `Implementado / Mergeado en main`

---

## Historia de Usuario

> **Como** Encargado de tienda  
> **Quiero** registrar una venta indicando el producto, la cantidad y la fecha y hora  
> **Para** mantener un historial que actualice el inventario y alimente el análisis de demanda.  

---

## Criterios de Aceptación

1. **Descuento automático de stock:** Dado que el producto tiene stock disponible en la tienda, cuando registro la venta, entonces el sistema la guarda en el historial y descuenta inmediatamente la cantidad del inventario de la tienda.
2. **Validación de stock suficiente:** El sistema no permite registrar una venta por una cantidad mayor al stock disponible; responde con error (`400 Bad Request` - `StockInsuficienteException`) y no altera el inventario.
3. **Catálogo y estado del producto:** El producto debe existir en el catálogo y tener estado activo (`activo = true`). Si el producto está inactivo o no existe, la venta es rechazada.
4. **Validación de existencia en tienda:** El producto debe estar previamente registrado en el inventario de la tienda. Si no pertenece a la tienda, responde `404 Not Found`.
5. **Control de acceso y permisos:** Solo el encargado asignado a la tienda o un usuario con rol `ADMINISTRADOR` pueden registrar ventas (`403 Forbidden` para otros usuarios o ubicaciones ajenas).

---

## Especificación del Endpoint

* **Método:** `POST`
* **Ruta:** `/api/ventas`
* **Seguridad:** Requiere header `Authorization: Bearer <token_jwt_supabase>` con rol `ENCARGADO_TIENDA` asignado a la tienda o `ADMINISTRADOR`.
* **Cuerpo de la petición (JSON):**

```json
{
  "tiendaId": 1,
  "productoId": 5,
  "cantidad": 3,
  "fechaHora": "2026-10-18T10:30:00"
}
```

*Nota:* Si `fechaHora` se omite, el sistema asigna automáticamente la fecha y hora actual del servidor (`LocalDateTime.now()`).

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`201 Created`** | Venta registrada y descontada del inventario exitosamente. | JSON con el detalle de la venta (`id`, `tiendaId`, `productoId`, `cantidad`, `fechaHora`). |
| **`400 Bad Request`** | Cantidad `<= 0`, stock insuficiente o producto inactivo. | `{"error": "Stock insuficiente. Stock disponible: 2, solicitado: 3"}` |
| **`401 Unauthorized`** | Token JWT no suministrado o inválido. | Error de autenticación OAuth2. |
| **`403 Forbidden`** | Usuario no asignado a la tienda o sin permisos. | `{"error": "No tiene permisos para registrar ventas en la tienda especificada."}` |
| **`404 Not Found`** | La tienda, el producto o el registro de inventario no existen. | `{"error": "La tienda con ID 1 no fue encontrada."}` |

---

## Componentes Hexagonales Asociados

* **Puerto de Entrada:** [RegistrarVentaUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/RegistrarVentaUseCase.java)
* **Comando DTO:** [RegistrarVentaCommand.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/RegistrarVentaCommand.java)
* **Servicio de Aplicación:** [VentaService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java)
* **Adaptador REST:** [VentaController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/VentaController.java)
* **DTO:** [RegistrarVentaRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/RegistrarVentaRequest.java)
* **Puerto de Salida:** [VentaRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/VentaRepositoryPort.java)
* **Adaptador de Persistencia:** [VentaRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/VentaRepositoryAdapter.java)
* **Entidad JPA:** [VentaJpaEntity.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/VentaJpaEntity.java)
* **Repositorio JPA:** [VentaJpaRepository.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/VentaJpaRepository.java)
