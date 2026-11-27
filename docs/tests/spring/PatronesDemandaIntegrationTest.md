# `PatronesDemandaIntegrationTest`

- **Ubicación:** `src/test/java/com/example/optiway/integration/PatronesDemandaIntegrationTest.java`
- **Endpoint:** `GET /api/demanda/patrones` (HU-19)
- **Tecnología:** `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Transactional`, H2 en memoria y `jwt()` (`spring-security-test`).

## Casos de prueba (7)

1. **`consultaConTiendasRepetidasDevuelveSerieAgrupadaYExcluyeLimitesDelRango`**: Envía `tiendaIds` repetidos, valida deduplicación, agrupación diaria (incluyendo días sin ventas en `0`), exclusión fuera de `[desde, hasta]` y cálculo de mayor/menor rotación.
2. **`aceptaTiendaIdsSeparadosPorComa`**: Valida que `tiendaIds=1,2` en formato CSV responde `200 OK`.
3. **`rechazaFechaConFormatoNoIso`**: Envía fecha no `ISO-8601` y verifica `400 Bad Request`.
4. **`rechazaRangoSuperiorATrescientosSesentaYSeisDias`**: Envía un período mayor a 366 días y verifica `400 Bad Request`.
5. **`rechazaMasDeCincuentaTiendas`**: Envía 51 IDs de tienda y verifica `400 Bad Request`.
6. **`devuelve403ParaUnAdministrador`**: Un usuario con rol `ADMINISTRADOR` (no `PLANIFICADOR`) recibe `403 Forbidden`.
7. **`devuelve404CuandoNoExisteElProductoOAlgunaTienda`**: Producto o tienda inexistente responde `404 Not Found`.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=PatronesDemandaIntegrationTest"
```
