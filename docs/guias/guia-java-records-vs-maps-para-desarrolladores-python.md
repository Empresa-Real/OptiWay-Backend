# Guía Práctica: Java Records vs. Maps en APIs REST (Para Desarrolladores Python / POO)

Esta guía explica de forma visual e intuitiva la transición de manejar payloads con diccionarios genéricos (`Map<String, Object>`) hacia **Java Records**, utilizando analogías directas con **Python**, `@dataclass` y **Pydantic / FastAPI**.

---

## 1. La Analogía Fundamental: Diccionario vs. Modelo Tipado

Si vienes de Python, probablemente estés familiarizado con estas dos formas de recibir datos en una función o endpoint:

| En Java | En Python | Concepto |
|---|---|---|
| `Map<String, Object> body` | `body: dict` | **Bolsa de datos genérica.** Acepta cualquier llave y cualquier tipo de dato sin validación previa. |
| `record MiRequest(...)` | `@dataclass(frozen=True)` o Pydantic `BaseModel` | **Molde estricto e inmutable.** Garantiza qué llaves existen y de qué tipo son antes de ejecutar tu lógica. |

---

## 2. El Problema del Diccionario Suelto (`Map<String, Object>`)

Imagina que en Python tienes un endpoint en Flask o FastAPI escrito así:

```python
# ENFOQUE ANTIGUO EN PYTHON (Diccionario genérico):
def asignar_encargado(tienda_id: int, body: dict):
    encargado_id = None
    
    # Código defensivo manual para evitar que reviente:
    if body is not None and "encargadoId" in body:
        valor = body["encargadoId"]
        if valor is not None:
            try:
                encargado_id = int(str(valor)) # ¿Qué pasa si envían "carlos"? -> ValueError
            except ValueError:
                return {"error": "El encargadoId debe ser un número entero"}, 400
                
    return tienda_service.asignar(tienda_id, encargado_id)
```

En Java con `Map<String, Object>` pasaba **exactamente lo mismo**:

```java
// ENFOQUE ANTIGUO EN JAVA (Code Smell):
@PutMapping("/{id}/encargado")
public Tienda asignarEncargado(
        @PathVariable Long id, 
        @RequestBody(required = false) Map<String, Object> body, 
        @AuthUserId UUID authUserId) {

    Long encargadoId = null;
    if (body != null && body.get("encargadoId") != null) {
        encargadoId = Long.valueOf(body.get("encargadoId").toString()); // Peligro de NumberFormatException
    }
    return asignarEncargadoTiendaUseCase.asignarEncargado(id, encargadoId, authUserId);
}
```

### ¿Por qué esto es una mala práctica en APIs de producción?

1. **Pérdida de Seguridad de Tipos (*Type Safety*):**
   - El tipo `Object` en Java es el equivalente a `Any` en Python. Puede ser un número `10`, un texto `"diez"`, un booleano `True` o una lista `[1, 2, 3]`.
   - Si el cliente envía `{"encargadoId": "invalido"}`, el código arroja un error interno en runtime en vez de ser rechazado en la entrada.
2. **Código defensivo repetitivo (Boilerplate):**
   - Obliga al desarrollador a comprobar `body != null`, si la clave existe, si el valor es null, convertir a String y luego castear a número.
3. **El endpoint es una "Caja Negra" (Sin Contrato):**
   - Nadie (ni otro programador del equipo, ni herramientas como Swagger/OpenAPI o Postman) puede saber qué campos espera recibir el endpoint a menos que abra el archivo y lea el código fuente línea por línea.
4. **No permite validaciones declarativas:**
   - En un `Map` o `dict` no puedes usar anotaciones de validación como `@NotNull`, `@Min(1)`, etc.

---

## 3. La Solución: ¿Qué es un Java Record?

Introducido como estándar a partir de Java 16, un **Record** es una clase compacta diseñada exclusivamente para ser un **portador transparente de datos inmutables** (*Data Transfer Object* o DTO).

En **Python**, el equivalente exacto es una `@dataclass` congelada:

```python
from dataclasses import dataclass

@dataclass(frozen=True)
class AsignarEncargadoRequest:
    encargado_id: int | None
```

En **Java**, se define en una sola línea elegante:

```java
package com.example.optiway.infrastructure.adapter.in.rest.dto;

public record AsignarEncargadoRequest(Long encargadoId) {}
```

### ¿Qué genera Java automáticamente por ti?

Sin que tengas que escribir código extra, el compilador genera:
* Atributos privados e inmutables (`final`).
* Constructor canónico con todos los argumentos: `new AsignarEncargadoRequest(12L)`.
* Métodos de lectura limpios: `request.encargadoId()` (equivalente a `request.encargado_id` en Python).
* Métodos de comparación e impresión: `equals()`, `hashCode()` y `toString()`.

---

## 4. ¿Cómo Funciona el Flujo Moderno con Jackson y Spring Boot?

Spring Boot utiliza una librería llamada **Jackson** (que actúa exactamente como **Pydantic** en FastAPI):

```
Petición HTTP entrante con JSON
      │
      ▼
┌────────────────────────────────────────────────────────┐
│  Jackson (Motor de Deserialización / Validación)      │
│  ¿El JSON cumple la estructura del Record?            │
└────────────────────────────────────────────────────────┘
      │
      ├── NO (ej. "encargadoId": "hola") ──► HTTP 400 Bad Request (Rechazo inmediato en puerta)
      │
      ▼ SÍ (ej. "encargadoId": 12)
Instancia el Record inmutable AsignarEncargadoRequest(12L)
      │
      ▼
Entra limpio y seguro al método del Controlador
```

### El nuevo código en el Controlador:

```java
// ENFOQUE MODERNO Y LIMPIO (Clean Code):
@PutMapping("/{id}/encargado")
public Tienda asignarEncargado(
        @PathVariable Long id,
        @RequestBody(required = false) AsignarEncargadoRequest request,
        @AuthUserId UUID authUserId) {

    Long encargadoId = request != null ? request.encargadoId() : null;
    return asignarEncargadoTiendaUseCase.asignarEncargado(id, encargadoId, authUserId);
}
```

---

## 5. Casos Prácticos Aplicados en el Proyecto

### Caso 1: Asignación de Encargado en Tiendas y Centros de Distribución
* **Archivo:** [AsignarEncargadoRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/AsignarEncargadoRequest.java)
* **Definición:**
  ```java
  public record AsignarEncargadoRequest(Long encargadoId) {}
  ```
* **Comportamiento:**
  - `{"encargadoId": 5}` ➔ `request.encargadoId() == 5L`
  - `{"encargadoId": null}` o `{}` ➔ `request.encargadoId() == null` (desvinculación limpia).
  - `{"encargadoId": "no_es_numero"}` ➔ `400 Bad Request` automático.

---

### Caso 2: Actualización de Rol de Usuario
* **Archivo:** [ActualizarRolRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/ActualizarRolRequest.java)
* **Definición:**
  ```java
  public record ActualizarRolRequest(Rol rol) {}
  ```
* **En Python sería:**
  ```python
  from enum import Enum
  from pydantic import BaseModel

  class Rol(str, Enum):
      ADMINISTRADOR = "ADMINISTRADOR"
      ENCARGADO_TIENDA = "ENCARGADO_TIENDA"
      ENCARGADO_CD = "ENCARGADO_CD"
      PLANIFICADOR = "PLANIFICADOR"

  class ActualizarRolRequest(BaseModel):
      rol: Rol
  ```
* **Comportamiento:**
  - Antes se recibía un `Map<String, String>` y se hacía un `try/catch(IllegalArgumentException)` manual con `Rol.valueOf(...)` dentro del controlador.
  - Ahora Jackson valida que el string del JSON pertenezca estrictamente al enum `Rol`. Si envían `{"rol": "HACKER"}`, se rechaza automáticamente con `400 Bad Request`.

---

## 6. Cuadro Resumen: Buenas Prácticas

| Aspecto | Con `Map<String, Object>` (Mala Práctica) | Con `record` / DTO (Buena Práctica) |
|---|---|---|
| **Seguridad de Tipos** | Nula (todo es `Object` / `Any`). | Total (verificado en tiempo de compilación). |
| **Validación de Errores** | Manual, repetitiva y dispersa. | Automática en la barrera HTTP (Jackson). |
| **Documentación API** | Oculta; requiere inspeccionar código. | Auto-documentable en OpenAPI / Swagger. |
| **Mantenibilidad** | Frágil ante cambios de nombres de llaves. | Refactorizable con seguridad en el IDE. |
| **Arquitectura Hexagonal** | Fuga detalles del protocolo HTTP. | Aísla los adaptadores de entrada mediante DTOs formales. |
