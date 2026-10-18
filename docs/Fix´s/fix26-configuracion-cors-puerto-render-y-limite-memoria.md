# Fix 26: Configuración de CORS, Puerto Dinámico y Límite de Memoria JVM para Render

## 1. Contexto y Diagnóstico del Problema

Para garantizar un despliegue sin caídas ni bloqueos de red en el tier gratuito de **Render** (512 MB de RAM) y permitir la comunicación con clientes frontend en diferentes dominios, se identificaron los siguientes puntos críticos:

1. **Ausencia de Puerto Dinámico (`server.port`):**
   - En Render, el contenedor web asigna dinámicamente un puerto a través de la variable de entorno `$PORT` (comúnmente `10000`).
   - Sin `server.port=${PORT:8080}`, Spring Boot arrancaba en el puerto predeterminado `8080`, provocando fallos por *port binding timeout* en el health check de Render.

2. **Bloqueo por CORS (Cross-Origin Resource Sharing):**
   - `SecurityConfig.java` carecía de `.cors(...)`, por lo que navegadores web bloqueaban las solicitudes HTTP de clientes alojados en otros dominios o puertos con errores de *preflight* `OPTIONS`.

3. **Riesgo de OOM (Out Of Memory) en Render Free Tier (512 MB RAM):**
   - En contenedores Linux con memoria restringida, la JVM por defecto utiliza el recolector G1 y asigna límites de Heap y Metaspace que pueden exceder los 512 MB totales del contenedor, activando el *OOM Killer* del sistema operativo.
   - Además, `render.yaml` se encontraba en `.gitignore`, impidiendo que Render leyera la infraestructura como código automáticamente.

---

## 2. Solución Aplicada

### A. Puerto Dinámico y Configuración CORS en `application.properties`
En [application.properties](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties):
```properties
# Puerto dinámico para Render con fallback en 8080 local
server.port=${PORT:8080}

# Configuración de orígenes CORS permitidos (por defecto comodín en local, configurable en producción)
cors.allowed-origins=${ALLOWED_ORIGINS:*}
```

### B. Habilitación de CORS en `SecurityConfig.java`
En [SecurityConfig.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/config/SecurityConfig.java):
- Se activó `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`.
- Se implementó el bean `CorsConfigurationSource` configurando:
  - Soporte de origen flexible mediante `addAllowedOriginPattern("*")` o lista separada por comas (`cors.allowed-origins`).
  - Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
  - Encabezados permitidos y expuestos: `Authorization`, `Content-Disposition`, etc.
  - Soporte de credenciales (`allowCredentials = true`) y tiempo de caché preflight (`maxAge = 3600L`).

### C. Optimización de Memoria JVM para Render (512 MB RAM)
En [render.yaml](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/render.yaml):
- Se fijaron los parámetros de arranque y variables de entorno para maximizar la estabilidad en 512 MB:
  ```bash
  -XX:+UseSerialGC -Xmx256m -Xss512k -XX:MaxMetaspaceSize=128m
  ```
  - `-XX:+UseSerialGC`: Recolector de basura liviano de un solo hilo, reduciendo el overhead de memoria en comparación con G1GC.
  - `-Xmx256m`: Límite máximo de memoria Heap de 256 MB.
  - `-Xss512k`: Reduce el tamaño de stack por hilo de 1 MB a 512 KB.
  - `-XX:MaxMetaspaceSize=128m`: Evita el crecimiento descontrolado del Metaspace.
- Se eliminó `render.yaml` de `.gitignore` para versionarlo en el repositorio.

---

## 3. Verificación

- **Pruebas Automatizadas:** `37 de 37` pruebas pasando en verde (`BUILD SUCCESS`).
- **Compatibilidad Local:** Al ejecutar localmente sin `$PORT`, el servidor sigue levantando en `http://localhost:8080`.
- **Knowledge Graph:** Actualizado mediante `graphify update .`.
