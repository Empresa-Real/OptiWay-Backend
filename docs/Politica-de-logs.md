# Política de logs y observabilidad de OptiWay

Aplica al equipo básico (lineamientos 4.1, 5.3, 6.2 y DoD 9.1). El foco de esta entrega es que el despliegue tenga **logs y métricas disponibles**, no solo configuración local.

## 1. Niveles por entorno

| Entorno | Nivel | Cómo se activa |
|---|---|---|
| Local | `DEBUG` (incluye SQL y seguridad) | `spring.profiles.active=local` → `application-local.properties` |
| Producción (Render) | `INFO` / `WARN` | Variables `LOG_LEVEL` y `APP_LOG_LEVEL` (por defecto `INFO`) |

En producción **no** se activa el perfil `local`, por lo que `show-sql=false` y no se exponen trazas de seguridad.

## 2. Formato

Patron único, definido en `application.properties`:

```
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n
```

Render interpreta los niveles de este formato en su explorador de logs. El formato **JSON no es obligatorio** en el nivel básico (es un requisito del equipo avanzado, sección 3.3), así que se conserva el formato de texto consistente.

## 3. Dónde se escriben los logs

- **Consola (stdout/stderr):** es lo que Render captura y muestra en el dashboard. Es la fuente oficial en producción.
- **Archivo local:** `logs/optiway.log`, con rotación de 10 MB, 14 días de historial y tope de 100 MB. **El filesystem de Render es efímero por defecto**: lo escrito en disco se pierde en cada deploy o reinicio (y cada instancia tiene su propio archivo). Además, el visor de logs de Render captura **stdout/stderr**, no este archivo. Por eso el archivo es solo para desarrollo / diagnóstico local; no requiere Persistent Disk (de pago y sin zero-downtime).

## 4. Qué se registra y qué no

**Sí se registra (auditoría de acciones críticas, con identificadores):**

| Servicio | Evento |
|---|---|
| `VentaService` | Venta registrada (tienda, producto, cantidad) |
| `AjustarInventarioService` | Ajuste de inventario (ubicación, producto, cantidad anterior y nueva) |
| `IngresoMercanciaService` | Ingreso de mercancía (CD, producto, cantidad) |
| `RecomendacionReposicionService` | Recomendación creada / aprobada / rechazada |
| `UsuarioService` | Usuario creado / eliminado y cambio de rol |
| `ApiExceptionHandler` | Excepciones no controladas (`log.error` con stack trace) |

**Nunca se registra:** contraseñas, tokens JWT, `SUPABASE_SERVICE_ROLE_KEY`, `DB_PASSWORD`, correos electrónicos completos ni datos personales innecesarios. Los mensajes de auditoría usan IDs y cantidades.

## 5. Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `LOG_LEVEL` | `INFO` | Nivel global (`logging.level.root`) |
| `APP_LOG_LEVEL` | `INFO` | Nivel del paquete `com.example.optiway` |
| `LOG_FILE` | `logs/optiway.log` | Ruta del archivo local |
| `SWAGGER_ENABLED` | `true` | Activar/desactivar OpenAPI y Swagger UI |

## 6. Verificación en Render (plan gratuito)

1. **Logs:** en el servicio → pestaña *Logs*. Buscar, por ejemplo, `Venta registrada` o `Usuario eliminado` después de ejecutar la acción. Soporta filtro por `level` e `instance` y *live tail*. Retención en Hobby: **7 días**.
2. **Métricas:** pestaña *Metrics* → CPU, memoria, volumen de peticiones HTTP y ancho de banda. Retención en Hobby: **7 días**.
3. **Health:** `GET https://<servicio>.onrender.com/actuator/health` → `UP`. Actuator expone `health,info`.
4. **Demo:** el plan gratuito duerme el servicio por inactividad; despertarlo antes de la muestra.

Lo exclusivo del plan Pro (HTTP request logs, latencia p50/p99, streaming a proveedores externos) **no es requisito del equipo básico**.

## 7. Panel interno `/admin`

`GET /admin` sirve un panel HTML simple con:

- **Salud:** estado de `/actuator/health`.
- **Métricas:** `GET /admin/metrics` (uptime, memoria usada/total e hilos de JVM).
- **Logs:** `GET /admin/logs?lines=100` (últimas líneas de `logs/optiway.log`, tope 500). Si el appender de archivo no arranca (Render: filesystem efímero, `Attempted to append to non started appender [FILE]`), el endpoint cae a un **buffer circular en memoria** de las últimas 500 líneas, alimentado por un appender de Logback adjunto al logger raíz. La respuesta incluye `"fuente": "archivo" | "memoria"`.
- **Estadísticas:** `GET /api/estadisticas` devuelve número de tiendas, CDs y unidades de stock. Es **público** (sin token ni cookie) porque el frontend lo muestra en el login, antes de autenticarse; por eso no expone datos personales ni trazas.

El panel `/admin` es **público por decisión del equipo**: ni la página ni los datos (`/admin/logs`, `/admin/metrics`, `/api/estadisticas`) piden token ni cookie. No hay campo de JWT en la interfaz. Esto se documenta para la demo; en un entorno real conviene proteger los logs y las métricas con rol ADMINISTRADOR.

En Render el archivo puede no existir después de un deploy (filesystem efímero): el panel muestra “Sin líneas disponibles” y el visor oficial sigue siendo la pestaña *Logs*.

## 8. Ejecución local

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot'
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## 9. Evidencia

- Captura de la pestaña *Logs* con una acción auditada.
- Captura de la pestaña *Metrics*.
- Respuesta de `/actuator/health`.
- Informe de cobertura `target/site/jacoco/index.html` (ver pipeline de CI).
- Captura del panel `/admin` (logs, métricas, health y estadísticas), sin iniciar sesión.
