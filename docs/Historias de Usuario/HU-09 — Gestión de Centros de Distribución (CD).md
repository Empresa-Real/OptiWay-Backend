# Guía de continuación: HU-09 — Gestión de Centros de Distribución (CD)

## Estado actual

La API permite registrar, listar y estructurar los Centros de Distribución (CD) dentro de la red logística mediante el endpoint:
`POST /api/centros-distribucion`

El cuerpo JSON esperado para registrar un Centro de Distribución es:

```json
{
  "nombre": "CD Principal Valle de Aburrá",
  "direccion": "Autopista Sur # 65-120, Itagüí",
  "capacidad": 15000,
  "tiendasAbastecidasIds": [1, 2, 3],
  "encargadoId": "c3d4e5f6-a1b2-7890-abcd-0987654321fe"
}
```

### Reglas de negocio y roles permitidos

* **Atributos requeridos:** `nombre`, `direccion` y `capacidad` (valor numérico positivo) son obligatorios.
* **Zona de cobertura (`tiendasAbastecidasIds`):** Lista opcional de identificadores numéricos (`int8`) de tiendas existentes que el centro abastecerá, mapeadas mediante relación `@ManyToMany` en la tabla `centro_distribucion_tiendas`.
* **Asignación de encargado:** El CD se vincula opcionalmente a un usuario local mediante su `encargadoId` (UUID de la tabla local `usuarios`). El usuario asignado debe poseer el rol `ENCARGADO_CD`.
* **Restricción de acceso:** Solo un usuario autenticado cuyo perfil local tenga el rol `ADMINISTRADOR` o `PLANIFICADOR` tiene permisos para crear o modificar Centros de Distribución.
* **Unicidad:** El nombre del Centro de Distribución debe ser único en el sistema (comparación case-insensitive).

---

## Respuestas esperadas

| Código HTTP | Condición |
| --- | --- |
| **`201 Created`** | Centro de Distribución creado y guardado correctamente en la base de datos. |
| **`400 Bad Request`** | Datos de entrada inválidos (ej. nombre vacío, capacidad menor o igual a cero, o `encargadoId` sin el rol adecuado). |
| **`409 Conflict`** | Ya existe un Centro de Distribución registrado con el mismo nombre. |
| **`401 Unauthorized`** | El token JWT de Supabase falta, es inválido o se encuentra vencido. |
| **`403 Forbidden`** | El usuario autenticado carece de los permisos requeridos (`ADMINISTRADOR` o `PLANIFICADOR`). |

### Endpoints de consulta

* **Listar todos los CD:** `GET /api/centros-distribucion`
* **Consultar un CD por ID:** `GET /api/centros-distribucion/{id}`

---

## Flujo de autorización y arquitectura limpia

El procesamiento de solicitudes para la HU-09 sigue el flujo desacoplado del sistema:

```text
Cliente (Frontend / Postman)
  ↓ [Authorization: Bearer <JWT>]
SecurityFilterChain (Spring Security valida el JWT de Supabase)
  ↓
CentroDistribucionController (Recibe el DTO y gestiona la respuesta HTTP)
  ↓
CrearCentroDistribucionUseCase / CentroDistribucionService
  ↓ (Valida roles del creador y del encargado, comprueba disponibilidad del nombre)
CentroDistribucionRepositoryAdapter (Persistencia JPA)
  ↓
Respuesta HTTP 201 Created

```

### Componentes involucrados

1. **`CentroDistribucionController`:** Expone los endpoints REST (`/api/centros-distribucion`) y mapea las peticiones de entrada.
2. **`CrearCentroDistribucionUseCase` / `CentroDistribucionService`:** Contiene las reglas de negocio de logística básica, validación de capacidad y roles.
3. **`CentroDistribucionRepositoryPort` & `Adapter`:** Maneja la interacción con la base de datos relacional mediante Spring Data JPA.

---

## Reglas que ya están implementadas

* Los campos de texto (`nombre` y `direccion`) se recortan de espacios innecesarios antes de ser procesados.
* Verificación de unicidad del nombre mediante consulta case-insensitive (`findByNombreIgnoreCase`).
* Restricción a nivel de base de datos (`UNIQUE`) sobre la columna `nombre` de la tabla `centros_distribucion`.
* Integración con Spring Security para validar el `sub` (UUID de Supabase Auth) y asociar permisos del perfil local.
* Generación automática de código de negocio (`CD-XXXXXXXX`).
* Vinculación relacional `@ManyToMany` mediante la tabla intermedia `centro_distribucion_tiendas` con Foreign Keys hacia `centros_distribucion` y `tiendas` para gestionar la zona de cobertura.

---

## Trabajo pendiente

* **Asociación de Flota y Vehículos:** Vincular la disponibilidad de camiones y conductores a cada Centro de Distribución para la gestión de envíos.
* **Métricas de Ocupación:** Implementar el cálculo en tiempo real de la capacidad utilizada vs. la capacidad total de almacenamiento.
* **Geocodificación y Puntos de Salida:** Asignar coordenadas GPS (latitud y longitud) fijas a cada CD para utilizarlas como punto de origen en los algoritmos de optimización de rutas (como la solución al problema Knapsack / Ruteo).

---

## Siguiente paso recomendado

Escribir la suite de pruebas unitarias y de integración para la HU-09:

1. Validar la creación exitosa con `201 Created` enviando datos válidos y rol `ADMINISTRADOR`.
2. Verificar respuesta `400 Bad Request` al ingresar capacidades negativas o nulas.
3. Comprobar rechazo `409 Conflict` cuando el nombre del CD ya exista.
4. Asegurar respuesta `403 Forbidden` si un usuario con rol `ENCARGADO_TIENDA` intenta crear un CD.