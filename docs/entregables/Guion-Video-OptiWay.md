# Guion del video de presentación - OptiWay

**Duración estimada:** 6 minutos
**Proyecto:** OptiWay - Sistema de gestión logística e inventarios

## 1. Presentación del proyecto (0:00 - 0:40)

**Mostrar:** Nombre del proyecto y pantalla inicial o presentación.

**Guion:**

"Hola. En este video presentaré OptiWay, un sistema orientado a la gestión logística y el control de inventarios.

El objetivo del proyecto es facilitar la administración de productos, tiendas y centros de distribución, además de apoyar las decisiones de reposición de mercancía mediante información del inventario."

## 2. Arquitectura y tecnologías (0:40 - 1:30)

**Mostrar:** Diagramas C4 de contexto, contenedores y componentes.

**Guion:**

"El backend está desarrollado en Java con Spring Boot y expone una API REST.

La solución utiliza una arquitectura hexagonal, separando los adaptadores de entrada, los servicios de aplicación, el dominio y los adaptadores de salida.

La persistencia utiliza PostgreSQL alojado en Supabase. La autenticación se integra con Supabase Auth mediante tokens JWT.

Esta separación permite organizar las responsabilidades y facilita el mantenimiento y las pruebas del sistema."

## 3. Gestión de inventario - HU-13 (1:30 - 2:40)

**Mostrar:** Diagrama de secuencia HU-13 y, si está disponible, la funcionalidad en la aplicación o Swagger.

**Guion:**

"Una funcionalidad implementada es el ajuste de inventario.

El usuario autorizado puede registrar una nueva cantidad para un producto en una tienda o en un centro de distribución.

El sistema valida la ubicación, verifica los permisos, consulta el inventario actual y registra tanto la cantidad anterior como la nueva y la diferencia.

También conserva un historial del ajuste, permitiendo conocer quién realizó la operación y cuándo se efectuó."

## 4. Recomendaciones de reposición - HU-20 (2:40 - 3:50)

**Mostrar:** Diagrama de secuencia HU-20 y la operación disponible en Swagger o en la aplicación.

**Guion:**

"OptiWay también permite crear recomendaciones de reposición.

El planificador indica el producto, la cantidad, el centro de distribución de origen y la tienda de destino.

Antes de guardar la propuesta, el sistema valida que exista suficiente inventario y verifica si el centro de distribución cubre la tienda de destino.

Si no existe suficiente stock, la operación se rechaza. Si el centro está fuera de cobertura, el sistema solicita confirmación.

La recomendación queda inicialmente en estado Propuesta. Es importante aclarar que crear la propuesta no ejecuta el traslado ni descuenta existencias."

## 5. Seguridad y calidad (3:50 - 5:00)

**Mostrar:** Configuración de seguridad, reportes OWASP y resultados de pruebas.

**Guion:**

"El sistema incorpora controles de autenticación y autorización según los roles de usuario.

También se trabajó en el endurecimiento de las cabeceras HTTP y en la evaluación de seguridad mediante OWASP ZAP.

Para el análisis de dependencias se utilizó OWASP Dependency-Check. El reporte generado registró 127 dependencias analizadas, cero dependencias vulnerables y cero vulnerabilidades detectadas.

Adicionalmente, se ejecutaron pruebas automatizadas para validar los servicios y las reglas de negocio."

## 6. Resultados de pruebas (5:00 - 5:40)

**Mostrar:** Salida de Maven y matriz de pruebas.

**Guion:**

"Para las historias HU-13 y HU-20 se ejecutaron 18 pruebas automatizadas: 4 correspondientes al ajuste de inventario y 14 correspondientes a las recomendaciones de reposición.

Todas finalizaron correctamente, sin fallos ni errores.

También se ejecutó la suite general del proyecto, que finalizó con BUILD SUCCESS."

## 7. Cierre (5:40 - 6:00)

**Mostrar:** Vista general del proyecto o repositorio.

**Guion:**

"En conclusión, OptiWay integra gestión de inventarios, control de acceso por roles, registro de operaciones y recomendaciones de reposición.

El proyecto cuenta con documentación técnica, diagramas de arquitectura, evidencias de seguridad y pruebas automatizadas.

Muchas gracias."

## Recomendaciones para la grabación

- Grabar la pantalla con texto legible y sin ventanas innecesarias.
- Mostrar los diagramas con suficiente zoom.
- No mostrar contraseñas, claves API, tokens ni variables secretas.
- Al mostrar reportes, utilizar los archivos de evidencias del repositorio.
- Si una funcionalidad no está disponible en la interfaz, mostrar su endpoint en Swagger y explicar el flujo del backend.
- No afirmar que una funcionalidad está integrada visualmente si no se ha comprobado.
