# `RecomendacionReposicionRollbackIntegrationTest`

- **Ubicación:** `src/test/java/com/example/optiway/integration/RecomendacionReposicionRollbackIntegrationTest.java`
- **Caso de uso:** `AprobarRecomendacionUseCase` (HU-21)
- **Tecnología:** `@SpringBootTest`, `@MockitoSpyBean` sobre `OrdenReposicionRepositoryPort`, H2 en memoria y limpieza explícita en `@AfterEach`.

## Casos de prueba (1)

1. **`aprobarRecomendacion_CuandoFallaPersistenciaDeOrden_DebeHacerRollbackDeRecomendacion`**
   - Inyecta una excepción al llamar `ordenRepositoryPort.guardar(...)` después de aprobar una recomendación en estado `PROPUESTA`.
   - Verifica que la transacción hace rollback completo: la recomendación conserva su estado `PROPUESTA` y no quedan órdenes persistidas.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=RecomendacionReposicionRollbackIntegrationTest"
```
