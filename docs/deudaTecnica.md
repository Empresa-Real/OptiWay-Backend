# Deuda Técnica y Decisiones Arquitectónicas Abiertas (OptiWay)

> Este documento recoge **decisiones de diseño pendientes e inconsistencias internas** del código que no son tareas de producto.
> El trabajo funcional (endpoints por conectar, features a completar) vive en [`docs/pendientes.md`](./pendientes.md).
> Los cambios ya ejecutados y justificados viven en [`docs/Fix´s/`](./Fix´s/).

**Actualizado:** 20 de octubre de 2026

---

## 1. Duplicidad `save` / `guardar` en los puertos de repositorio

**Estado:** ⚠️ Activo · **Impacto:** Legibilidad / coherencia · **Esfuerzo:** Bajo (renombrado mecánico)

Todos los puertos de repositorio (`application/port/out/*RepositoryPort`) exponen **los dos nombres para la misma operación**, puenteados con un método `default`:

```java
// VentaRepositoryPort — el primario es "save", el alias es "guardar"
Venta save(Venta venta);
default Venta guardar(Venta venta) { return save(venta); }

// UsuarioRepositoryPort — el primario es "guardar", el alias es "save"
Usuario guardar(Usuario usuario);
default Usuario save(Usuario usuario) { return guardar(usuario); }
```

### Evidencia (reparto actual)

| Puerto | Método primario | Alias `default` |
| :--- | :--- | :--- |
| `TiendaRepositoryPort` | `save` | `guardar` |
| `ProductoRepositoryPort` | `save` | `guardar` |
| `InventarioRepositoryPort` | `save` | `guardar` |
| `VentaRepositoryPort` | `save` | `guardar` |
| `UsuarioRepositoryPort` | `guardar` | `save` |
| `CentroDistribucionRepositoryPort` | `guardar` | `save` |
| `IngresoMercanciaRepositoryPort` | `guardar` | `save` |

El problema no es solo el reparto 4/3: **los servicios los mezclan libremente, incluso en el mismo método**:

- `VentaService`: `inventarioRepositoryPort.save(...)` (L93) y `ventaRepositoryPort.guardar(...)` (L104).
- `IngresoMercanciaService`: `inventarioRepositoryPort.save(...)` (L103) y `ingresoRepositoryPort.guardar(...)` (L114).

Y existe código redundante por duplicación: `VentaRepositoryAdapter` implementa **también** un `guardar()` concreto que delega en `save()`, cuando el propio puerto ya lo trae como `default`.

### Opciones

- **Opción A — Unificar en `guardar` (recomendada):** el resto del lenguaje de dominio ya es español (`obtenerPorId`, `obtenerPorAuthUserId`, `registrarVenta`, `asignarEncargado`). Los puertos son contratos de dominio, no la API de Spring Data. Se elimina todo par de alias y el `guardar` redundante del adapter de Venta. Dentro de los adaptadores, `springDataJpaRepository.save(...)` **se queda como está** (es el método de Spring Data, ajeno al contrato).
- **Opción B — Unificar en `save`:** alineación con la convención de Spring Data; implica renombrar en español → inglés, más discordante con los casos de uso.

**Decisión pendiente de confirmación.** Esfuerzo estimado: ~14 archivos, rename 100% mecánico (compilación + 38 tests como red de seguridad).

---

## 2. ¿`InventarioController` propio o sub-recurso de Tienda y CD?

**Estado:** ⚠️ Abierto · **Impacto:** Coherencia de la API · **Esfuerzo:** Medio (rutas + docs; frontend casi nulo)

### Estado actual

```http
GET  /api/inventario?tiendaId={id}&nombre=&categoria=&productoId=     # tiendaId OBLIGATORIO en query
GET  /api/inventario/centro-distribucion?centroDistribucionId={id}&…  # cdId OBLIGATORIO en query
PATCH /api/inventario/{id}/stock                                      # pendiente (pendientes.md §3)
```

### El conflicto

Tras los fixes 38 y 40, la API quedó con una **regla explícita**: el identificador del *padre* va en la URL (`POST /api/tiendas/{id}/ventas`, `POST /api/centros-distribucion/{id}/ingresos`, `PUT /{id}/encargado`). Pero en inventario el id de tienda/CD viaja como **query param obligatorio**, lo que lo convierte en un identificador de recurso disfrazado de filtro — justo lo que la regla quiere evitar.

A favor de **mantenerlo propio**: en *consultas* (GET de listas), filtros en query son práctica REST estándar (`?nombre=`), y `InventarioController` es también el hogar natural del futuro `PATCH /api/inventario/{id}/stock`, cuyo `{id}` es **la fila de inventario**, no la tienda/CD.

### Opciones

- **Opción A — Anidar las lecturas (coherencia total):**
  ```http
  GET /api/tiendas/{id}/inventario?nombre=&categoria=&productoId=
  GET /api/centros-distribucion/{id}/inventario?nombre=&categoria=&productoId=
  PATCH /api/inventario/{id}/stock          # sigue propio: {id} = fila de inventario
  ```
  `InventarioController` conserva solo el `PATCH`. Costo: ajustar HU-12, HU-16, Swagger, dto/README. **El frontend `inventario.html` aún no está conectado** (pendiente §3), así que el impacto real es casi nulo: es el mejor momento para decidirlo.
- **Opción B — Mantener controller propio con query params:** las consultas son colecciones filtradas multi-rol (Admin/Planificador/Encargado) y el id obligatorio en query no rompe nadie. Costo: la API queda con dos filosofías conviviendo (path-id en transacciones, query-id en lecturas).

**Decisión pendiente de confirmación.** Recomendación tácita de la serie fix38/40: **Opción A** — si se elige, hacerlo *antes* de conectar `inventario.html` para no romper nada.

---

## 3. Cobertura de pruebas desbalanceada

**Estado:** ⚠️ Activo · **Impacto:** Regresiones silenciosas · **Esfuerzo:** Medio

Los 38 tests actuales cubren inventario, ingresos, ventas, usuarios, ubicación y grafo. **Sin ninguna prueba:**

| Servicio sin tests | Operaciones expuestas |
| :--- | :--- |
| `TiendaService` | crear tienda, listar, asignar encargado |
| `CentroDistribucionService` | crear CD (capacidad), listar, asignar encargado |
| `ProductoService` | crear, editar, cambiar estado |

Son justo los servicios que ejecutan reglas de negocio (código único de tienda, aforo del CD, historial de ediciones de producto). Sugerencia: una clase por servicio siguiendo el molde de `VentaServiceTest` (Mockito + `AuthorizationService` real, como hace `UsuarioServiceTest`).

---

## 4. `codigo` de negocio en Tienda y CD: campo decorativo que no se usa

**Estado:** ⚠️ Abierto · **Impacto:** Modelo de datos + contrato de API · **Esfuerzo:** Bajo-medio

### Estado actual

`Tienda` y `CentroDistribucion` tienen un campo `String codigo` **además del `id` numérico** (que es la clave primaria real):

- Se **genera en el servicio** al crear: `TiendaService` → `"TND-" + UUID…substring(0,8)`; `CentroDistribucionService` → `"CD-" + UUID…`.
- Viaja en las respuestas (`TiendaResponse.codigo`, `CentroDistribucionResponse.codigo`) y en las columnas `codigo` de las tablas `tiendas` / `centros_distribucion` (creadas por `ddl-auto=update`).
- Aparece en 5 vistas web (`tiendas.html`, `centros-distribucion.html`, `admin.html`, `ingreso-mercancia.html`, `inventario.html`) y en `docs/dto/`.

### El problema

**Nadie lo consume.** No existe ningún endpoint ni consulta que busque por `codigo`; los `existsByCodigo(...)` de `TiendaRepositoryPort` y `CentroDistribucionJpaRepository` **no son invocados por ningún servicio** (API muerta). Es un identificador de negocio generado, guardado y mostrado… pero jamás usado para nada — un UUID acortado con prefijo, ilegible para un humano y sin valor funcional.

### Opciones

- **Opción A — Eliminar `codigo` de Tienda y CD (recomendada):** la PK numérica pasa a ser el único identificador. Tocaría: dominio (`Tienda`, `CentroDistribucion`), servicios (borrar las 2 líneas de generación), adapters y entidades JPA (quitar el mapeo), `*Response`, `existsByCodigo` muerto, 4 clases de test, 5 vistas web y docs. La columna en Supabase hay que borrarla a mano (`ALTER TABLE tiendas DROP COLUMN codigo;` + ídem `centros_distribucion`), porque `ddl-auto=update` **nunca elimina columnas**.
- **Opción B — Mantener `codigo` pero darle uso real:** exponer `GET /api/tiendas?codigo=…`, validar unicidad en la creación y volverlo la referencia legible de negocio. Implica más trabajo que eliminarlo y hoy no hay requisito que lo pida.
- **Opción C — Mantenerlo tal cual:** solo aclarar en README/docs que el identificador primario es el `id` y que `codigo` es un valor de negocio decorativo.

Nota: en `Producto` el campo equivalente **sí tiene sentido** (identificador de catálogo con el que filtra el frontend y con restricción `unique`); el caso de Tienda/CD es distinto.

---


## Cómo mantener este documento

- Cada ítem lleva **estado** (`⚠️ Abierto` / `✅ Resuelto` / `🔄 En curso`), impacto y esfuerzo.
- Cuando una decisión se ejecute, mover el detalle al fix correspondiente en `docs/Fix´s/` y marcarlo aquí como resuelto con enlace.
