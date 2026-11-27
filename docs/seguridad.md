# Seguridad y Evidencias (DAST / SCA) — OptiWay Backend

Este documento resume los controles de seguridad implementados en el backend y los resultados obtenidos en los análisis DAST y SCA.

---

## 1. Controles en código

- **Autenticación y sesión (`SecurityConfig.java`):**
  - API **OAuth2 Resource Server** con validación de **JWT** emitido por Supabase Auth.
  - `@AuthUserId` extrae el `sub` criptográficamente del JWT (nunca del cuerpo ni de parámetros de ruta).
  - `SessionCreationPolicy.STATELESS` y `csrf().disable()` (API *stateless* con *Bearer token*).
- **Cabeceras de seguridad (`Content-Security-Policy`):**
  - Configurada en `SecurityConfig.java` sin `'unsafe-inline'` en `script-src` ni `style-src`, con `object-src 'none'`, `base-uri 'self'`, `frame-ancestors 'none'` y `form-action 'self'`.
- **Autorización (RBAC):**
  - `AuthorizationService` valida rol y pertenencia a tienda o centro de distribución (`ADMINISTRADOR`, `PLANIFICADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`) según `AuthorizedAction`.
- **Detección de secretos en CI:**
  - `.github/workflows/ci.yml` ejecuta **Gitleaks** en `produccion`.

---

## 2. Análisis Dinámico (DAST — OWASP ZAP 2.17.0)

Perfil aislado utilizado para las pruebas locales: `src/main/resources/application-zap.properties` (H2 en memoria, sin conexión a Supabase).

### Escaneo inicial (2026-11-22 13:08)
**5 tipos de alerta** (`0` Alto, `3` Medio, `1` Bajo, `1` Informativo):

| Alerta | Riesgo | Ocurrencias |
|---|---|---:|
| CSP: `script-src unsafe-inline` | Medio | 2 |
| CSP: `style-src unsafe-inline` | Medio | 2 |
| Cabecera Content Security Policy (CSP) no configurada | Medio | 2 |
| Cookie sin el atributo `SameSite` | Bajo | 1 |
| Respuesta de Gestión de Sesión Identificada | Informativo | 1 |

### Escaneo final (2026-11-22 15:32)
Tras configurar `SessionCreationPolicy.STATELESS` y la cabecera `Content-Security-Policy` en `SecurityConfig.java`:
- **Resultado:** **0 alertas** (*"No alerts were found within the report parameters"*).

---

## 3. Análisis de Dependencias (SCA — OWASP Dependency-Check 13.0.0)

- **Dependencias escaneadas:** `127` (`73` únicas).
- **Dependencias vulnerables:** `0`.
- **Vulnerabilidades encontradas:** `0`.
- Configurado en `pom.xml` (`org.owasp:dependency-check-maven:13.0.0`) y ejecutable en CI activando `vars.SCA_ENABLED == 'true'` con `NVD_API_KEY`.
