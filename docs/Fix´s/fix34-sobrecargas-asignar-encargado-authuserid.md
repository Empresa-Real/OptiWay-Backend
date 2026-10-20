# Fix 34 — Eliminación de sobrecargas de asignarEncargado y exigencia de authUserId

> **Commit asociado:** `5984ef8` — `refactor(ubicacion): eliminar sobrecargas obsoletas de asignarEncargado y exigir authUserId obligatorio` (2026-10-19 15:09)
> **Nota:** doc retroactivo generado dentro del plan de estandarización hexagonal (serie fix28–fix37).

## 1. Contexto y Diagnóstico del Problema

1. **Bypass de auditoría por sobrecarga:** `AsignarEncargadoTiendaUseCase` y `AsignarEncargadoCDUseCase` exponían una sobrecarga sin `authUserId` que delegaba pasando `null`:
   ```java
   // Patrón inseguro eliminado:
   Tienda asignarEncargado(Long tiendaId, Long encargadoId); // delega con authUserId = null
   ```
   Cualquier caller interna podía asignar encargados sin usuario autenticado.
2. **Guard de autorización tolerante:** la implementación usaba `requireAdministradorIfPresent(authUserId, ...)`, que con `authUserId = null` **no exigía rol de administrador** — la operación quedaba sin control de rol efectivo.
3. **Código muerto:** ninguna de las invocaciones reales (controladores con `@AuthUserId`) usaba la sobrecarga.

## 2. Solución Aplicada

- **Eliminadas las sobrecargas** de ambos puertos, dejando una única firma obligatoria con `authUserId`.
- **`requireAdministradorIfPresent` → `requireAdministrador` estricto** en `TiendaService` y `CentroDistribucionService`: con `null` ahora lanza excepción en lugar de omitir la comprobación.
- Resultado:
  ```java
  Tienda asignarEncargado(Long tiendaId, Long encargadoId, UUID authUserId);           // exige auth
  CentroDistribucion asignarEncargado(Long centroId, Long encargadoId, UUID authUserId); // exige auth
  ```

## 3. Archivos Intervenidos

| Estado | Archivo |
|---|---|
| Modificado | `application/port/in/AsignarEncargadoTiendaUseCase.java` |
| Modificado | `application/port/in/AsignarEncargadoCDUseCase.java` |
| Modificado | `application/service/TiendaService.java` |
| Modificado | `application/service/CentroDistribucionService.java` |

## 4. Beneficios Obtenidos

1. **Auditoría blindada:** ninguna ruta de asignación de encargados puede ejecutarse sin `authUserId` verificado.
2. **Control de rol efectivo:** `requireAdministrador` estricto garantiza que solo ADMINISTRADOR asigne encargados.
3. **Menos superficie de API:** dos firmas ambiguas → una explícita.

## 5. Observaciones

- **Corrección posterior en dos pasos de la serie:**
  - `fix28` movió `authUserId` al **primer** lugar de la firma (`asignarEncargado(UUID authUserId, Long tiendaId, Long encargadoId)`).
  - `fix35`/`fix37` propagan el mismo orden a servicios y controladores.
- Los retornos siguieron siendo entidades de dominio (`Tienda`, `CentroDistribucion`) hasta `fix35`, donde pasan a `TiendaResponse`/`CentroDistribucionResponse`.
- `graph.html` es un sidecar generado por Maven (`copy-graph-html`).

## 6. Criterios de Aceptación

- [x] Ningún puerto de asignación admite llamada sin `authUserId`
- [x] Cero usos de `requireAdministradorIfPresent` en asignación de encargados
- [x] Ninguna sobrecarga delega con `null`
- [x] Los callers reales (controladores con `@AuthUserId`) no se vieron afectados
