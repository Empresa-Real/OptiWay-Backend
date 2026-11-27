# `tests/api/endpoints.py`

Script de recorrido funcional por rol contra los endpoints principales de OptiWay Backend.

## Qué prueba
- Autentica contra Supabase Auth usando variables de entorno por rol (`ADMINISTRADOR`, `ENCARGADO_TIENDA`, `ENCARGADO_CD`).
- Ejecuta peticiones sobre rutas de usuarios, tiendas, centros de distribución, productos, inventario, ventas e ingresos imprimiendo el código HTTP y la respuesta.

## Variables de entorno requeridas
- `BASE_URL` (por defecto `http://localhost:8080`)
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`
- `ADMIN_EMAIL` / `ADMIN_PASSWORD`, `TIENDA_EMAIL` / `TIENDA_PASSWORD`, `CD_EMAIL` / `CD_PASSWORD`

## Ejecución
```powershell
python tests/api/endpoints.py
```
