# Informe de Auditoría y Correcciones: HU-18 — Ingreso de Mercancía en Centro de Distribución (Fix 4)

Este documento detalla la auditoría técnica sobre las tareas requeridas para la **HU-18**, contrastando los requerimientos formales con la implementación realizada por el compañero en la rama, sus brechas arquitectónicas y el plan de remediación.

---

## 1. Comparativa de Cumplimiento de Tareas (HU-18)

| Tarea Requerida | Estado | Brecha Encontrada |
| :--- | :---: | :--- |
| **Task 1:** Implementar servicio transaccional para incremento de inventario y auditoría. | ⚠️ **Incompleta** | Concurrencia vulnerable (*lost updates* en stock), falta validación de roles de usuario, se ignora la capacidad del CD y retorna `void` descartando la constancia. |
| **Task 2:** Implementar validación de cantidades y datos de entrada para ingreso de mercancía. | ❌ **No Cumplida** | El DTO carece de anotaciones Jakarta Bean Validation (`@NotNull`, `@Positive`, `@NotBlank`) y el endpoint no utiliza `@Valid`. |
| **Task 3:** Crear endpoint `POST /api/centros-distribucion/{id}/ingresos`. | ❌ **No Cumplida** | Se implementó una ruta errónea (`POST /api/ingresos-mercancia`), devuelve `void` con `200 OK` en vez de `201 Created` con el detalle del movimiento. |

---

## 2. Análisis Detallado de Brechas

### 🚨 Brecha 1: Ruta y Contrato REST Inválidos (Task 3)
* **Requerimiento:** `POST /api/centros-distribucion/{id}/ingresos`
  * Debe anidar la acción bajo el recurso del Centro de Distribución correspondiente.
  * Debe responder **`HTTP 201 Created`** retornando el detalle de la constancia generada.
* **Implementación de Daniel:**
  * Endpoint suelto: `POST /api/ingresos-mercancia` solicitando el `centroDistribucionId` dentro del body.
  * Firma del método:
    ```java
    @PostMapping
    public void registrarIngreso(...) // Retorna HTTP 200 OK con cuerpo vacío
    ```
  * **Consecuencia:** El frontend no puede obtener la confirmación del ingreso, el ID generado, la fecha/hora asignada por el servidor ni el nuevo balance de stock.

---

### 🚨 Brecha 2: Ausencia de Bean Validation en el DTO de Entrada (Task 2)
* **Requerimiento:** Validar en la capa de entrada que `cantidad > 0` y los campos obligatorios no sean nulos, respondiendo automáticamente `400 Bad Request` antes de llegar a la capa de dominio.
* **Implementación de Daniel:**
  * En [RegistrarIngresoMercanciaRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/RegistrarIngresoMercanciaRequest.java) no existe ninguna anotación:
    ```java
    // Código actual sin validaciones
    private Long centroDistribucionId;
    private Long productoId;
    private Integer cantidad;
    private String origen;
    ```
  * En el controlador no se incluyó `@Valid`:
    ```java
    public void registrarIngreso(@RequestBody RegistrarIngresoMercanciaRequest request)
    ```
  * Se delegó la validación a `if`s manuales dentro de la lógica del servicio, violando el estándar de validación en los adaptadores de entrada REST.

---

### 🚨 Brecha 3: Deficiencias en el Servicio Transaccional y Auditoría (Task 1)

1. **Condición de carrera en stock (*Race Condition*):**
   * El servicio lee el inventario, calcula en memoria `cantidadActual + cantidad` y luego guarda. En entornos concurrentes con múltiples ingresos simultáneos al mismo CD, esto provoca *Lost Updates* (actualizaciones perdidas de inventario).
2. **Falta de autorización / roles:**
   * La tarea exige auditar al *"usuario que ejecuta la acción"*. El servicio recibe el `authUserId`, pero **no valida que el usuario sea `ADMINISTRADOR` o `ENCARGADO_CD`**. Cualquier usuario con token válido puede registrar ingresos.
3. **Capacidad del Centro de Distribución desatendida:**
   * La entidad `CentroDistribucion` cuenta con el campo `capacidad`, pero el servicio no comprueba si el ingreso ocasiona un sobrecupo de almacenamiento.
4. **Descarte de la constancia:**
   * `ingresoRepositoryPort.guardar(ingreso)` genera el ID y la fecha, pero el caso de uso retorna `void`.

---

## 3. Plan de Remediación y Código Propuesto

### A. DTO con Bean Validation
Crear/actualizar `RegistrarIngresoRequest.java` en `infrastructure/adapter/in/rest/dto/`:
```java
package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RegistrarIngresoRequest {

    @NotNull(message = "El identificador del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    // Getters y Setters...
}
```

### B. DTO de Respuesta (Constancia del Movimiento)
```java
package com.example.optiway.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

public record IngresoMercanciaResponse(
    Long id,
    Long centroDistribucionId,
    Long productoId,
    Integer cantidad,
    String origen,
    Long usuarioId,
    LocalDateTime fechaIngreso,
    Integer stockTotalActualizado
) {}
```

### C. Caso de Uso y Servicio Transaccional
* Modificar `RegistrarIngresoMercanciaUseCase` para retornar `IngresoMercancia`.
* En `IngresoMercanciaService`:
  1. Validar que el usuario tenga rol `ADMINISTRADOR` o `ENCARGADO_CD`.
  2. Verificar existencia de CD y Producto.
  3. Validar que no se exceda la capacidad del CD.
  4. Realizar actualización atómica del inventario en base de datos.
  5. Guardar y retornar la constancia con fecha y usuario responsable.

### D. Endpoint REST Correcto
Reubicar o complementar en `CentroDistribucionController` o `IngresoMercanciaController`:
```java
@PostMapping("/{id}/ingresos")
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<IngresoMercanciaResponse> registrarIngreso(
        @PathVariable("id") Long centroDistribucionId,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody RegistrarIngresoRequest request) {

    UUID authUserId = UUID.fromString(jwt.getSubject());
    IngresoMercancia ingreso = registrarIngresoMercanciaUseCase.registrarIngreso(
            authUserId,
            centroDistribucionId,
            request.getProductoId(),
            request.getCantidad(),
            request.getOrigen()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ingreso));
}
```
