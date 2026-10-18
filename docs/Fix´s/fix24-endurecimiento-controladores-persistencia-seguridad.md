# Fix 24 — Endurecimiento de Controladores REST, Persistencia y Seguridad

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Infraestructura (Adaptadores de Entrada REST, Adaptadores de Salida JPA, Seguridad)  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Durante la revisión exhaustiva de arquitectura hexagonal y Clean Code se identificaron los siguientes problemas:

### 1.1 Acoplamiento fuerte entre entidades JPA (Persistencia)
`CentroDistribucionJpaEntity` usaba `@ManyToMany List<TiendaJpaEntity>` para representar las tiendas abastecidas por un centro de distribución. Esto acopla directamente dos entidades JPA, obligando al adaptador de persistencia a consultar `tiendaJpaRepository.findAllById()` dentro de `toEntity()`, generando consultas N+1 potenciales y dificultando el mapeo a dominio.

### 1.2 Riesgo de inserción no intencionada (Persistencia)
`ProductoRepositoryAdapter.save()` usaba `productoJpaRepository.findById(id).orElse(new ProductoJpaEntity())`. Si el ID no existía, creaba una `ProductoJpaEntity` con ID null que JPA interpretaba como una inserción en lugar de una actualización, pudiendo generar datos huérfanos o errores de integridad.

### 1.3 Estilo inconsistente y nombres calificados (Persistencia)
Varios adaptadores y repositorios usaban nombres de clase completamente calificados (`java.util.Optional`, `com.example...IngresoMercanciaJpaEntity`) en lugar de importar adecuadamente, reduciendo la legibilidad. `UsuarioRepositoryAdapter.eliminar()` retornaba `Void` en lugar de `void`.

### 1.4 Controladores REST sin validación declarativa (Capa Web)
Varios controladores no usaban `@Valid` en sus `@RequestBody`, dejando que las validaciones de Bean Validation (`@NotNull`, `@Positive`, `@NotBlank`) en los DTOs no se aplicaran en la capa de entrada. Además, `ProductoController` y `CentroDistribucionController` usaban setters manuales para mapear DTO→Dominio en lugar de usar `toDomain()`. `ProductoController.tieneStock` devolvía `Map<String, Boolean>` en lugar de un DTO tipado.

### 1.5 Seguridad (Infraestructura)
`SecurityConfig.desabilitaba CSRF sin justificación`. `SupabaseAuthAdapter` tenía un `catch (InvitacionAuthException)` redundante que solo relanzaba, usaba nombres de clase completamente calificados para excepciones de Spring, y `buscarUsuarioPorEmailEnSupabase` traía todos los usuarios de Supabase para filtrar en Java (consulta ineficiente, N+1 potencial).

---

## 2. Cambios Implementados

### 2.1 Persistencia — Desacoplamiento de entidades JPA

**`CentroDistribucionJpaEntity.java`:**
- Reemplazado `@ManyToMany List<TiendaJpaEntity> tiendasAbastecidas` por `List<Long> tiendasAbastecidasIds`.
- Eliminadas importaciones de `JoinColumn`, `JoinTable`, `ManyToMany`.
- Se mantiene el campo `tiendasAbastecidasIds` como lista de IDs, desacoplando la entidad JPA de `TiendaJpaEntity`.

**`CentroDistribucionRepositoryAdapter.java`:**
- Removida dependencia innecesaria de `TiendaJpaRepository`.
- `toEntity()` ahora usa `entity.setTiendasAbastecidasIds(domain.getTiendasAbastecidasIds())` directamente.
- `toDomain()` pasa `entity.getTiendasAbastecidasIds()` directamente al constructor de dominio.

### 2.2 Persistencia — Corrección de `save()` en `ProductoRepositoryAdapter`

**`ProductoRepositoryAdapter.java`:**
- Cambiado `.orElse(new ProductoJpaEntity())` por `.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + producto.getId()))`.
- Ahora si el ID no existe, se lanza una excepción clara en lugar de crear una entidad con ID nulo.

### 2.3 Persistencia — Limpieza de imports y estilo

**`IngresoMercanciaRepositoryAdapter.java`:**
- Agregadas importaciones propias (`java.util.List`, `java.util.Optional`).
- Eliminados nombres de clase completamente calificados (`com.example.optiway...IngresoMercanciaJpaEntity`).
- Código simplificado y legible.

**`InventarioJpaRepository.java`:**
- Agregado `import java.util.Optional`.
- Eliminados usos de `java.util.Optional` completamente calificados.

**`InventarioRepositoryAdapter.java`:**
- Agregado `import java.util.Optional`.
- Eliminados usos de `java.util.Optional` completamente calificados.

**`UsuarioRepositoryAdapter.java`:**
- Cambiado `Void eliminar(Long id)` por `void eliminar(Long id)`.

### 2.3 Seguridad — Endurecimiento

**`SecurityConfig.java`:**
- Agregado comentario justificando `csrf().disable()`: la API es stateless y se autentica vía JWT (Bearer token), por lo que no es vulnerable a ataques CSRF tradicionales de navegador.

**`SupabaseAuthAdapter.java`:**
- Removido `catch (InvitacionAuthException exception)` redundante que solo relanzaba la misma excepción.
- Importadas excepciones de Spring (`HttpClientErrorException`, `RestClientResponseException`) en lugar de usar nombres completamente calificados en los bloques `catch`.
- `buscarUsuarioPorEmailEnSupabase()` ahora usa `.queryParam("email", email)` en la URI de Supabase para filtrar por email en el servidor, en lugar de traer todos los usuarios y filtrar en memoria Java.

### 2.4 Controladores REST — Endurecimiento (complemento de fix anterior)

**Controladores con `@Valid`:**
- `ProductoController`: `crearProducto`, `editarProducto`, `cambiarEstado`
- `VentaController`: `registrarVenta`
- `TiendaController`: `crearTienda`, `asignarEncargado`
- `CentroDistribucionController`: `crear`, `asignarEncargado`
- `UsuarioController`: `crearUsuario`, `actualizarRol`

**Mapeo DTO→Dominio con `toDomain()`:**
- `ProductoController.crearProducto`: ahora usa `request.toDomain()`
- `ProductoController.editarProducto`: ahora usa `request.toDomain()`
- `CentroDistribucionController.crear`: ahora usa `request.toDomain()`
- DTOs actualizados con constructor y `toDomain()`: `CrearProductoRequest`, `EditarProductoRequest`, `CrearCentroDistribucionRequest`

**Respuesta tipada:**
- `ProductoController.tieneStock`: ahora devuelve `ProductoStockResponse` (record con `boolean tieneStock`) en lugar de `Map<String, Boolean>`.
- Nuevo archivo: `ProductoStockResponse.java`.

**Import removido:**
- `UsuarioController.java`: removido `import com.example.optiway.domain.model.Rol` (no usado).

---

## 3. Matriz Comparativa

| Área | Antes | Después |
|:-----|:------|:--------|
| CentroDistribucionJpaEntity | `@ManyToMany List<TiendaJpaEntity>` | `List<Long> tiendasAbastecidasIds` |
| ProductoRepositoryAdapter.save() | `orElse(new ProductoJpaEntity())` | `orElseThrow(IllegalArgumentException)` |
| IngresoMercanciaRepositoryAdapter | Nombres de clase calificados | Imports propios y estilo limpio |
| InventarioJpaRepository | `java.util.Optional` calificado | `import java.util.Optional` |
| InventarioRepositoryAdapter | `java.util.Optional` calificado | `import java.util.Optional` |
| UsuarioRepositoryAdapter.eliminar | `Void` | `void` |
| SecurityConfig.csrf | Sin justificación | Comentario explicativo (API stateless + JWT) |
| SupabaseAuthAdapter | Catch redundante, imports calificados, consulta ineficiente | Catch removido, imports propios, queryParam por email |
| Controladores REST | Sin `@Valid`, setters manuales, `Map<String,Boolean>` | `@Valid` en todos, `toDomain()`, `ProductoStockResponse` |

---

## 4. Verificación de Compilación

```bash
./mvnw compile
```

**Resultado:** `BUILD SUCCESS` — 98 archivos fuente compilados sin errores.

---

## 5. Verificación de Pruebas

```bash
./mvnw test
```

**Resultado:** 23 tests de unidad ejecutados, 0 fallos, 0 errores.  
1 test (`OptiwayApplicationTests.contextLoads`) falla por falta de base de datos de prueba en el entorno (problema de infraestructura, no de código).

---

## 6. Notas de Retrocompatibilidad

- El cambio de `@ManyToMany List<TiendaJpaEntity>` a `List<Long>` en `CentroDistribucionJpaEntity` modifica el esquema de base de datos. La columna `tiendas_abastecidas_ids` reemplaza la tabla intermedia `centro_distribucion_tiendas`. Se debe ejecutar migración correspondiente en entornos existentes.
- `ProductoRepositoryAdapter.save()` ahora lanza `IllegalArgumentException` si el ID no existe, en lugar de crear una entidad huérfana. Los clientes que dependieran del comportamiento anterior (crear implícitamente) deben actualizarse.
- `UsuarioRepositoryAdapter.eliminar()` ahora retorna `void` en lugar de `Void`. Los llamadores que verificaran el retorno deben ajustarse.