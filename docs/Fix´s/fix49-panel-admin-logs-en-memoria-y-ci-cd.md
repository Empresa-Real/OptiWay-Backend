# Fix 49 — Panel `/admin` con fallback en memoria y ajustes del pipeline CI/CD

**Fecha:** 24 de noviembre de 2026  
**Área:** Observabilidad, Infraestructura Web, Maven (JaCoCo) y GitHub Actions  
**Referencia:** Commits `1ed364e`, `9177610`, `4b037a5`, `b9fa9dd`, `fe42ccf`, `6750a59`, `76e2948`, `cf47ec6`, `7aa3f1d`

---

## 1. Contexto y Problema

1. **Entorno efímero en Render:** al desplegar en Render, el archivo local `logs/optiway.log` puede no existir tras un reinicio/deploy, haciendo fallar la lectura de logs en el panel `/admin` y sus pruebas.
2. **Pipeline CI/CD y cobertura:** el proyecto no contaba con pipeline automatizado de validación ni verificación de cobertura mínima, y los pasos opcionales de SCA/Sonar podían generar anotaciones de error o bloquear despliegues cuando no estaban habilitados.

---

## 2. Solución Implementada

- **Panel `/admin` y fallback de logs:**
  - Se configuró el acceso público del panel `/admin` y sus recursos estáticos.
  - Se implementó fallback a un buffer en memoria cuando el archivo de logs en disco no está disponible, ajustando los tests para aceptar dicho comportamiento.
- **Cobertura JaCoCo en `pom.xml`:**
  - Se configuró `jacoco-maven-plugin` (`0.8.13`) en fase `verify` exigiendo cobertura mínima de líneas del **40%** (`LINE COVEREDRATIO >= 0.40`) y generando `target/site/jacoco/jacoco.xml`.
- **Workflow `.github/workflows/ci.yml`:**
  - Ejecuta `bash ./mvnw -B verify` con JDK 17 (Temurin) en `push` a `produccion`.
  - Sube el artefacto `jacoco-report` (`if: always()`).
  - Ejecuta `gitleaks/gitleaks-action@v2` (`continue-on-error: true`).
  - Mantiene SCA (OWASP Dependency-Check) y SonarQube Cloud (OIDC secretless) como pasos condicionales (`vars.SCA_ENABLED`, `vars.SONAR_ENABLED`) sin bloquear el flujo cuando están desactivados.

---

## 3. Archivos Principales

| Archivo | Cambio |
|---|---|
| `.github/workflows/ci.yml` | Pipeline CI (`verify`, JaCoCo, Gitleaks, SCA/Sonar opcionales) |
| `pom.xml` | Agrega `jacoco-maven-plugin` `0.8.13` con regla mínima `0.40` |
| `infrastructure/adapter/in/rest/AdminController.java` | Fallback a buffer en memoria para `/admin/logs` |
| `src/test/java/.../AdminControllerTest.java` | Pruebas del panel `/admin` y fallback en memoria |

---

## 4. Verificación

- Pruebas unitarias e integración pasando con umbral JaCoCo >= 40% en `verify` (`BUILD SUCCESS`).
