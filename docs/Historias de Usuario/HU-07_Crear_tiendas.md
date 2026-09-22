# Guía de continuación: HU-07 — Crear Tiendas

## Estado actual

La API permite registrar y administrar tiendas dentro del sistema mediante el endpoint:
`POST /api/tiendas`

El cuerpo JSON esperado para registrar una tienda es:

```json
{
  "nombre": "Tienda Central Medellín",
  "direccion": "Calle 50 # 45-10, Medellín",
  "telefono": "6041234567",
  "encargadoId": "a1b2c3d4-e5f6-7890-abcd-1234567890ab"
}

```

### Reglas de negocio y roles permitidos

* **Atributos requeridos:** `nombre` y `direccion` son obligatorios.
* **Asignación de encargado:** La tienda se vincula opcionalmente a un usuario local mediante su `encargadoId` (UUID de la tabla local `usuarios`). El usuario asignado debe tener el rol `ENCARGADO_TIENDA`.
* **Restricción de acceso:** Solo un usuario autenticado cuyo perfil local tenga el rol `ADMINISTRADOR` o `PLANIFICADOR` puede crear o modificar tiendas.
* **Unicidad:** El nombre de la tienda no puede estar duplicado (comparación case-insensitive).

---

## Respuestas esperadas

| Código HTTP | Condición |
| --- | --- |
| **`201 Created`** | Tienda creada exitosamente y persistida en base de datos. |
| **`400 Bad Request`** | Datos de entrada inválidos (ej. nombre vacío, `encargadoId` con formato inválido o rol no permitido). |
| **`409 Conflict`** | Ya existe una tienda registrada con el mismo nombre. |
| **`401 Unauthorized`** | Falta el header `Authorization: Bearer <access_token>` o el JWT de Supabase es inválido/expirado. |
| **`403 Forbidden`** | El usuario autenticado no tiene permisos de `ADMINISTRADOR` o `PLANIFICADOR`. |

### Endpoints de consulta

* **Listar todas las tiendas:** `GET /api/tiendas`
* **Consultar una tienda específica:** `GET /api/tiendas/{id}`

---

## Prueba local

1. Configura las variables de entorno en la configuración de ejecución de IntelliJ / VS Code:
```properties
DB_PASSWORD=tu_contraseña_local
SUPABASE_PROJECT_REF=tu_project_ref

```


2. Inicia la aplicación Spring Boot (`OptiwayApplication`).
3. Inicia sesión en la interfaz web de Supabase o mediante el frontend para obtener un `access_token` válido de un usuario con rol `ADMINISTRADOR` o `PLANIFICADOR`.
4. Realiza una petición desde Postman o cURL:
* **URL:** `http://localhost:8080/api/tiendas`
* **Método:** `POST`
* **Headers:**
* `Content-Type: application/json`
* `Authorization: Bearer <access_token_de_supabase>`


* **Body (raw JSON):**
```json
{
  "nombre": "Tienda Norte",
  "direccion": "Carrera 43A # 1-50",
  "telefono": "3001234567"
}

```





---

## Flujo de autorización y arquitectura limpia

El procesamiento de una solicitud para la HU-07 sigue el siguiente flujo desacoplado:

```text
Cliente (Frontend / Postman)
  ↓ [Authorization: Bearer <JWT>]
SecurityFilterChain (Spring Security valida firma y exp de Supabase)
  ↓
TiendaController (Extrae el JWT e invoca el caso de uso)
  ↓
CrearTiendaUseCase / TiendaService
  ↓ (Valida rol del usuario mediante auth_user_id y comprueba no duplicidad de nombre)
TiendaRepositoryAdapter (JPA Persistencia)
  ↓
Respuesta HTTP 201 Created

```

### Componentes involucrados

1. **`TiendaController`:** Expone los endpoints REST y gestiona la conversión de DTOs a entidades de dominio.
2. **`CrearTiendaUseCase` / `TiendaService`:** Aplica la lógica de negocio, validando que el creador tenga permisos adecuados y que el encargado asignado exista.
3. **`TiendaRepositoryPort` & `TiendaRepositoryAdapter`:** Define la interfaz de persistencia e implementa las consultas JPA (`findByNombreIgnoreCase`).

---

## Reglas que ya están implementadas

* El nombre y la dirección de la tienda se sanitizan y recortan espacios en blanco antes de guardarse.
* Comprobación de nombres duplicados ignorando mayúsculas y minúsculas.
* La entidad JPA cuenta con una restricción única (`UNIQUE`) en la columna `nombre`.
* Integración con Spring Security para la lectura transparente del UUID (`sub`) contenido en el JWT de Supabase.
* Generación automática de código de tienda de negocio (`TND-XXXXXXXX`).
* Endpoint de consulta `GET /api/tiendas` implementado para listar las tiendas registradas y utilizarlas en la asignación de cobertura de Centros de Distribución.

---

## Trabajo pendiente

* **Asociación de Centro de Distribución (CD):** Vinculación resuelta en HU-09 mediante la tabla intermedia relacional `centro_distribucion_tiendas` (@ManyToMany), permitiendo a cada CD asociar las tiendas de su zona de cobertura.
* **Geolocalización:** Integrar la conversión automática de la dirección en coordenadas de latitud y longitud (geocodificación) para la optimización de rutas de entrega.
* **Deshabilitación/Inactivación:** Implementar borrado lógico (`activo = false`) para evitar eliminar físicamente tiendas con historial de pedidos.

---

## Siguiente paso recomendado

Agregar pruebas unitarias e integrales para la HU-07:

1. Pruebas de integración para `POST /api/tiendas` verificando creación exitosa con rol `ADMINISTRADOR`.
2. Prueba de rechazo `409 Conflict` al intentar registrar un nombre de tienda existente.
3. Prueba de acceso `403 Forbidden` cuando un usuario con rol `ENCARGADO_CD` o `ENCARGADO_TIENDA` intenta registrar una tienda.
4. Validación de persistencia y mapeo correcto en la base de datos de pruebas (H2 / PostgreSQL Testcontainers).