# Diagramas de OptiWay

Este directorio contiene los diagramas editables del proyecto, elaborados en PlantUML.

## 1. Diagramas de secuencia

Ubicación: `secuencia/`

| Archivo | Historia de usuario | Descripción |
|---|---|---|
| `HU-13-Ajustar-Inventario.puml` | HU-13 | Flujo de ajuste de inventario, validaciones, autorización y registro del historial. |
| `HU-20-Crear-Recomendacion-Reposicion.puml` | HU-20 | Flujo de creación de recomendaciones, validación de stock y cobertura. |

## 2. Diagramas C4

Ubicación: `c4/`

| Archivo | Nivel C4 | Descripción |
|---|---|---|
| `C4-Contexto.puml` | Nivel 1 | Usuarios, sistema OptiWay y servicios externos. |
| `C4-Contenedores.puml` | Nivel 2 | Aplicación web, backend, autenticación y base de datos. |
| `C4-Componentes-Backend.puml` | Nivel 3 | Controladores, aplicación, dominio y adaptadores del backend. |

## 3. Tecnologías y arquitectura representadas

- Backend: Java y Spring Boot.
- API: REST.
- Arquitectura: hexagonal, organizada en dominio, aplicación y adaptadores.
- Persistencia: PostgreSQL mediante JPA/Hibernate.
- Autenticación: Supabase Auth y JWT.
- Documentación API: Springdoc/OpenAPI.

## 4. Cómo visualizar los diagramas

Los archivos `.puml` son fuentes editables de PlantUML. Para generar imágenes, se pueden abrir con una herramienta compatible con PlantUML y exportar a PNG o SVG.

Los diagramas representan la arquitectura y los flujos identificados en el código y la configuración del proyecto.

## Imágenes generadas

Los diagramas están disponibles en PNG y SVG dentro de `imagenes/`.

### Diagramas C4

- Contexto: [PNG](imagenes/C4-Contexto.png) | [SVG](imagenes/C4-Contexto.svg)
- Contenedores: [PNG](imagenes/C4-Contenedores.png) | [SVG](imagenes/C4-Contenedores.svg)
- Componentes Backend: [PNG](imagenes/C4-Componentes-Backend.png) | [SVG](imagenes/C4-Componentes-Backend.svg)

### Diagramas de secuencia

- HU-13 Ajustar inventario: [PNG](imagenes/HU-13-Ajustar-Inventario.png) | [SVG](imagenes/HU-13-Ajustar-Inventario.svg)
- HU-20 Crear recomendación de reposición: [PNG](imagenes/HU-20-Crear-Recomendacion-Reposicion.png) | [SVG](imagenes/HU-20-Crear-Recomendacion-Reposicion.svg)
