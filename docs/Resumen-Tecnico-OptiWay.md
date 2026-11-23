# Resumen técnico del proyecto OptiWay

## 1. Descripción

OptiWay es un sistema de gestión logística e inventarios que permite administrar productos, tiendas, centros de distribución y operaciones relacionadas con las existencias y la reposición de mercancía.

## 2. Arquitectura

El backend utiliza una arquitectura hexagonal organizada en:

- `domain/model`: modelos y reglas del dominio.
- `application/port/in`: puertos de entrada y casos de uso.
- `application/port/out`: contratos para servicios externos y persistencia.
- `application/service`: implementación de los servicios de aplicación.
- `infrastructure/adapter/in/rest`: controladores y adaptadores REST.
- `infrastructure/adapter/out/persistence`: persistencia mediante JPA.
- `infrastructure/adapter/out/auth`: integración de autenticación.
- `infrastructure/config` e `infrastructure/security`: configuración y seguridad.

## 3. Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje backend | Java |
| Framework | Spring Boot |
| API | REST |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL en Supabase |
| Autenticación | Supabase Auth y JWT |
| Documentación API | Springdoc / OpenAPI |
| Pruebas | JUnit, Mockito y H2 |
| Análisis de dependencias | OWASP Dependency-Check |
| Pruebas dinámicas de seguridad | OWASP ZAP |

## 4. Roles del sistema

Los roles definidos en el dominio son:

- `ADMINISTRADOR`
- `ENCARGADO_TIENDA`
- `ENCARGADO_CD`
- `PLANIFICADOR`

La autorización se aplica según la operación y el recurso al que intenta acceder cada usuario.

## 5. Funcionalidades técnicas destacadas

### HU-13 - Ajustar inventario

Permite ajustar las existencias de un producto en una tienda o centro de distribución. Valida la ubicación, los permisos y la cantidad, actualiza el inventario y registra el historial del ajuste.

### HU-20 - Crear recomendación de reposición

Permite crear una propuesta de reposición con producto, cantidad, centro de distribución de origen y tienda de destino. Valida stock disponible y cobertura logística. La creación de la propuesta no ejecuta el traslado ni descuenta existencias.

### Seguridad

El proyecto utiliza autenticación basada en JWT, autorización por roles y controles de seguridad HTTP. Se realizaron análisis de dependencias y pruebas dinámicas documentadas en la carpeta de evidencias.

### Registro de eventos

El backend cuenta con registros de operaciones relevantes para facilitar el seguimiento y diagnóstico de eventos del sistema.

## 6. Pruebas automatizadas

Las clases de prueba de HU-13 y HU-20 se ejecutaron conjuntamente:

| Clase | Pruebas | Fallos | Errores |
|---|---:|---:|---:|
| AjustarInventarioServiceTest | 4 | 0 | 0 |
| RecomendacionReposicionServiceTest | 14 | 0 | 0 |
| **Total** | **18** | **0** | **0** |

La suite general también registró `BUILD SUCCESS` en la ejecución documentada del 22 de noviembre de 2026.

## 7. Estructura de documentación y evidencias

- `docs/diagramas/README.md`: índice de diagramas.
- `docs/diagramas/secuencia/`: diagramas de secuencia HU-13 y HU-20.
- `docs/diagramas/c4/`: diagramas C4 de contexto, contenedores y componentes.
- `docs/Matriz-Pruebas-HU-13-HU-20.md`: matriz de pruebas.
- `evidencias/pruebas/`: reportes XML y registro de ejecución.
- `evidencias/seguridad/`: reportes de análisis SCA y DAST.

## 8. Consideraciones

Este documento resume la arquitectura y las funcionalidades verificadas en el código y los reportes disponibles. La disponibilidad de una funcionalidad en la interfaz web debe comprobarse por separado de su implementación en el backend.
