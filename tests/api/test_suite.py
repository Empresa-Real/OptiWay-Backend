"""
OptiWay Backend — Suite de Pruebas E2E / Contrato HTTP (Spring MVC).

Cubre endpoints de Sprint 1, 2 y 3 contra un servidor Spring MVC en ejecución
(local http://localhost:8080 o Render):
  1. Autorización (401 Unauthorized sin token).
  2. Type Mismatch en PathVariables Long (400 Bad Request).
  3. Recursos inexistentes con ID Long 999999 (404 Not Found).
  4. Validación de cuerpos y parámetros obligatorios (400 Bad Request).
  5. Rutas y métodos HTTP no soportados (404 / 405).
  6. Flujos funcionales CRUD y operaciones Sprint 3 (HU-08, HU-10, HU-13,
     HU-15, HU-17, HU-19, HU-20, HU-21, HU-27 y órdenes de CD).

Uso:
  $env:BASE_URL = "http://localhost:8080"
  $env:AUTH_TOKEN = "tu_jwt_aqui"
  python tests/api/test_suite.py
"""

import json
import os
import sys
import time
from datetime import date, datetime, timedelta
import requests

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# ==============================================================================
# CONFIGURACIÓN
# ==============================================================================
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080").rstrip("/")
AUTH_TOKEN = os.environ.get("AUTH_TOKEN") or os.environ.get("BEARER_TOKEN")

SUPABASE_URL = os.environ.get("SUPABASE_URL", "").rstrip("/")
SUPABASE_ANON_KEY = os.environ.get("SUPABASE_ANON_KEY", "")
ADMIN_EMAIL = os.environ.get("ADMIN_EMAIL", "")
ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "")

TIMEOUT = int(os.environ.get("TIMEOUT", "30"))

ID_NUMERICO_INEXISTENTE = 999999
ID_UUID_INVALIDO = "00000000-0000-0000-0000-000000000000"


class Colors:
    GREEN = "\033[92m"
    RED = "\033[91m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    CYAN = "\033[96m"
    MAGENTA = "\033[95m"
    BOLD = "\033[1m"
    END = "\033[0m"


RESUMEN = {
    "total": 0,
    "ok": 0,
    "fail": 0,
    "omitido": 0,
    "detalles_fallos": []
}

VARS = {
    "tienda_id": None,
    "cd_id": None,
    "producto_id": None,
    "usuario_id": None,
}


def print_header(texto):
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 75}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  {texto}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 75}{Colors.END}\n")


def print_result(nombre, metodo, url, datos_envio, respuesta):
    print(f"{Colors.BLUE}[{nombre}]{Colors.END}")
    print(f"  {Colors.CYAN}Endpoint:{Colors.END} {metodo} {url}")
    if datos_envio is not None:
        cuerpo_str = json.dumps(datos_envio, ensure_ascii=False)
        if len(cuerpo_str) > 120:
            cuerpo_str = cuerpo_str[:117] + "..."
        print(f"  {Colors.CYAN}Payload :{Colors.END} {cuerpo_str}")
    print(f"  {Colors.CYAN}Status  :{Colors.END} {respuesta.status_code}")
    try:
        cuerpo = respuesta.json()
        cuerpo_str = json.dumps(cuerpo, ensure_ascii=False)
        if len(cuerpo_str) > 140:
            cuerpo_str = cuerpo_str[:137] + "..."
        print(f"  {Colors.CYAN}Response:{Colors.END} {cuerpo_str}")
    except ValueError:
        txt = respuesta.text.strip().replace("\n", " ")
        print(f"  {Colors.CYAN}Response:{Colors.END} {txt[:140]}")


def ejecutar_test(session, nombre, metodo, url, datos=None, token=None, esperado=None, validacion_extra=None):
    RESUMEN["total"] += 1
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    try:
        resp = session.request(metodo, url, json=datos, headers=headers, timeout=TIMEOUT)
    except requests.exceptions.ConnectionError:
        print(f"{Colors.RED}❌ [{nombre}] No se pudo conectar al servidor en {BASE_URL}{Colors.END}")
        RESUMEN["fail"] += 1
        RESUMEN["detalles_fallos"].append((nombre, "ConnectionError", "Servidor no alcanzable"))
        return None
    except Exception as e:
        print(f"{Colors.RED}❌ [{nombre}] Error en la llamada: {e}{Colors.END}")
        RESUMEN["fail"] += 1
        RESUMEN["detalles_fallos"].append((nombre, type(e).__name__, str(e)))
        return None

    print_result(nombre, metodo, url, datos, resp)

    status_ok = True
    if esperado is not None:
        if isinstance(esperado, (list, tuple)):
            status_ok = resp.status_code in esperado
        else:
            status_ok = resp.status_code == esperado

    extra_ok = True
    extra_msg = ""
    if status_ok and validacion_extra and callable(validacion_extra):
        try:
            extra_ok, extra_msg = validacion_extra(resp)
        except Exception as ex:
            extra_ok = False
            extra_msg = f"Excepción en validación extra: {ex}"

    if status_ok and extra_ok:
        print(f"  {Colors.GREEN}✓ PASÓ ({resp.status_code}){Colors.END}\n")
        RESUMEN["ok"] += 1
    else:
        motivo = f"Esperado {esperado}, se obtuvo {resp.status_code}"
        if not extra_ok and extra_msg:
            motivo += f" | {extra_msg}"
        print(f"  {Colors.RED}✗ FALLÓ: {motivo}{Colors.END}\n")
        RESUMEN["fail"] += 1
        RESUMEN["detalles_fallos"].append((nombre, resp.status_code, motivo))

    return resp


def resolver_token(session):
    if AUTH_TOKEN and AUTH_TOKEN.strip():
        print(f"{Colors.GREEN}✓ Usando AUTH_TOKEN desde variable de entorno.{Colors.END}")
        return AUTH_TOKEN.strip()

    if SUPABASE_URL and SUPABASE_ANON_KEY and ADMIN_EMAIL and ADMIN_PASSWORD:
        print(f"{Colors.YELLOW}⚠️  No se encontró AUTH_TOKEN. Intentando login Supabase...{Colors.END}")
        try:
            resp = session.post(
                f"{SUPABASE_URL}/auth/v1/token?grant_type=password",
                json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
                headers={"apikey": SUPABASE_ANON_KEY, "Content-Type": "application/json"},
                timeout=TIMEOUT,
            )
            if resp.status_code == 200:
                print(f"{Colors.GREEN}✓ Login exitoso en Supabase.{Colors.END}")
                return resp.json().get("access_token")
            print(f"{Colors.RED}❌ Login en Supabase falló ({resp.status_code}){Colors.END}")
        except Exception as e:
            print(f"{Colors.RED}❌ Error contactando Supabase: {e}{Colors.END}")

    print(f"{Colors.YELLOW}ℹ️  Define AUTH_TOKEN para ejecutar los casos autenticados:{Colors.END}")
    print("   PowerShell: $env:AUTH_TOKEN = \"tu_token_aqui\"")
    print("   Bash      : export AUTH_TOKEN=\"tu_token_aqui\"")
    return None


def main():
    session = requests.Session()
    hoy = date.today()
    hace_7_dias = hoy - timedelta(days=7)

    print(f"\n{Colors.BOLD}{Colors.MAGENTA}{'#' * 75}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.MAGENTA}  OptiWay Backend — Suite API / E2E (tests/api/test_suite.py){Colors.END}")
    print(f"{Colors.BOLD}{Colors.MAGENTA}{'#' * 75}{Colors.END}")
    print(f"  {Colors.CYAN}Target URL:{Colors.END} {BASE_URL}")
    print(f"  {Colors.CYAN}Hora local:{Colors.END} {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    token = resolver_token(session)

    # ==============================================================================
    # 1. AUTORIZACIÓN: SOLICITUDES SIN TOKEN (401 Unauthorized)
    # ==============================================================================
    print_header("1. AUTORIZACIÓN: Endpoints protegidos sin token (401 Unauthorized)")

    endpoints_auth = [
        ("GET /api/usuarios", "GET", f"{BASE_URL}/api/usuarios", None),
        ("GET /api/usuarios/1", "GET", f"{BASE_URL}/api/usuarios/1", None),
        ("GET /api/usuarios/me", "GET", f"{BASE_URL}/api/usuarios/me", None),
        ("DELETE /api/usuarios/1", "DELETE", f"{BASE_URL}/api/usuarios/1", None),
        ("PATCH /api/usuarios/1/rol", "PATCH", f"{BASE_URL}/api/usuarios/1/rol", {"rol": "PLANIFICADOR"}),
        ("GET /api/tiendas", "GET", f"{BASE_URL}/api/tiendas", None),
        ("POST /api/tiendas", "POST", f"{BASE_URL}/api/tiendas", {"nombre": "T", "direccion": "D", "ciudad": "C"}),
        ("PUT /api/tiendas/1/encargado", "PUT", f"{BASE_URL}/api/tiendas/1/encargado", {"encargadoId": 1}),
        ("PATCH /api/tiendas/1/estado", "PATCH", f"{BASE_URL}/api/tiendas/1/estado", {"activo": True}),
        ("GET /api/tiendas/1/ordenes-en-transito", "GET", f"{BASE_URL}/api/tiendas/1/ordenes-en-transito", None),
        ("POST /api/tiendas/1/ventas", "POST", f"{BASE_URL}/api/tiendas/1/ventas", {"productoId": 1, "cantidad": 1}),
        ("POST /api/tiendas/1/solicitudes-reposicion", "POST", f"{BASE_URL}/api/tiendas/1/solicitudes-reposicion", {"productoId": 1, "cantidad": 5}),
        ("GET /api/solicitudes-reposicion", "GET", f"{BASE_URL}/api/solicitudes-reposicion", None),
        ("GET /api/cd", "GET", f"{BASE_URL}/api/cd", None),
        ("GET /api/centros-distribucion", "GET", f"{BASE_URL}/api/centros-distribucion", None),
        ("POST /api/cd", "POST", f"{BASE_URL}/api/cd", {"nombre": "CD", "direccion": "D", "capacidad": 100}),
        ("PATCH /api/cd/1/estado", "PATCH", f"{BASE_URL}/api/cd/1/estado", {"activo": True}),
        ("PUT /api/cd/1/encargado", "PUT", f"{BASE_URL}/api/cd/1/encargado", {"encargadoId": 1}),
        ("POST /api/cd/1/ingresos", "POST", f"{BASE_URL}/api/cd/1/ingresos", {"productoId": 1, "cantidad": 1, "origen": "Prov"}),
        ("PATCH /api/cd/1/inventario", "PATCH", f"{BASE_URL}/api/cd/1/inventario", {"productoId": 1, "nuevaCantidad": 10}),
        ("GET /api/cd/1/ordenes", "GET", f"{BASE_URL}/api/cd/1/ordenes", None),
        ("GET /api/productos", "GET", f"{BASE_URL}/api/productos", None),
        ("POST /api/productos", "POST", f"{BASE_URL}/api/productos", {"identificador": "P-1", "nombre": "P", "categoria": "C", "precio": 10}),
        ("PATCH /api/productos/1", "PATCH", f"{BASE_URL}/api/productos/1", {"nombre": "P2"}),
        ("PATCH /api/productos/1/estado", "PATCH", f"{BASE_URL}/api/productos/1/estado", {"activo": True}),
        ("GET /api/productos/1/tiene-stock", "GET", f"{BASE_URL}/api/productos/1/tiene-stock", None),
        ("GET /api/inventario", "GET", f"{BASE_URL}/api/inventario?tiendaId=1", None),
        ("GET /api/inventario/centro-distribucion", "GET", f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId=1", None),
        ("POST /api/inventario/ajustes", "POST", f"{BASE_URL}/api/inventario/ajustes", {"tiendaId": 1, "productoId": 1, "nuevaCantidad": 10}),
        ("GET /api/demanda/patrones", "GET", f"{BASE_URL}/api/demanda/patrones?productoId=1&tiendaIds=1&desde={hace_7_dias}&hasta={hoy}", None),
        ("POST /api/recomendaciones-reposicion", "POST", f"{BASE_URL}/api/recomendaciones-reposicion", {"productoId": 1, "cantidad": 5, "centroDistribucionOrigenId": 1, "tiendaDestinoId": 1}),
        ("GET /api/recomendaciones-reposicion", "GET", f"{BASE_URL}/api/recomendaciones-reposicion", None),
        ("PATCH /api/recomendaciones-reposicion/1/aprobar", "PATCH", f"{BASE_URL}/api/recomendaciones-reposicion/1/aprobar", {}),
        ("PATCH /api/recomendaciones-reposicion/1/rechazar", "PATCH", f"{BASE_URL}/api/recomendaciones-reposicion/1/rechazar", {"motivo": "Prueba"}),
        ("GET /api/reportes/rotacion", "GET", f"{BASE_URL}/api/reportes/rotacion?tiendaId=1&desde={hace_7_dias}&hasta={hoy}", None),
        ("GET /api/reportes/rotacion/exportar", "GET", f"{BASE_URL}/api/reportes/rotacion/exportar?tiendaId=1&desde={hace_7_dias}&hasta={hoy}", None),
        ("GET /admin/logs", "GET", f"{BASE_URL}/admin/logs", None),
        ("GET /admin/metrics", "GET", f"{BASE_URL}/admin/metrics", None),
    ]

    for nombre, metodo, url, payload in endpoints_auth:
        ejecutar_test(session, f"AUTH - {nombre}", metodo, url, payload, token=None, esperado=401)

    if not token:
        print(f"{Colors.YELLOW}⚠️  Saltando secciones 2..6 porque no hay AUTH_TOKEN configurado.{Colors.END}\n")
    else:
        # ==============================================================================
        # 2. TYPE MISMATCH: IDs no numéricos o UUIDs en PathParams Long (400 Bad Request)
        # ==============================================================================
        print_header("2. TYPE MISMATCH: IDs no numéricos o UUIDs en Path Params (400 Bad Request)")

        endpoints_type_mismatch = [
            ("GET /api/productos/abc/tiene-stock", "GET", f"{BASE_URL}/api/productos/abc/tiene-stock", None),
            ("GET /api/productos/UUID/tiene-stock", "GET", f"{BASE_URL}/api/productos/{ID_UUID_INVALIDO}/tiene-stock", None),
            ("PATCH /api/productos/UUID/estado", "PATCH", f"{BASE_URL}/api/productos/{ID_UUID_INVALIDO}/estado", {"activo": True}),
            ("PUT /api/cd/UUID/encargado", "PUT", f"{BASE_URL}/api/cd/{ID_UUID_INVALIDO}/encargado", {"encargadoId": 1}),
            ("GET /api/cd/UUID/ordenes", "GET", f"{BASE_URL}/api/cd/{ID_UUID_INVALIDO}/ordenes", None),
            ("POST /api/cd/UUID/ingresos", "POST", f"{BASE_URL}/api/cd/{ID_UUID_INVALIDO}/ingresos", {"productoId": 1, "cantidad": 1, "origen": "P"}),
            ("POST /api/tiendas/UUID/ventas", "POST", f"{BASE_URL}/api/tiendas/{ID_UUID_INVALIDO}/ventas", {"productoId": 1, "cantidad": 1}),
        ]

        for nombre, metodo, url, payload in endpoints_type_mismatch:
            ejecutar_test(session, f"TYPEMISMATCH - {nombre}", metodo, url, payload, token=token, esperado=400)

        # ==============================================================================
        # 3. RECURSOS INEXISTENTES CON ID NUMÉRICO LONG (404 Not Found)
        # ==============================================================================
        print_header(f"3. RECURSO INEXISTENTE: IDs numéricos inexistentes ({ID_NUMERICO_INEXISTENTE})")

        endpoints_inexistentes = [
            ("PATCH /api/usuarios/999999/rol", "PATCH", f"{BASE_URL}/api/usuarios/{ID_NUMERICO_INEXISTENTE}/rol", {"rol": "PLANIFICADOR"}, 404),
            ("PUT /api/tiendas/999999/encargado", "PUT", f"{BASE_URL}/api/tiendas/{ID_NUMERICO_INEXISTENTE}/encargado", {"encargadoId": 1}, 404),
            ("PUT /api/cd/999999/encargado", "PUT", f"{BASE_URL}/api/cd/{ID_NUMERICO_INEXISTENTE}/encargado", {"encargadoId": 1}, 404),
            ("GET /api/cd/999999/ordenes", "GET", f"{BASE_URL}/api/cd/{ID_NUMERICO_INEXISTENTE}/ordenes", None, (403, 404)),
            ("POST /api/cd/999999/ingresos", "POST", f"{BASE_URL}/api/cd/{ID_NUMERICO_INEXISTENTE}/ingresos", {"productoId": 1, "cantidad": 1, "origen": "Proveedor"}, 404),
            ("POST /api/tiendas/999999/ventas", "POST", f"{BASE_URL}/api/tiendas/{ID_NUMERICO_INEXISTENTE}/ventas", {"productoId": 1, "cantidad": 1}, 404),
            ("PATCH /api/productos/999999", "PATCH", f"{BASE_URL}/api/productos/{ID_NUMERICO_INEXISTENTE}", {"nombre": "Nuevo"}, 404),
            ("PATCH /api/productos/999999/estado", "PATCH", f"{BASE_URL}/api/productos/{ID_NUMERICO_INEXISTENTE}/estado", {"activo": True}, 404),
            ("GET /api/inventario?tiendaId=999999", "GET", f"{BASE_URL}/api/inventario?tiendaId={ID_NUMERICO_INEXISTENTE}", None, 404),
            ("GET /api/inventario/centro-distribucion?centroDistribucionId=999999", "GET", f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={ID_NUMERICO_INEXISTENTE}", None, 404),
        ]

        for nombre, metodo, url, payload, esp in endpoints_inexistentes:
            ejecutar_test(session, f"NOT_FOUND - {nombre}", metodo, url, payload, token=token, esperado=esp)

        # ==============================================================================
        # 4. VALIDACIÓN DE CUERPO Y PARÁMETROS OBLIGATORIOS (400 Bad Request)
        # ==============================================================================
        print_header("4. VALIDACIÓN: Cuerpos incompletos, cantidades negativas y unidades (400 Bad Request)")

        endpoints_validacion = [
            ("POST /api/productos (body vacío)", "POST", f"{BASE_URL}/api/productos", {}),
            ("POST /api/productos (precio negativo)", "POST", f"{BASE_URL}/api/productos", {"identificador": "BAD-1", "nombre": "Prod", "categoria": "Cat", "precio": -5}),
            ("POST /api/productos (unidad inválida)", "POST", f"{BASE_URL}/api/productos", {"identificador": "BAD-2", "nombre": "Prod", "categoria": "Cat", "precio": 100, "unidadMedida": "invalida"}),
            ("POST /api/tiendas (body incompleto)", "POST", f"{BASE_URL}/api/tiendas", {"nombre": ""}),
            ("POST /api/centros-distribucion (capacidad <= 0)", "POST", f"{BASE_URL}/api/centros-distribucion", {"nombre": "CD", "direccion": "Dir", "capacidad": 0}),
            ("GET /api/inventario (sin tiendaId)", "GET", f"{BASE_URL}/api/inventario", None),
            ("GET /api/inventario/centro-distribucion (sin centroDistribucionId)", "GET", f"{BASE_URL}/api/inventario/centro-distribucion", None),
            ("POST /api/inventario/ajustes (nuevaCantidad negativa)", "POST", f"{BASE_URL}/api/inventario/ajustes", {"tiendaId": 1, "productoId": 1, "nuevaCantidad": -1}),
            ("POST /api/cd/1/ingresos (cantidad cero)", "POST", f"{BASE_URL}/api/cd/1/ingresos", {"productoId": 1, "cantidad": 0, "origen": "Prov"}),
            ("POST /api/tiendas/1/ventas (cantidad negativa)", "POST", f"{BASE_URL}/api/tiendas/1/ventas", {"productoId": 1, "cantidad": -1}),
        ]

        for nombre, metodo, url, payload in endpoints_validacion:
            ejecutar_test(session, f"VALIDACION - {nombre}", metodo, url, payload, token=token, esperado=400)

        # ==============================================================================
        # 5. RUTAS Y MÉTODOS HTTP NO SOPORTADOS (404 / 405)
        # ==============================================================================
        print_header("5. RUTAS Y MÉTODOS NO SOPORTADOS (404 / 405)")

        rutas_especiales = [
            ("GET /api/ruta-inexistente-12345", "GET", f"{BASE_URL}/api/ruta-inexistente-12345", None, 404),
            ("DELETE /api/productos/1 (método no implementado)", "DELETE", f"{BASE_URL}/api/productos/1", None, 405),
        ]

        for nombre, metodo, url, payload, esp in rutas_especiales:
            ejecutar_test(session, f"ROUTING - {nombre}", metodo, url, payload, token=token, esperado=esp)

        # ==============================================================================
        # 6. FLUJOS FUNCIONALES CRUD (200 OK / 201 Created)
        # ==============================================================================
        print_header("6. FLUJOS FUNCIONALES COMPLETOS (CRUD con captura dinámica de IDs)")

        timestamp = int(time.time())

        # Endpoints públicos
        ejecutar_test(session, "PUBLIC - GET /api/estadisticas", "GET", f"{BASE_URL}/api/estadisticas", esperado=200)
        ejecutar_test(session, "PUBLIC - GET /graph", "GET", f"{BASE_URL}/graph", esperado=200)

        r_me = ejecutar_test(session, "FUNC - GET /api/usuarios/me", "GET", f"{BASE_URL}/api/usuarios/me", token=token, esperado=200)
        if r_me and r_me.status_code == 200:
            VARS["usuario_id"] = r_me.json().get("id")
            if VARS["usuario_id"]:
                ejecutar_test(session, f"FUNC - GET /api/usuarios/{VARS['usuario_id']}", "GET",
                              f"{BASE_URL}/api/usuarios/{VARS['usuario_id']}", token=token, esperado=200)

        ejecutar_test(session, "FUNC - GET /api/usuarios", "GET", f"{BASE_URL}/api/usuarios", token=token, esperado=200)

        r_prod = ejecutar_test(
            session, "FUNC - POST /api/productos (normaliza unidad 'kilogramos' -> 'KG')", "POST",
            f"{BASE_URL}/api/productos",
            datos={
                "identificador": f"SKU-{timestamp}",
                "nombre": f"Producto Test {timestamp}",
                "categoria": "GENERAL",
                "precio": 99.90,
                "unidadMedida": "kilogramos",
            },
            token=token,
            esperado=201,
            validacion_extra=lambda r: (r.json().get("unidadMedida") == "KG", "Debe normalizar unidadMedida a KG"),
        )
        if r_prod and r_prod.status_code == 201:
            VARS["producto_id"] = r_prod.json().get("id")

        ejecutar_test(session, "FUNC - GET /api/productos", "GET", f"{BASE_URL}/api/productos", token=token, esperado=200)

        if VARS["producto_id"]:
            pid = VARS["producto_id"]
            ejecutar_test(session, f"FUNC - GET /api/productos/{pid}/tiene-stock", "GET", f"{BASE_URL}/api/productos/{pid}/tiene-stock", token=token, esperado=200)
            ejecutar_test(session, f"FUNC - PATCH /api/productos/{pid}", "PATCH", f"{BASE_URL}/api/productos/{pid}", datos={"precio": 149.99}, token=token, esperado=200)

        r_cd = ejecutar_test(
            session, "FUNC - POST /api/cd", "POST", f"{BASE_URL}/api/cd",
            datos={"nombre": f"Centro Dist {timestamp}", "direccion": "Av. Logística 123", "capacidad": 5000},
            token=token, esperado=201,
        )
        if r_cd and r_cd.status_code == 201:
            VARS["cd_id"] = r_cd.json().get("id")

        ejecutar_test(session, "FUNC - GET /api/cd", "GET", f"{BASE_URL}/api/cd", token=token, esperado=200)

        r_tnd = ejecutar_test(
            session, "FUNC - POST /api/tiendas", "POST", f"{BASE_URL}/api/tiendas",
            datos={"nombre": f"Tienda Centro {timestamp}", "direccion": "Calle Comercial 456", "ciudad": "Bogotá"},
            token=token, esperado=201,
        )
        if r_tnd and r_tnd.status_code == 201:
            VARS["tienda_id"] = r_tnd.json().get("id")

        ejecutar_test(session, "FUNC - GET /api/tiendas", "GET", f"{BASE_URL}/api/tiendas", token=token, esperado=200)

        cd_target = VARS["cd_id"] or 1
        prod_target = VARS["producto_id"] or 1
        ejecutar_test(
            session, f"FUNC - POST /api/cd/{cd_target}/ingresos", "POST",
            f"{BASE_URL}/api/cd/{cd_target}/ingresos",
            datos={"productoId": prod_target, "cantidad": 50, "origen": "Proveedor Internacional"},
            token=token, esperado=201,
        )
        ejecutar_test(
            session, f"FUNC - GET /api/cd/{cd_target}/ordenes", "GET",
            f"{BASE_URL}/api/cd/{cd_target}/ordenes", token=token, esperado=200,
        )
        ejecutar_test(
            session, f"FUNC - PATCH /api/cd/{cd_target}/inventario", "PATCH",
            f"{BASE_URL}/api/cd/{cd_target}/inventario",
            datos={"productoId": prod_target, "nuevaCantidad": 45},
            token=token, esperado=(200, 403),
        )
        ejecutar_test(
            session, f"FUNC - GET /api/inventario/centro-distribucion?centroDistribucionId={cd_target}", "GET",
            f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_target}",
            token=token, esperado=200,
        )
        tnd_target = VARS["tienda_id"] or 1
        ejecutar_test(
            session, f"FUNC - GET /api/tiendas/{tnd_target}/ordenes-en-transito", "GET",
            f"{BASE_URL}/api/tiendas/{tnd_target}/ordenes-en-transito", token=token, esperado=200,
        )
        ejecutar_test(
            session, "FUNC - GET /api/solicitudes-reposicion", "GET",
            f"{BASE_URL}/api/solicitudes-reposicion", token=token, esperado=(200, 403),
        )
        ejecutar_test(
            session, "FUNC - GET /api/recomendaciones-reposicion", "GET",
            f"{BASE_URL}/api/recomendaciones-reposicion", token=token, esperado=(200, 403),
        )
        ejecutar_test(
            session, "FUNC - GET /api/demanda/patrones", "GET",
            f"{BASE_URL}/api/demanda/patrones?productoId={prod_target}&tiendaIds={tnd_target}&desde={hace_7_dias}&hasta={hoy}",
            token=token, esperado=(200, 403),
        )
        ejecutar_test(
            session, "FUNC - GET /api/reportes/rotacion", "GET",
            f"{BASE_URL}/api/reportes/rotacion?tiendaId={tnd_target}&desde={hace_7_dias}&hasta={hoy}",
            token=token, esperado=(200, 403),
        )
        ejecutar_test(
            session, "FUNC - GET /api/reportes/rotacion/exportar", "GET",
            f"{BASE_URL}/api/reportes/rotacion/exportar?tiendaId={tnd_target}&desde={hace_7_dias}&hasta={hoy}",
            token=token, esperado=(200, 403),
        )

    print_header("RESUMEN GLOBAL DE EJECUCIÓN")
    pct_ok = (RESUMEN["ok"] / RESUMEN["total"] * 100) if RESUMEN["total"] > 0 else 0
    print(f"  {Colors.BOLD}Total pruebas ejecutadas:{Colors.END} {RESUMEN['total']}")
    print(f"  {Colors.GREEN}✓ Exitosas (Passed)     :{Colors.END} {RESUMEN['ok']} ({pct_ok:.1f}%)")
    print(f"  {Colors.RED}✗ Fallidas (Failed)     :{Colors.END} {RESUMEN['fail']}")

    if RESUMEN["fail"] > 0:
        print(f"\n{Colors.RED}{Colors.BOLD}Detalle de pruebas que no pasaron:{Colors.END}")
        for idx, (nombre, code, motivo) in enumerate(RESUMEN["detalles_fallos"], 1):
            print(f"  {idx}. {nombre} -> {code}: {motivo}")
        sys.exit(1)


if __name__ == "__main__":
    main()
