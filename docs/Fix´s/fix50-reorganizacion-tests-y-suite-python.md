# Fix 50 — Reorganización de pruebas Java, suite HTTP en Python y documentación técnica

**Fecha:** 24–26 de noviembre de 2026  
**Área:** Pruebas (JUnit/MockMvc/Python), Seguridad de repositorio y Documentación  
**Referencia:** Reorganización de `src/test/java`, `tests/api/`, `.env.example` y `docs/`

---

## 1. Contexto y Problema

1. **Pruebas Java dispersas:** las 31 clases de prueba estaban ubicadas de forma plana en `src/test/java/com/example/optiway/`, mezclando pruebas unitarias, pruebas `@SpringBootTest` y pruebas de dominio.
2. **Scripts Python fuera de control de versiones:** los scripts E2E/contrato HTTP vivían en `other/` (ignorado por `.gitignore`), contenían contraseñas y claves de respaldo hardcodeadas y no cubrían los endpoints nuevos del Sprint 3.
3. **Documentación fragmentada o redundante:** `Documentacion-del-Sistema-OptiWay.md` tenía secciones desactualizadas (conteo viejo de 18 tests, referencia inexistente a `evidencias/logs/` y duplicidad con `README.md` y la wiki), y faltaban documentos dedicados para seguridad, logs, CI/CD y pruebas.

---

## 2. Solución Implementada

- **Reorganización de `src/test/java/com/example/optiway/`:**
  - `integration/` (7 clases, 21 casos `@SpringBootTest` / `MockMvc`).
  - `unit/application/` (13 clases de servicios), `unit/domain/` (2 clases), `unit/infrastructure/` (8 clases) e `infrastructure/adapter/out/auth/SupabaseAuthAdapterTest.java` (mantenida en su paquete por acceso *package-private*).
- **Suite HTTP / E2E en Python (`tests/api/`):**
  - Migrados `test_suite.py`, `endpoints.py`, `endpoints_errores.py`, `endpoints_permisos.py` y `endpoints_supabase.py` eliminando credenciales hardcodeadas y ampliando `test_suite.py` a todos los endpoints de Sprint 1, 2 y 3 (incluyendo `/api/cd/{id}/ordenes`, `/admin/*`, `/api/estadisticas`, `/graph` y validación de `unidadMedida`).
- **Documentación y entorno:**
  - Creada `docs/tests/` con un `.md` individual por cada suite de integración Spring (7) y por cada script Python (5), más `docs/tests/README.md`.
  - Creados `docs/seguridad.md` (resumen DAST ZAP y SCA), `docs/logs.md`, `docs/CI-CD-Gitflow.md`, `docs/guias/` (`How-Work-DB.md`, `frontend.md`) y `.env.example`.
  - Actualizados `Documentacion-del-Sistema-OptiWay.md` y `Hexagonal-Arquitecture.md` (matriz de permisos de Sprint 3), y eliminada la carpeta `evidencias/` redundante.

---

## 3. Verificación

- `./mvnw.cmd test` tras la reorganización de paquetes: **258** ejecuciones, **0** fallos, **0** errores, **0** omitidas (`BUILD SUCCESS`).
- `python -m py_compile` en los 5 scripts de `tests/api/`: **0** errores de sintaxis.
