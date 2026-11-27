# `tests/api/test_suite.py`

Suite principal HTTP / E2E en Python (`requests`) para validar el contrato real de los controladores Spring MVC de OptiWay Backend (Sprint 1, 2 y 3).

## Cobertura
1. **Autorización (`401 Unauthorized`):** verifica todos los endpoints protegidos de Sprint 1, 2 y 3 (usuarios, tiendas, CD, productos, inventario, demanda, recomendaciones, reportes de rotación, solicitudes de reposición y `/admin/logs`, `/admin/metrics`) sin cabecera `Authorization`.
2. **Type Mismatch (`400 Bad Request`):** envía strings y UUIDs en `@PathVariable Long id`.
3. **Recursos inexistentes (`404 Not Found`):** consultas y mutaciones con ID `999999`.
4. **Validación de cuerpos y parámetros (`400 Bad Request`):** payloads vacíos, cantidades/precios negativos, `unidadMedida` inválida, `nuevaCantidad` negativa.
5. **Rutas y métodos no soportados (`404` / `405`).**
6. **Flujos funcionales y rutas públicas:** `/api/estadisticas`, `/graph`, creación de producto (normalizando `unidadMedida` `"kilogramos"` → `"KG"`), creación/listado de CD y tienda, ingreso de mercancía, ajuste de inventario en CD, consulta de órdenes de CD, órdenes en tránsito, demanda, recomendaciones y reporte de rotación (JSON/CSV).

## Ejecución
```powershell
$env:BASE_URL = "http://localhost:8080"
$env:AUTH_TOKEN = "tu_jwt_aqui"
python tests/api/test_suite.py
```
Variables opcionales si no defines `AUTH_TOKEN`: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
