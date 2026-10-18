# Informe de Correcciones y Optimización: HU-16 — Consulta de Inventario en Centros de Distribución (Fix 8)

Este documento detalla la integración de la **HU-16 (Consultar Inventario de Centro de Distribución)** en la rama `main`, subsanando las fallas de control de acceso por rol, eliminando el problema de rendimiento por consultas redundantes N+1 y adaptando el endpoint al estándar de seguridad con `@AuthUserId`.

---

## 1. Resumen Ejecutivo y Alcance

La **HU-16** habilita la consulta de existencias físicas y stock bajo en Centros de Distribución a través del endpoint:
* `GET /api/inventario/centro-distribucion?centroDistribucionId={id}&nombre={nombre}&categoria={categoria}&productoId={id}`

Permite filtrar existencias acumuladas por nombre o categoría de producto, y calcula de forma dinámica el flag `stockBajo` cuando `cantidadActual < stockMinimo`.

---

## 2. Brechas Detectadas Previas al Fix

| Componente | Brecha Identificada | Impacto |
| :--- | :--- | :--- |
| **Control de Acceso / Roles** | `ConsultarInventarioCDService` contenía una condición estricta: `if (usuario.getRol() != Rol.ENCARGADO_CD)`. | Bloqueaba con `403 Forbidden` a usuarios con rol `ADMINISTRADOR` y `PLANIFICADOR`, impidiendo la gestión logística global. |
| **Rendimiento / Base de Datos** | En el pipeline de Streams se ejecutaba `productoRepositoryPort.findById(i.getProductoId())` hasta 3 veces consecutivas por cada registro de inventario. | Problema de **Query N+1 x 3**. Para un CD con 50 productos, ejecutaba hasta **150 consultas `SELECT` a PostgreSQL** en una sola petición. |
| **Adaptador REST** | `InventarioCentroDistribucionController` usaba `@AuthenticationPrincipal Jwt jwt` y parseo manual `UUID.fromString(jwt.getSubject())`. | Inconsistencia con la arquitectura estandarizada en Fix 7 (`@AuthUserId`) y riesgo de error 500 ante tokens malformados. |

---

## 3. Soluciones Implementadas

### A. Autorización Multirrol Flexible
En [ConsultarInventarioCDService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/ConsultarInventarioCDService.java):
* Se homologó la lógica de autorización con la establecida en `InventarioService` (Tiendas):
  ```java
  boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;
  boolean esPlanificador = usuario.getRol() == Rol.PLANIFICADOR;
  boolean esEncargado = usuario.getRol() == Rol.ENCARGADO_CD 
          && centro.getEncargadoId() != null 
          && usuario.getId().equals(centro.getEncargadoId());

  if (!esAdmin && !esPlanificador && !esEncargado) {
      throw new AccesoDenegadoException(
          "No tiene permisos para consultar el inventario de este centro de distribución");
  }
  ```
* `ADMINISTRADOR` y `PLANIFICADOR` pueden supervisar el inventario de cualquier CD del sistema.
* `ENCARGADO_CD` únicamente puede consultar el CD que tiene formalmente asignado.

### B. Optimización del Problema N+1 y Filtro Único
* Se eliminó el triple llamado repetido a la base de datos dentro de los `.filter()` y `.map()`.
* Se extraen los `productoIds` únicos del inventario y se pre-cargan en un `Map<Long, Producto>` en memoria:
  ```java
  Set<Long> productoIds = inventarios.stream()
          .map(Inventario::getProductoId)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());

  Map<Long, Producto> productosMap = new HashMap<>();
  for (Long pId : productoIds) {
      productoRepositoryPort.findById(pId).ifPresent(p -> productosMap.put(pId, p));
  }
  ```
* Todo el filtrado por nombre, categoría y enriquecimiento de `nombreProducto`, `categoriaProducto` y `stockBajo` se realiza en un único recorrido en memoria a partir del mapa.

### C. Estandarización del Controlador REST
En [InventarioCentroDistribucionController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/InventarioCentroDistribucionController.java):
* Se reemplazó el uso directo de `Jwt` por `@AuthUserId UUID authUserId`, delegando la resolución segura al `AuthUserIdArgumentResolver`.

---

## 4. Matriz Comparativa

| Criterio | Implementación Original HU-16 | Implementación con Fix 8 |
| :--- | :--- | :--- |
| **Acceso Administrador** | ❌ Bloqueado (`403 Forbidden`) | ✅ Permitido globalmente |
| **Acceso Planificador** | ❌ Bloqueado (`403 Forbidden`) | ✅ Permitido globalmente |
| **Acceso Encargado CD** | ✅ Solo su CD | ✅ Solo su CD |
| **Consultas a BD por Producto** | 3 consultas por fila | 1 sola consulta agrupada |
| **Inyección de Identidad** | `UUID.fromString(jwt.getSubject())` | `@AuthUserId UUID authUserId` |

---

## 5. Estado de Compilación

* **Herramienta:** Maven Wrapper (`mvnw compile -DskipTests`)
* **Resultado:** `BUILD SUCCESS`
