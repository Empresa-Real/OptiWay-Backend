# `tests/api/endpoints_supabase.py`

Script de pruebas de autenticación e invitación de usuarios contra Supabase Auth y `POST /api/usuarios`.

## Qué prueba
- **Capa Supabase Auth (`/auth/v1/token`):** login válido (`200`), contraseña incorrecta (`400`), email inexistente (`400`), ausencia de header `apikey` (`401`) y formato de email inválido (`400`).
- **Capa Backend (`POST /api/usuarios`):** sin token (`401`), rol no administrador (`403`), email duplicado (`409`), validaciones de entrada / JSON inválido (`400`), creación exitosa (`201`) y limpieza posterior (`204`).

## Variables de entorno requeridas
- `BASE_URL` (por defecto `http://localhost:8080`)
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`
- `ADMIN_EMAIL` / `ADMIN_PASSWORD`, `TIENDA_EMAIL` / `TIENDA_PASSWORD`

## Ejecución
```powershell
python tests/api/endpoints_supabase.py
```
