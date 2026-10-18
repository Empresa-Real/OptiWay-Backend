# Fix 15 — Centralización de Autorización de Roles y Eliminación de Boilerplate Repetido (DRY)

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Dominio, Servicios de Aplicación, Infraestructura de Seguridad  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Durante la auditoría de arquitectura y Clean Code se identificó una violación clara del principio **DRY (Don't Repeat Yourself)** y de la **Responsabilidad Única (SRP)** de SOLID en los servicios de aplicación:

En `ProductoService`, `TiendaService`, `CentroDistribucionService` y `UsuarioService` se repetía el mismo bloque de 6-8 líneas para verificar la identidad y el rol del usuario autenticado:

```java
if (authUserId == null) {
    throw new AccesoDenegadoException("Solo un administrador puede ...");
}
Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
        .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
if (usuario.getRol() != Rol.ADMINISTRADOR) {
    throw new AccesoDenegadoException("Solo un administrador puede ...");
}
```

Este patrón aparecía en múltiples métodos de cada servicio (`crearProducto`, `editarProducto`, `cambiarEstadoProducto`, `crearTienda`, `asignarEncargado`, `crearCentroDistribucion`, `asignarEncargadoCD`, `crearUsuario`, `obtenerUsuarios`, `eliminarUsuario`, `actualizarRol`), lo que implica:

- **Duplicación de lógica:** Cualquier cambio en la forma de validar la autorización requiere editar varios servicios.
- **Riesgo de inconsistencia:** Un servicio podría usar un mensaje de error distinto o olvidar validar `authUserId == null`.
- **Acoplamiento:** Cada servicio conoce directamente `UsuarioRepositoryPort`, `Rol` y `AccesoDenegadoException` para fines de autorización, que no son su responsabilidad principal.

## 2. Cambios Implementados

### A. [Usuario.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java)

Se enriqueció el modelo de dominio con métodos de consulta de rol:

```java
public boolean esAdministrador() {
    return this.rol == Rol.ADMINISTRADOR;
}

public boolean esEncargado() {
    return this.rol == Rol.ENCARGADO_TIENDA || this.rol == Rol.ENCARGADO_CD;
}
```

Esto encapsula la lógica de rol dentro del dominio, donde pertenece.

### B. [AuthorizationService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthorizationService.java) (nuevo)

Componente `@Component` centralizado que valida identidad y permisos de manera uniforme:

- **`requireAdministrador(authUserId, accion)`**: valida que `authUserId` no sea null, que el usuario exista y que tenga rol `ADMINISTRADOR`. Devuelve el `Usuario` autenticado.
- **`requireAdministradorIfPresent(authUserId, accion`)**: igual que el anterior, pero permite `authUserId == null` (útil para endpoints donde la autenticación es opcional).
- **`requireAuthenticated(authUserId)`**: valida que el usuario exista, sin restricción de rol.

### C. Refactorización de Servicios de Aplicación

Se eliminó el boilerplate repetido de los cuatro servicios, delegando en `AuthorizationService`:

| Servicio | Métodos refactorizados | Antes (líneas por verificación) | Después |
| :--- | :--- | :--- | :--- |
| **ProductoService** | `crearProducto`, `editarProducto`, `cambiarEstadoProducto` | 6-8 líneas | 1 línea |
| **TiendaService** | `crearTienda`, `asignarEncargado` | 6-8 líneas | 1 línea |
| **CentroDistribucionService** | `crearCentroDistribucion`, `asignarEncargado` | 6-8 líneas | 1 línea |
| **UsuarioService** | `crearUsuario`, `obtenerUsuarios`, `eliminarUsuario`, `actualizarRol` | 6-8 líneas + método privado `esAdministrador()` | 1 línea cada uno; método privado eliminado |

Ejemplo del antes y después en `ProductoService`:

**Antes:**
```java
if (authUserId == null) {
    throw new AccesoDenegadoException("Solo un administrador puede crear productos");
}
Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
        .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
if (usuario.getRol() != Rol.ADMINISTRADOR) {
    throw new AccesoDenegadoException("Solo un administrador puede crear productos");
}
```

**Después:**
```java
authorizationService.requireAdministrador(authUserId, "crear productos");
```

## 3. Matriz Comparativa

| Aspecto | Antes | Después |
| :--- | :--- | :--- |
| **Lógica de autorización** | Duplicada en 4 servicios | Centralizada en `AuthorizationService` |
| **Líneas por verificación** | 6-8 líneas | 1 línea |
| **Métodos de dominio en Usuario** | Ninguno | `esAdministrador()`, `esEncargado()` |
| **Consistencia de mensajes** | Variable por servicio | Uniforme a través de `AuthorizationService` |
| **Responsabilidad de servicios** | Negocio + autorización | Solo negocio |

## 3. Verificación de Compilación

Se ejecutó la compilación del proyecto:

```bash
./mvnw compile
```

**Resultado:** `BUILD SUCCESS` — 98 archivos fuente compilados sin errores.

## 4. Notas de Retrocompatibilidad

- Los mensajes de error se mantienen semánticamente equivalentes (ej. `"Solo un administrador puede crear productos"`).
- El método privado `esAdministrador(UUID)` de `UsuarioService` fue eliminado; su funcionalidad queda cubierta por `AuthorizationService.requireAdministrador()`.
- `Usuario.esAdministrador()` y `Usuario.esEncargado()` son métodos de consulta aditivos; no modifican el comportamiento existente.

---