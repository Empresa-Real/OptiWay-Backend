# HU-03 — Eliminación de Usuarios

**Épica 1:** Usuarios y accesos  
**Sprint:** Sprint 1  
**Estado:** `Implementado y Verificado`  
**Endpoint:** `DELETE /api/usuarios/{id}`  

---

## Historia de Usuario

> **Como** Administrador  
> **Quiero** eliminar usuarios del sistema  
> **Para** revocar el acceso a empleados desvinculados y mantener la base de usuarios limpia y segura.

---

## Criterios de Aceptación y Reglas de Negocio

1. **Permiso Exclusivo de Administrador (HTTP 403):**
   - Solo un usuario autenticado con rol `ADMINISTRADOR` puede invocar el endpoint de eliminación.
   - Si un usuario con cualquier otro rol (`PLANIFICADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`) o no registrado intenta eliminar un usuario, el sistema rechaza la petición con `HTTP 403 Forbidden` (`AccesoDenegadoException: Solo un administrador puede eliminar usuarios`).

2. **Protección Anti-Lockout / Auto-eliminación (HTTP 400):**
   - Un administrador **no puede eliminarse a sí mismo**.
   - Si `adminAuthUserId.equals(usuarioAEliminar.getAuthUserId())`, el sistema lanza `IllegalArgumentException("Un administrador no puede eliminarse a sí mismo")` y responde `HTTP 400 Bad Request`.

3. **Protección Jerárquica entre Administradores (HTTP 403):**
   - Ningún administrador puede eliminar a otro usuario con rol `ADMINISTRADOR`.
   - Si `usuarioAEliminar.getRol() == Rol.ADMINISTRADOR`, el sistema rechaza la operación con `HTTP 403 Forbidden` (`AccesoDenegadoException: No está permitido eliminar a otro usuario con rol ADMINISTRADOR`).

4. **Existencia del Usuario (HTTP 400):**
   - Si el ID especificado en la ruta no corresponde a ningún usuario en el sistema local, se retorna `HTTP 400 Bad Request` (`IllegalArgumentException: Usuario no encontrado con ID: {id}`).

5. **Eliminación en Cascada (Supabase Auth + Base de Datos Local):**
   - **Paso 1 (Supabase Auth):** Si el usuario tiene un `auth_user_id` asociado, se invoca `DELETE /auth/v1/admin/users/{userId}` en Supabase Auth mediante la clave de servicio (`SUPABASE_SERVICE_ROLE_KEY`). Si el usuario ya fue eliminado previamente en Supabase (HTTP 404), se captura y continúa normalmente sin interrumpir el flujo.
   - **Paso 2 (Repositorio Local):** Se elimina el registro de la tabla `usuarios` mediante `usuarioJpaRepository.deleteById(id)`.

6. **Respuesta Exitosa (HTTP 204):**
   - Al completar la eliminación, el endpoint responde con código de estado `HTTP 204 No Content` sin cuerpo de respuesta.

---

## Diagrama de Flujo del Caso de Uso

```text
DELETE /api/usuarios/{id}
   │
   ├─► Capa 1: ¿Token JWT válido?
   │     NO  ──► HTTP 401 Unauthorized
   │
   ├─► Capa 2: ¿El llamante tiene rol ADMINISTRADOR?
   │     NO  ──► HTTP 403 Forbidden ("Solo un administrador puede eliminar usuarios")
   │
   ├─► Capa 3: ¿El usuario con {id} existe en BD?
   │     NO  ──► HTTP 400 Bad Request ("Usuario no encontrado con ID: {id}")
   │
   ├─► Capa 4: ¿El administrador intenta eliminarse a sí mismo?
   │     SÍ  ──► HTTP 400 Bad Request ("Un administrador no puede eliminarse a sí mismo")
   │
   ├─► Capa 5: ¿El usuario a eliminar tiene rol ADMINISTRADOR?
   │     SÍ  ──► HTTP 403 Forbidden ("No está permitido eliminar a otro usuario con rol ADMINISTRADOR")
   │
   └─► Ejecución:
         1. authPort.eliminarUsuarioAuth(targetAuthId)   [Supabase Auth Admin API]
         2. usuarioRepositoryPort.eliminar(id)          [PostgreSQL / JPA]
         ──► HTTP 204 No Content
```

---

## Componentes Técnicos Involucrados

| Capa | Archivo | Responsabilidad |
|---|---|---|
| **Controlador REST** | [UsuarioController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/UsuarioController.java) | Expone `@DeleteMapping("/{id}")`, extrae `@AuthUserId UUID authUserId` y retorna `HttpStatus.NO_CONTENT`. |
| **Puerto de Entrada** | [EliminarUsuarioUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/EliminarUsuarioUseCase.java) | Contrato del caso de uso `eliminarUsuario(Long id, UUID adminAuthUserId)`. |
| **Servicio de Aplicación** | [UsuarioService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/UsuarioService.java) | Implementa las 5 reglas de validación y la orquestación de borrado dual. |
| **Adaptador Auth** | [SupabaseAuthAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/auth/SupabaseAuthAdapter.java) | Ejecuta la llamada administrativa HTTP `DELETE /auth/v1/admin/users/{userId}` con `serviceRoleKey`. |
| **Adaptador Persistencia** | [UsuarioRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/UsuarioRepositoryAdapter.java) | Borrado físico en PostgreSQL mediante `deleteById(id)`. |
| **Frontend Web** | [admin.html](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/admin.html) | Botón `Eliminar` con modal de confirmación y recarga dinámica de la tabla de usuarios. |
| **Pruebas Unitarias** | [UsuarioServiceTest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/UsuarioServiceTest.java) | 7 pruebas unitarias con Mockito que validan éxito, auto-eliminación, protección inter-admin, 403 y 400. |

---

## Consideraciones sobre Asignaciones de Ubicación

- En el modelo de dominio actual, las entidades `Tienda` y `CentroDistribucion` almacenan el identificador escalar `encargadoId` (tipo `Long`).
- En la interfaz administrativa (`admin.html`), antes de eliminar un usuario asignado a una tienda o centro, el administrador puede reasignar o desvincular la ubicación usando el modal de asignación. Si se elimina un usuario que fungía como encargado, la interfaz muestra la ubicación como `Sin asignar` al no encontrar el objeto usuario correspondiente.
