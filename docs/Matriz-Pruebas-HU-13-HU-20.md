# Matriz de pruebas - HU-13 y HU-20

## 1. HU-13 - Ajustar inventario

| ID | Escenario | Método automatizado | Resultado esperado |
|---|---|---|---|
| HU13-01 | Ajustar inventario de una tienda con datos válidos | `ajustarInventarioTienda_CuandoConteoEsValido_DebeActualizarYGuardarHistorial` | Actualiza la cantidad y registra el historial del ajuste. |
| HU13-02 | Ajustar inventario de un centro de distribución con datos válidos | `ajustarInventarioCD_CuandoConteoEsValido_DebeActualizarYGuardarHistorial` | Actualiza la cantidad y registra el historial del ajuste. |
| HU13-03 | Intentar establecer una cantidad negativa | `ajustarInventario_CuandoNuevaCantidadEsNegativa_DebeLanzarExcepcion` | Rechaza la operación mediante una excepción y no permite una cantidad negativa. |

## 2. HU-20 - Crear recomendación de reposición

| ID | Escenario | Método automatizado | Resultado esperado |
|---|---|---|---|
| HU20-01 | Crear recomendación con datos válidos y stock suficiente | `crearRecomendacion_CuandoDatosSonValidos_DebeGuardarPropuesta` | Guarda la recomendación con estado Propuesta. |
| HU20-02 | Intentar crear recomendación con stock insuficiente | `crearRecomendacion_CuandoStockEsInsuficiente_DebeLanzarExcepcionYSinGuardar` | Rechaza la operación y no guarda la recomendación. |
| HU20-03 | Crear recomendación fuera de cobertura sin confirmación | `crearRecomendacion_FueraDeCoberturaSinConfirmacion_DebeLanzarConflicto` | Devuelve conflicto y solicita confirmar o cambiar el centro de distribución. |

## 3. Ejecución de las pruebas

Las seis pruebas corresponden a métodos automatizados existentes en:

- `AjustarInventarioServiceTest.java`
- `RecomendacionReposicionServiceTest.java`

Las pruebas fueron identificadas en el código fuente y posteriormente ejecutadas; sus resultados se presentan en la sección 5.

## 4. Alcance

- HU-13 valida ajustes en tienda y centro de distribución, así como el rechazo de cantidades negativas.
- HU-20 valida la creación de propuestas, la disponibilidad de stock y la confirmación cuando el centro de distribución está fuera de cobertura.
- La creación de una recomendación no ejecuta el traslado ni descuenta existencias del inventario.

## 5. Resultados de ejecución

**Fecha:** 22 de noviembre de 2026.

| Clase de prueba | Pruebas ejecutadas | Fallidas | Errores | Omitidas | Resultado |
|---|---:|---:|---:|---:|---|
| `AjustarInventarioServiceTest` (HU-13) | 4 | 0 | 0 | 0 | APROBADO |
| `RecomendacionReposicionServiceTest` (HU-20) | 14 | 0 | 0 | 0 | APROBADO |
| **Total** | **18** | **0** | **0** | **0** | **BUILD SUCCESS** |

Los resultados se respaldan con los reportes XML de Surefire y el registro de ejecución de Maven guardados en `evidencias/pruebas/`.
