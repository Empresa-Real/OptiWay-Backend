# Documentación del Sistema OptiWay

**Proyecto:** OptiWay
**Tipo:** Sistema de gestión logística e inventarios
**Backend:** Java y Spring Boot
**Base de datos:** PostgreSQL mediante Supabase
**Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## 1. Introducción

OptiWay es un sistema orientado a la gestión de inventarios y operaciones logísticas. Permite administrar productos, tiendas, centros de distribución, existencias y procesos relacionados con la reposición de mercancía.

El backend proporciona una API REST que centraliza las reglas de negocio, la validación de operaciones, la autorización de usuarios y la persistencia de información.

Esta documentación describe la estructura técnica, los componentes principales, los mecanismos de seguridad y las funcionalidades implementadas.

## 2. Objetivos del sistema

- Centralizar la administración de productos y ubicaciones logísticas.
- Consultar y controlar las existencias de inventario.
- Registrar ajustes de inventario y conservar su historial.
- Gestionar operaciones de ingreso y venta de mercancía.
- Generar propuestas de reposición entre centros de distribución y tiendas.
- Controlar el acceso según la identidad, el rol y los permisos del usuario.
- Facilitar el seguimiento de operaciones mediante registros y evidencias.

## 3. Tecnologías utilizadas

| Área | Tecnología |
|---|---|
| Lenguaje | Java |
| Framework backend | Spring Boot |
| API | REST |
| Persistencia | Spring Data JPA y Hibernate |
| Base de datos | PostgreSQL |
| Plataforma de base de datos y autenticación | Supabase |
| Autenticación | Supabase Auth y JWT |
| Contrato de API | OpenAPI 3 y Springdoc |
| Interfaz de pruebas | Swagger UI |
| Pruebas automatizadas | JUnit y Mockito |
| Base de datos para pruebas | H2, según configuración de pruebas |
| Análisis de seguridad | OWASP ZAP |
| Análisis de dependencias | OWASP Dependency-Check |
| Diagramas | PlantUML y notación C4 |

## 4. Arquitectura del sistema

El backend utiliza arquitectura hexagonal, también conocida como arquitectura de puertos y adaptadores.

Su propósito es separar las reglas del negocio de los detalles de infraestructura, como HTTP, bases de datos y servicios externos.

### 4.1. Organización de paquetes

| Paquete | Responsabilidad |
|---|---|
| `domain/model` | Modelos y conceptos del dominio. |
| `application/port/in` | Contratos de los casos de uso. |
| `application/port/out` | Contratos para persistencia y servicios externos. |
| `application/service` | Implementación de los casos de uso y reglas de aplicación. |
| `infrastructure/adapter/in/rest` | Controladores y adaptadores HTTP. |
| `infrastructure/adapter/out/persistence` | Adaptadores y entidades de persistencia. |
| `infrastructure/adapter/out/auth` | Integración con autenticación externa. |
| `infrastructure/config` | Configuración de la aplicación. |
| `infrastructure/security` | Resolución de identidad y mecanismos de seguridad. |

### 4.2. Flujo general de una petición

1. El cliente envía una petición HTTP al controlador REST.
2. El controlador recibe los datos y los transforma en una solicitud o comando.
3. El caso de uso correspondiente ejecuta la operación.
4. El servicio aplica validaciones y reglas de negocio.
5. Cuando se requiere información persistente, utiliza los puertos de salida.
6. Los adaptadores implementan la comunicación con PostgreSQL u otros servicios.
7. El resultado se transforma en una respuesta HTTP.

Esta organización permite mantener separadas las responsabilidades y facilita las pruebas unitarias.

## 5. Módulos funcionales

El backend contempla funcionalidades relacionadas con:

- Usuarios y roles.
- Productos.
- Tiendas.
- Centros de distribución.
- Inventarios.
- Ajustes de inventario.
- Ingreso de mercancía.
- Registro de ventas.
- Recomendaciones de reposición.
- Órdenes de reposición.
- Autenticación y autorización.

La disponibilidad de cada operación depende de los permisos definidos para el usuario.

## 6. Autenticación y autorización

### 6.1. Autenticación

El sistema utiliza Supabase Auth para la autenticación de usuarios.

Después de iniciar sesión, el cliente obtiene un JWT que se envía al backend mediante el encabezado:

`Authorization: Bearer <token>`

El backend funciona como servidor de recursos OAuth2 y valida los tokens utilizando la configuración del emisor de Supabase.

### 6.2. Identidad del usuario

Los controladores utilizan el mecanismo `@AuthUserId` para obtener la identidad del usuario autenticado a partir del token.

La identidad no debe confiarse a un identificador enviado libremente en el cuerpo de una petición.

### 6.3. Roles

Los roles definidos en el dominio son:

- `ADMINISTRADOR`
- `ENCARGADO_TIENDA`
- `ENCARGADO_CD`
- `PLANIFICADOR`

La autorización se aplica según la acción solicitada y el recurso al que se intenta acceder.

Por ejemplo, el servicio de recomendaciones exige el rol `PLANIFICADOR` para crear, consultar, aprobar y rechazar recomendaciones.

Un usuario autenticado puede recibir una respuesta HTTP 403 si no dispone del permiso requerido.

## 7. Persistencia de información

La persistencia utiliza PostgreSQL y Spring Data JPA.

Las entidades JPA representan la estructura persistente, mientras que los adaptadores de salida implementan los puertos definidos por la aplicación.

La configuración de conexión se gestiona mediante propiedades y variables de entorno, evitando incorporar credenciales directamente en el código fuente.

La configuración de Hibernate y las migraciones deben administrarse considerando el entorno en el que se ejecuta la aplicación.

## 8. Funcionalidades HU-13 y HU-20

### 8.1. HU-13: Ajustar inventario

Esta funcionalidad permite registrar un ajuste de existencias para un producto ubicado en una tienda o en un centro de distribución.

El proceso contempla:

1. Validar los datos de la solicitud.
2. Comprobar que se haya indicado una única ubicación.
3. Verificar la existencia del producto y de la ubicación.
4. Validar los permisos del usuario.
5. Consultar el registro de inventario correspondiente.
6. Calcular la diferencia entre la cantidad anterior y la nueva.
7. Actualizar las existencias.
8. Registrar el historial del ajuste.

El endpoint de ajuste es:

`POST /api/inventario/ajustes`

El campo `nuevaCantidad` representa la cantidad final que debe quedar registrada, no una cantidad que se suma automáticamente.

### 8.2. HU-20: Crear recomendación de reposición

Esta funcionalidad permite registrar una propuesta de reposición de mercancía desde un centro de distribución hacia una tienda.

El proceso contempla:

1. Validar la información de la solicitud.
2. Comprobar el permiso del usuario.
3. Verificar que el producto esté activo.
4. Validar la tienda y el centro de distribución.
5. Comprobar la disponibilidad de existencias.
6. Evaluar la cobertura logística.
7. Registrar la propuesta en estado `PROPUESTA`.

La creación de una recomendación no ejecuta por sí misma el traslado ni descuenta existencias.

Las operaciones de creación, consulta, aprobación y rechazo están restringidas al rol `PLANIFICADOR`.

## 9. Registro de eventos (logging)

El backend utiliza el sistema de logging de Spring Boot y Logback.

Se registran eventos relevantes para facilitar el diagnóstico y seguimiento de las operaciones, entre ellos:

- Ajustes de inventario.
- Ingresos de mercancía.
- Registro de ventas.
- Creación y gestión de recomendaciones.
- Eventos relacionados con autenticación.
- Errores no controlados.

La configuración contempla:

- Nivel general de registro configurable.
- Archivo de salida configurable.
- Registro en consola y archivo.
- Rotación por tamaño.
- Conservación histórica de archivos.
- Límite de almacenamiento de registros.

El archivo de configuración principal es:

`src/main/resources/application.properties`

La evidencia de ejecución se encuentra en:

`evidencias/logs/`

## 10. Seguridad

El proyecto incorpora mecanismos de autenticación, autorización y análisis de seguridad.

### 10.1. Análisis de dependencias (SCA)

Se utilizó OWASP Dependency-Check para analizar las dependencias del proyecto.

El informe generado documenta las dependencias examinadas y los resultados obtenidos de las fuentes consultadas.

El resultado del análisis no debe interpretarse como una garantía absoluta de ausencia de vulnerabilidades.

### 10.2. Análisis dinámico (DAST)

Se utilizó OWASP ZAP para realizar un análisis dinámico sobre el backend.

Se conservaron informes iniciales y finales para documentar los resultados y las alertas detectadas.

Los hallazgos deben evaluarse y corregirse según su impacto y prioridad. La existencia de un informe final no implica que todos los hallazgos estén resueltos.

### 10.3. Evidencias

Los informes se encuentran en:

`evidencias/seguridad/`

## 11. API y documentación interactiva

La API se documenta mediante OpenAPI 3 y Springdoc.

Swagger UI permite consultar los endpoints, sus parámetros, esquemas de solicitud y respuestas documentadas.

Cuando el backend se ejecuta localmente, la interfaz se encuentra en:

`http://localhost:8080/swagger-ui/index.html`

La especificación OpenAPI se encuentra en:

`http://localhost:8080/v3/api-docs`

Las operaciones protegidas requieren autorización mediante un JWT válido.

La documentación ampliada se encuentra en:

`docs/Swagger_OpenAPI.md`

## 12. Pruebas automatizadas

El proyecto utiliza JUnit y Mockito para verificar el comportamiento de los servicios.

En la ejecución documentada de las pruebas de HU-13 y HU-20 se obtuvieron los siguientes resultados:

| Clase de prueba | Pruebas | Fallos | Errores |
|---|---:|---:|---:|
| `AjustarInventarioServiceTest` | 4 | 0 | 0 |
| `RecomendacionReposicionServiceTest` | 14 | 0 | 0 |
| **Total** | **18** | **0** | **0** |

Resultado registrado: `BUILD SUCCESS`.

La evidencia de ejecución y los reportes XML se encuentran en:

`evidencias/pruebas/HU-13/`

`evidencias/pruebas/HU-20/`

`evidencias/pruebas/Resultado_Ejecucion_HU13_HU20.txt`

Estos resultados corresponden a pruebas automatizadas de servicios y no sustituyen las pruebas funcionales manuales que requieran evidencias de interfaz.

## 13. Diagramas del sistema

Los diagramas técnicos se organizan en `docs/diagramas/`.

### Diagramas C4

- Contexto del sistema.
- Contenedores.
- Componentes del backend.

### Diagramas de secuencia

- HU-13: Ajustar inventario.
- HU-20: Crear recomendación de reposición.

Los archivos fuente se encuentran en formato PlantUML (`.puml`) y las representaciones generadas están disponibles en PNG y SVG.

El índice de diagramas se encuentra en:

`docs/diagramas/README.md`

## 14. Configuración y ejecución local

Para ejecutar el backend se requiere un entorno con JDK compatible con la configuración del proyecto, Maven Wrapper y las variables de entorno necesarias.

Desde la raíz del repositorio:

1. Configurar las variables de entorno de conexión y autenticación.
2. Ejecutar las pruebas automatizadas con:

   `./mvnw test`

3. Iniciar la aplicación con:

   `./mvnw spring-boot:run`

4. Consultar la API desde Swagger UI:

   `http://localhost:8080/swagger-ui/index.html`

Las credenciales, claves privadas y tokens no deben incluirse en el repositorio ni en las evidencias públicas.

## 15. Estructura de documentación y evidencias

| Ruta | Contenido |
|---|---|
| `docs/Resumen-Tecnico-OptiWay.md` | Resumen técnico del proyecto. |
| `docs/Hexagonal-Arquitecture.md` | Explicación de arquitectura hexagonal. |
| `docs/Swagger_OpenAPI.md` | Documentación de la API. |
| `docs/Matriz-Pruebas-HU-13-HU-20.md` | Matriz de pruebas de las historias. |
| `docs/diagramas/` | Diagramas C4 y de secuencia. |
| `docs/deudaTecnica.md` | Deuda técnica y decisiones pendientes. |
| `docs/CHANGELOG.md` | Historial de cambios. |
| `evidencias/pruebas/` | Reportes de pruebas automatizadas. |
| `evidencias/seguridad/` | Informes de seguridad. |
| `evidencias/logs/` | Evidencias del sistema de logs. |

## 16. Consideraciones y limitaciones

- Los permisos dependen del rol y de la acción solicitada.
- Un código HTTP 403 indica que el usuario no dispone de autorización para la operación solicitada.
- Los resultados de pruebas automatizadas no garantizan por sí solos el correcto funcionamiento de todos los escenarios de producción.
- Los análisis de seguridad reflejan los resultados y el alcance de las herramientas utilizadas en el momento de su ejecución.
- Las operaciones que modifican información deben probarse en un entorno controlado para evitar alterar datos compartidos.
- La disponibilidad de funcionalidades en el frontend debe verificarse por separado de su implementación en el backend.

## 17. Conclusión

OptiWay cuenta con un backend organizado mediante arquitectura hexagonal, una API REST documentada, integración con Supabase para autenticación y persistencia, controles de autorización, registro de eventos y pruebas automatizadas.

La documentación y las evidencias organizadas en el repositorio permiten consultar la estructura del sistema, comprender sus principales procesos y dar seguimiento a las actividades de desarrollo, pruebas y seguridad.
