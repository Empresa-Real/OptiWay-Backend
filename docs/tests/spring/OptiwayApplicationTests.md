# `OptiwayApplicationTests`

- **Ubicación:** `src/test/java/com/example/optiway/integration/OptiwayApplicationTests.java`
- **Alcance:** Arranque del contexto de Spring Boot (`@SpringBootTest`).

## Casos de prueba (1)

1. **`contextLoads`**
   - Verifica que todos los beans, configuraciones (`SecurityConfig`, `OpenApiConfig`), controladores, servicios y repositorios JPA cargan correctamente.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=OptiwayApplicationTests"
```
