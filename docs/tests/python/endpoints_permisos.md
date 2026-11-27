# `tests/api/endpoints_permisos.py`

Script de verificación de la matriz de autorización por rol (`403 Forbidden` vs `200`/`201`).

## Qué prueba
- Valida el modelo de permisos de `AuthorizationService`:
  - `ADMINISTRADOR`: acceso completo.
  - `PLANIFICADOR`: gestión logística sin acceso a administración de usuarios.
  - `ENCARGADO_TIENDA`: solo operaciones sobre su tienda asignada (`403` en tiendas ajenas o CD).
  - `ENCARGADO_CD`: solo operaciones sobre su centro asignado (`403` en otros centros o tiendas).

## Variables de entorno requeridas
- `BASE_URL` (por defecto `http://localhost:8080`)
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`
- `ADMIN_EMAIL` / `ADMIN_PASSWORD`, `PLANIFICADOR_EMAIL` / `PLANIFICADOR_PASSWORD`, `TIENDA_EMAIL` / `TIENDA_PASSWORD`, `CD_EMAIL` / `CD_PASSWORD`

## Ejecución
```powershell
python tests/api/endpoints_permisos.py
```
