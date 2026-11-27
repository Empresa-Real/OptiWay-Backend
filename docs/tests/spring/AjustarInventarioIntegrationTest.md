# `AjustarInventarioIntegrationTest`

- **Ubicación:** `src/test/java/com/example/optiway/integration/AjustarInventarioIntegrationTest.java`
- **Endpoint:** `POST /api/inventario/ajustes` (HU-13 / HU-17)
- **Tecnología:** `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Transactional`, H2 en memoria y `jwt()` (`spring-security-test`).

## Casos de prueba (3)

1. **`debeAumentarInventarioYRegistrarHistorial`**
   - Envía `{"tiendaId": ..., "productoId": ..., "nuevaCantidad": 30}` sobre un inventario inicial de `20`.
   - Verifica `201 Created`, `cantidadAnterior: 20`, `diferencia: 10`, `cantidadNueva: 30`, actualización en `InventarioJpaRepository` y registro en `AjusteInventarioJpaRepository`.
2. **`debeDisminuirInventarioYRegistrarHistorial`**
   - Envía `nuevaCantidad: 12` sobre un inventario inicial de `20`.
   - Verifica `201 Created`, `cantidadAnterior: 20`, `diferencia: -8`, `cantidadNueva: 12` y persistencia en inventario e historial.
3. **`noDebePermitirInventarioNegativoNiRegistrarHistorial`**
   - Envía `nuevaCantidad: -1`.
   - Verifica `400 Bad Request`, que el inventario siga en `20` y que no se cree ningún registro de ajuste.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=AjustarInventarioIntegrationTest"
```
