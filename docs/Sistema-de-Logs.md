# Sistema de Logs en Spring Boot (OptiWay Backend)

Este documento detalla cómo funciona el sistema de logs, sus componentes, niveles de severidad y las distintas formas de implementarlo en la arquitectura hexagonal de **OptiWay**.

---

## 1. ¿Cómo Funciona el Sistema de Logs?

Spring Boot incluye de fábrica una arquitectura de logging robusta basada en dos piezas clave:

```
+-------------------------------------------------------------+
|               Código de la Aplicación                       |
|   (Controladores, Servicios, Filtros de Seguridad, JPA)    |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                 SLF4J (Simple Logging Facade)               |
|      Abstracción estándar: Logger, LoggerFactory, Marker    |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                   Logback (Motor de Ejecución)              |
|        Formateo, Appenders (Consola / Archivos), Rotación   |
+-------------------------------------------------------------+
```

1. **SLF4J (`org.slf4j.*`)**: Es la interfaz o fachada universal en Java. Permite que el código no dependa de una librería concreta.
2. **Logback**: Es el motor interno predeterminado de Spring Boot. Se encarga de procesar los mensajes, darles formato (colores, fecha, hilo, clase) y enviarlos a la consola o a un archivo en disco.

> **¿Por qué NUNCA usar `System.out.println` en un backend?**
> - `System.out.println` es sincrónico y bloqueante: congela el hilo de ejecución hasta escribir en la salida estándar.
> - No tiene niveles de severidad (no se puede silenciar en producción).
> - No registra fecha, hora, hilo, ni clase de origen.
> - No permite enviar los datos a archivos con rotación automática.

---

## 2. Niveles de Log y su Jerarquía

Los niveles determinan qué tan detallada es la información que se imprime. Operan en una jerarquía estricta:

$$\text{TRACE} < \text{DEBUG} < \text{INFO} < \text{WARN} < \text{ERROR} < \text{OFF}$$

| Nivel | Cuándo usarlo | Ejemplo |
| :--- | :--- | :--- |
| **`TRACE`** | Diagnóstico ultra detallado a nivel de bytes o parámetros vinculados. | Parámetros reales `?` en queries SQL de Hibernate. |
| **`DEBUG`** | Información útil para desarrolladores durante el flujo de una petición. | Headers HTTP recibidos, decisión del filtro de seguridad JWT. |
| **`INFO`** | Eventos normales e importantes del ciclo de vida del sistema. | Servidor iniciado en puerto 8080, usuario completó una orden. |
| **`WARN`** | Situaciones inesperadas que no detienen el sistema, pero requieren atención. | Intento de login fallido, stock por debajo del mínimo de seguridad. |
| **`ERROR`** | Fallas que impiden completar una operación (excepciones no recuperadas). | Base de datos inaccesible, NullPointerException no controlado. |

*Regla:* Si configuras el nivel en `INFO`, el sistema mostrará `INFO`, `WARN` y `ERROR`. Ocultará automáticamente `DEBUG` y `TRACE`.

---

## 3. Anatomía de una Línea de Log

El formato estándar que Logback imprime en consola es:

```text
2026-09-22T00:15:30.124-05:00  INFO 14220 --- [optiway] [nio-8080-exec-1] c.e.o.a.s.InventarioService : Consultando inventario - Usuario: 550e8400... Tienda: 1
       (1)                      (2)   (3)          (4)           (5)                     (6)                                (7)
```

1. **Fecha y Hora**: Formato ISO-8601 con milisegundos y zona horaria.
2. **Nivel**: INFO, DEBUG, WARN, ERROR, etc.
3. **PID**: ID del proceso del sistema operativo.
4. **Aplicación**: Nombre configurado en `spring.application.name`.
5. **Hilo (Thread)**: Hilo del pool de Tomcat que procesó la solicitud (ej. `[nio-8080-exec-1]`).
6. **Logger / Origen**: Paquete y clase abreviada donde se originó el log.
7. **Mensaje**: Contenido del evento.

---

## 4. Estrategias de Implementación en OptiWay

Existen **tres formas complementarias** de usar logs según la necesidad:

### Estrategia A: Logging Declarativo Global (Sin tocar código Java)

Para ver qué endpoints se consumen, qué queries se ejecutan y qué decide la seguridad, basta con configurar [application.properties](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties):

```properties
# 1. Monitoreo de endpoints HTTP entrantes (URL, método, controlador asignado)
logging.level.org.springframework.web=DEBUG

# 2. Monitoreo de autenticación JWT y autorización por roles (401 Unauthorized / 403 Forbidden)
logging.level.org.springframework.security=DEBUG

# 3. Sentencias SQL ejecutadas en PostgreSQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 4. Ver los valores reales de los parámetros (?) en las queries SQL
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

*Ventaja:* Aplica inmediatamente a toda la aplicación sin modificar controladores ni servicios.

---

### Estrategia B: Filtro Centralizado de Peticiones HTTP (`CommonsRequestLoggingFilter`)

Si se desea imprimir en consola el detalle completo de cada petición (URL, parámetros de consulta, dirección IP y payload del body) de forma uniforme:

1. Crear una clase de configuración en la capa de infraestructura (ej. `infrastructure/config/WebLoggingConfig.java`):

```java
package com.example.optiway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class WebLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);       // IP del cliente
        filter.setIncludeQueryString(true);      // Parámetros (?tiendaId=1)
        filter.setIncludePayload(true);          // Cuerpo JSON de la petición
        filter.setMaxPayloadLength(10000);       // Tamaño máximo a imprimir
        filter.setIncludeHeaders(false);         // No imprimir headers por privacidad de tokens
        return filter;
    }
}
```

2. Y activar su visualización en [application.properties](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties):
```properties
logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG
```

*Resultado en consola:*
```text
DEBUG [nio-8080-exec-2] o.s.w.f.CommonsRequestLoggingFilter : Before request [POST /api/ingresos-mercancia, client=127.0.0.1, payload={"centroDistribucionId":1,"productoId":3,"cantidad":50}]
```

---

### Estrategia C: Logging Manual para Reglas y Auditoría de Negocio

Se utiliza cuando se necesita registrar un evento específico de dominio que Spring no conoce (por ejemplo: transferencias de inventario, motivos de rechazo de negocio o cálculo de stock).

#### Sintaxis Estándar (SLF4J Nativo)
En proyectos sin Project Lombok (como OptiWay actual):

```java
package com.example.optiway.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventarioService implements ConsultarInventarioUseCase {

    // Instancia estática del logger vinculada a la clase actual
    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);

    @Override
    public List<Inventario> consultarInventario(UUID authUserId, Long tiendaId) {
        log.info("Iniciando consulta de inventario para tienda {} solicitada por usuario {}", tiendaId, authUserId);
        
        try {
            List<Inventario> inventario = inventarioPort.obtenerPorTienda(tiendaId);
            log.debug("Se recuperaron {} registros de inventario para tienda {}", inventario.size(), tiendaId);
            return inventario;
        } catch (Exception e) {
            log.error("Error al consultar inventario de la tienda {}: {}", tiendaId, e.getMessage(), e);
            throw e;
        }
    }
}
```

> **Regla de Oro en SLF4J:**  
> Usar siempre la interpolación con llaves `{}` en lugar del operador `+`.  
> - **Correcto:** `log.info("Usuario: {}, Tienda: {}", usuarioId, tiendaId);` (No consume memoria si el log está deshabilitado).
> - **Incorrecto:** `log.info("Usuario: " + usuarioId + ", Tienda: " + tiendaId);` (Concatena cadenas en memoria siempre, degradando el rendimiento).

---

## 5. Persistencia y Rotación de Archivos de Log

Para guardar los logs en un archivo físico en el servidor (útil para auditorías y diagnóstico de caídas):

En [application.properties](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/application.properties):

```properties
# Nombre y ruta del archivo de log
logging.file.name=logs/optiway-backend.log

# Tamaño máximo antes de crear un nuevo archivo
logging.logback.rollingpolicy.max-file-size=10MB

# Cantidad de días que se conservan los logs antiguos antes de eliminarse
logging.logback.rollingpolicy.max-history=30

# Límite de peso total de todos los logs archivados
logging.logback.rollingpolicy.total-size-cap=1GB
```

Spring Boot creará una carpeta `logs/` y rotará los archivos automáticamente comprimiéndolos en formato `.gz` (ej. `optiway-backend-2026-09-21.1.log.gz`).

---

## 6. Buenas Prácticas de Seguridad y Rendimiento

1. **Nunca loguear información confidencial:**
   - No loguear tokens JWT completos (evitar robo de sesión en caso de fuga de archivos de log).
   - No loguear contraseñas, PINs o números de tarjetas de crédito.
2. **Manejo de excepciones:**
   - Pasar la excepción como último argumento en el método de log para imprimir la traza de error completa:
     ```java
     log.error("Fallo inesperado procesando ingreso al CD {}: {}", cdId, ex.getMessage(), ex);
     ```
3. **Control por entornos:**
   - En **Desarrollo (local)**: Niveles `DEBUG` para web, seguridad y SQL.
   - En **Producción**: Nivel general en `INFO` o `WARN`, activando `ERROR` para capturar fallos sin saturar el almacenamiento en disco ni degradar la velocidad de respuesta de la API.
