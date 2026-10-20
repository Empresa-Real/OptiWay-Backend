# Fix 40 — Venta Anidada en Tienda: POST /api/tiendas/{id}/ventas

**Fecha:** 20 de Octubre de 2026  
**Área:** Capa de Infraestructura (Controladores, DTOs REST) y Documentación  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Tras consolidar los ingresos de mercancía en el Fix 38 (`POST /api/centros-distribucion/{id}/ingresos`), el registro de ventas presentaba exactamente el mismo problema, en su forma original:

### A. Doble Fuente de Verdad del Identificador de Tienda
`RegistrarVentaRequest` llevaba `tiendaId` en el body con `@NotNull` y `@Positive`, pese a que **toda venta ocurre en exactamente una tienda** — el padre de la operación es la tienda, y `VentaService` valida además que esa tienda coincida con la asignada al usuario autenticado (`403` si es ajena).

### B. Controlador con una Sola Operación
`VentaController` existía únicamente para exponer `POST /api/ventas` (una única operación), creando un controlador-uno-método cuya responsabilidad pertenece al ciclo de vida de la tienda.

### C. Migración a Record a Medias
`RegistrarVentaRequest` era técnicamente un `record` (Fix 25), pero conservaba getters manuales (`getTiendaId()`, `getProductoId()`, `getCantidad()`, `getFechaHora()`), un constructor secundario de 3 argumentos y **cero anotaciones `@Schema`** — residuos del POJO clásico que el estilo del paquete dto (Fix 36/39) ya no tolera.

---

## 2. Solución Implementada

### A. Endpoint Anidado y Único
- **Canonical:** `POST /api/tiendas/{id}/ventas` — el id de la tienda viaja en la URL, simétrico con `PUT /api/tiendas/{id}/encargado` (existente) y con `POST /api/centros-distribucion/{id}/ingresos` (Fix 38).
- **Eliminados:** `VentaController` (su única operación se movió a `TiendaController`) y la ruta plana `POST /api/ventas`.

```java
@PostMapping("/{id}/ventas")
@ResponseStatus(HttpStatus.CREATED)
public VentaResponse registrarVenta(
        @AuthUserId UUID authUserId,
        @PathVariable("id") Long tiendaId,
        @Valid @RequestBody RegistrarVentaRequest request) {
    return registrarVentaUseCase.registrarVenta(authUserId, request.toCommand(tiendaId));
}
```

### B. Request DTO Limpio con Id en la URL
`RegistrarVentaRequest` quedó sin `tiendaId` en el body, con el patrón canónico `toCommand(Long tiendaId)` (mismo molde que `EditarProductoRequest` y `RegistrarIngresoMercanciaRequest`), sin getters manuales ni constructor secundario, y con `@Schema` completo:

```java
public record RegistrarVentaRequest(
        @NotNull @Positive Long productoId,
        @NotNull @Positive Integer cantidad,
        LocalDateTime fechaHora) {

    public RegistrarVentaCommand toCommand(Long tiendaId) {
        return new RegistrarVentaCommand(tiendaId, productoId, cantidad,
                fechaHora != null ? fechaHora : LocalDateTime.now());
    }
}
```

**Comportamiento intacto:** `fechaHora` sigue siendo opcional en el body y sigue cayendo a `LocalDateTime.now()` cuando se omite.

### C. Documentación Sincronizada
- `HU-11` (ruta, parámetro de ruta, cuerpo del ejemplo y enlace del Adaptador REST → `TiendaController`).
- `HUS-01` (matriz de permisos y tabla de comportamiento `403`).
- `Swagger_OpenAPI` (catálogo: la fila `venta-controller` se elimina y la operación pasa a `tienda-controller`).
- `pendientes.md` (checklist de HU-11: endpoint implementado y tarea pendiente de conexión del frontend).

---

## 3. Archivos

| Archivo | Cambio |
| :--- | :--- |
| `infrastructure/adapter/in/rest/TiendaController.java` | +`POST /{id}/ventas` con delegación pura; tag ampliado con «ventas» |
| `infrastructure/adapter/in/rest/VentaController.java` | **Eliminado** |
| `infrastructure/adapter/in/rest/dto/RegistrarVentaRequest.java` | Sin `tiendaId` en body; `toCommand(Long)`; getters residuales y constructor secundario fuera; +`@Schema` (52 → 37 líneas) |
| `docs/Historias de Usuario/Sprint 2/HU-11_Registrar_venta.md` | Ruta, parámetro de path, ejemplo y adaptador actualizados |
| `docs/Historias de Usuario/Sprint 2/HUS-01_Restriccion_endpoints_privados.md` | 2 filas con la nueva ruta |
| `docs/Swagger_OpenAPI.md` | Fila `venta-controller` eliminada; operación en `tienda-controller` |
| `docs/pendientes.md` | Endpoint del checklist actualizado (2 referencias) |

---

## 4. Beneficios

- **Simetría total entre ambos flujos de stock:**

| Operación | Tienda | Centro de Distribución |
| :--- | :--- | :--- |
| Crear | `POST /api/tiendas` | `POST /api/centros-distribucion` |
| Listar | `GET /api/tiendas` | `GET /api/centros-distribucion` |
| Asignar encargado | `PUT /api/tiendas/{id}/encargado` | `PUT /api/centros-distribucion/{id}/encargado` |
| **Transacción de stock** | **`POST /api/tiendas/{id}/ventas`** | **`POST /api/centros-distribucion/{id}/ingresos`** |

- **Un solo origen para el id de tienda:** el body ya no puede contradecir la URL (regla path/body de Fix 36).
- **−56 líneas de superficie:** controlador de un método eliminado y el DTO reducido de 52 a 37 líneas.
- **Menor carga de Swagger:** una operación menos suelta; todo lo de tiendas vive bajo el tag `Tiendas`.

---

## 5. Observaciones

- **Sin alias corto para tiendas:** a diferencia de los CDs (alias `/api/cd` de Fix 22, restaurado en Fix 38), nunca existió un prefijo corto tipo `/api/t/` y no se inventó uno; si se desea, basta con un array en `@RequestMapping` del `TiendaController`.
- **El frontend no requirió cambios:** `registrar_venta_tienda.html` **aún no está conectado** a ventas (pendiente L47 abierto) — se actualizó la referencia del propio pendiente para que al conectarlo use la ruta anidada.
- `dto/README.md` no tenía sección de ventas (solo HU-12/18) → sin cambios; `CHANGELOG.md` y los fixes 10/11/24/25/32/37 se mantienen intactos por ser documentación histórica.
- Las pruebas (`VentaServiceTest`) construyen `RegistrarVentaCommand` directamente, sin el DTO ni rutas → sin impacto (confirmado por compilación y suite).
- Cambio realizado **sin commitear a petición del usuario**, junto con los fixes 38 y 39, a la espera de autorización.
- Verificación: `.\\mvnw clean test -o` → **BUILD SUCCESS, 38/38 tests, 0 fallos**.

---

## 6. Criterios de Aceptación

- [x] `POST /api/tiendas/{id}/ventas` responde con `201 Created` y delega en `RegistrarVentaUseCase`.
- [x] `RegistrarVentaRequest` no contiene `tiendaId` en el body; usa `toCommand(Long tiendaId)`.
- [x] `VentaController` y la ruta plana `POST /api/ventas` fueron eliminados.
- [x] Sin getters manuales ni constructores secundarios en el DTO; `@Schema` completo.
- [x] Ningún documento vigente referencia `/api/ventas` (solo CHANGELOG y fixes históricos).
- [x] Compilación y 38 tests en verde.
