# `tests/api/endpoints_errores.py`

Script enfocado en validar el contrato de respuestas de error de la API.

## Qué prueba
- Peticiones sin autenticación → `401 Unauthorized` (resuelto por `@AuthUserId` antes de validar el body).
- `POST` sin cuerpo o con datos inválidos (con token) → `400 Bad Request`.
- IDs numéricos inexistentes (`999999`) → `404 Not Found`.
- Rutas inexistentes → `404 Not Found`.

## Variables de entorno requeridas
- `BASE_URL` (por defecto `http://localhost:8080`)
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`

## Ejecución
```powershell
python tests/api/endpoints_errores.py
```
