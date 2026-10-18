# Fix 18: Comando Inmutable `RegistrarIngresoCommand` en Ingreso de Mercancía

## 1. Contexto y Diagnóstico
En la operación de ingreso de mercancía (`IngresoMercanciaService`), los parámetros viajaban dispersos o se acoplaba la firma del caso de uso a tipos primitivos o request DTOs de la capa de transporte REST, violando la separación entre adaptadores y la capa de aplicación. Además, en `VentaService` ya se había estandarizado el patrón Command (`RegistrarVentaCommand`), creando una asimetría arquitectónica.

## 2. Solución Aplicada
1. **Creación del Comando Inmutable:**
   - Se introdujo `RegistrarIngresoCommand` en `application/port/in/` como un `record` de Java con validación temprana de no-nulidad y reglas semánticas básicas.
2. **Refactorización del Puerto y Servicio:**
   - Se actualizó el puerto `RegistrarIngresoMercanciaUseCase` para recibir `(RegistrarIngresoCommand command, UUID authUserId)`.
   - En `IngresoMercanciaService`, se adaptó el flujo validando los campos del comando antes de proceder con la verificación de roles y entidades.
3. **Desacoplamiento en el Controlador:**
   - `IngresoMercanciaController` transforma el DTO de entrada en el comando de aplicación inmutable.

## 3. Matriz de Cambios
| Componente | Capa | Acción |
| :--- | :--- | :--- |
| `RegistrarIngresoCommand.java` | Puerto de Entrada | Creación del record inmutable. |
| `RegistrarIngresoMercanciaUseCase.java` | Puerto de Entrada | Firma estandarizada con `RegistrarIngresoCommand`. |
| `IngresoMercanciaService.java` | Aplicación | Adaptación del servicio para consumir el comando. |
| `IngresoMercanciaController.java` | Adaptador REST | Mapeo de Request DTO a Command. |

## 4. Validación
- **Compilación:** `BUILD SUCCESS`.
- **Commit:** `03a27f4` (`refactor: estandarizar comando inmutable RegistrarIngresoCommand en ingreso de mercancia (Fix 18)`).
