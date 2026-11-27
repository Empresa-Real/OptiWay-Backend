# `RepositoriosConsultasDerivadasTest`

- **Ubicación:** `src/test/java/com/example/optiway/integration/RepositoriosConsultasDerivadasTest.java`
- **Alcance:** Repositorios Spring Data JPA contra H2.

## Casos de prueba (1)

1. **`consultasPorRelacionSeEjecutan`**
   - Verifica que las consultas derivadas basadas en propiedades de relaciones (`_Id`, `existsByTiendaDestino_IdAndEstadoIn`, etc.) compilan y ejecutan sin errores de mapeo JPA/Hibernate.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=RepositoriosConsultasDerivadasTest"
```
