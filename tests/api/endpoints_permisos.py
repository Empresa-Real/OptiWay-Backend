"""
Prueba de autorización por rol de OptiWay Backend.

Verifica que cada rol reciba 403 cuando intenta acceder a algo para lo que no
tiene permisos, y que SÍ pueda acceder a lo que le corresponde.

Modelo de permisos (AuthorizationService):
  - ADMINISTRADOR: todo (usuarios, catálogo, tiendas, CD, inventario, ventas, ingresos).
  - PLANIFICADOR: toda la logística (catálogo, tiendas, CD, inventario, ventas,
    ingresos) PERO no gestiona usuarios.
  - ENCARGADO_TIENDA: sólo su tienda (inventario y ventas de ESA tienda).
  - ENCARGADO_CD:    sólo su centro (inventario e ingresos de ESE centro).

Orden importante en los servicios: primero se valida existencia del recurso y
luego la autorización. Por eso, para forzar un 403 hay que apuntar a un recurso
EXISTENTE pero ajeno (uno inexistente daría 404).

Credenciales por rol (variables de entorno, con defaults):
  ADMIN_EMAIL / ADMIN_PASSWORD
  PLANIFICADOR_EMAIL / PLANIFICADOR_PASSWORD
  TIENDA_EMAIL / TIENDA_PASSWORD
  CD_EMAIL / CD_PASSWORD

Uso:
    python endpoints_permisos.py
"""

import json
import os
import sys
from datetime import datetime

import requests

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# ==================== CONFIGURACIÓN ==========================================
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
SUPABASE_URL = os.environ.get("SUPABASE_URL", "https://cehzzcvnkiuvkabglrzp.supabase.co")
SUPABASE_ANON_KEY = os.environ.get(
    "SUPABASE_ANON_KEY", ""
)
SUPABASE_LOGIN_URL = f"{SUPABASE_URL}/auth/v1/token?grant_type=password"
TIMEOUT = 15

CREDENCIALES = {
    "ADMINISTRADOR": {
        "email": os.environ.get("ADMIN_EMAIL", "admin@empresa.com"),
        "password": os.environ.get("ADMIN_PASSWORD", ""),
    },
    "PLANIFICADOR": {
        "email": os.environ.get("PLANIFICADOR_EMAIL", "ana@empresa.com"),
        "password": os.environ.get("PLANIFICADOR_PASSWORD", ""),
    },
    "ENCARGADO_TIENDA": {
        "email": os.environ.get("TIENDA_EMAIL", "carlos@empresa.com"),
        "password": os.environ.get("TIENDA_PASSWORD", ""),
    },
    "ENCARGADO_CD": {
        "email": os.environ.get("CD_EMAIL", "roberto@empresa.com"),
        # OJO: la contraseña real termina en un espacio.
        "password": os.environ.get("CD_PASSWORD", ""),
    },
}


class Colors:
    GREEN = "\033[92m"
    RED = "\033[91m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    CYAN = "\033[96m"
    BOLD = "\033[1m"
    END = "\033[0m"


RESUMEN = {"ok": 0, "fail": 0, "omitido": 0}


def print_header(texto):
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  {texto}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}\n")


def print_result(nombre, metodo, url, respuesta):
    print(f"\n{Colors.BLUE}[{nombre}]{Colors.END}")
    print(f"  {Colors.CYAN}Método:{Colors.END} {metodo} {url}")
    print(f"  {Colors.CYAN}Status:{Colors.END} {respuesta.status_code}")
    try:
        print(f"  {Colors.CYAN}Body JSON:{Colors.END} {json.dumps(respuesta.json(), indent=2, ensure_ascii=False)}")
    except ValueError:
        print(f"  {Colors.CYAN}Body texto:{Colors.END} {respuesta.text[:300]}")
    print()


def login(session, rol, cred):
    if not cred["email"] or not cred["password"]:
        print(f"  {Colors.YELLOW}⚠️  {rol}: sin credenciales (define {rol}_PASSWORD). Se omite.{Colors.END}")
        return None
    resp = session.post(
        SUPABASE_LOGIN_URL,
        json={"email": cred["email"], "password": cred["password"]},
        headers={"apikey": SUPABASE_ANON_KEY, "Content-Type": "application/json"},
        timeout=TIMEOUT,
    )
    if resp.status_code == 200:
        print(f"  {Colors.GREEN}✓{Colors.END} {rol}: token obtenido ({cred['email']})")
        return resp.json().get("access_token")
    print(f"  {Colors.RED}✗{Colors.END} {rol}: login falló ({resp.status_code}) - {cred['email']}")
    return None


def probar(session, nombre, metodo, url, token, datos=None, esperados=(200, 201), esperado_fijo=None):
    """
    Ejecuta una petición y compara el status contra los códigos aceptables.
    Si esperado_fijo se define, se usa ese único valor.
    """
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    try:
        resp = session.request(metodo, url, json=datos, headers=headers, timeout=TIMEOUT)
    except requests.exceptions.ConnectionError:
        print(f"\n{Colors.RED}[{nombre}] ❌ No se pudo conectar a {BASE_URL}{Colors.END}")
        RESUMEN["fail"] += 1
        return None
    except Exception as e:
        print(f"\n{Colors.RED}[{nombre}] ❌ Error: {e}{Colors.END}")
        RESUMEN["fail"] += 1
        return None

    print_result(nombre, metodo, url, resp)

    aceptables = (esperado_fijo,) if esperado_fijo is not None else esperados
    if resp.status_code in aceptables:
        print(f"  {Colors.GREEN}✓ OK {resp.status_code} (esperado {'/'.join(map(str, aceptables))}){Colors.END}")
        RESUMEN["ok"] += 1
    else:
        print(f"  {Colors.RED}✗ Se esperaba {'/'.join(map(str, aceptables))}, se obtuvo {resp.status_code}{Colors.END}")
        RESUMEN["fail"] += 1
    return resp


def _get(session, url, token):
    try:
        resp = session.get(url, headers={"Authorization": f"Bearer {token}"}, timeout=TIMEOUT)
        return resp.json() if resp.status_code == 200 else []
    except Exception:
        return []


def descubrir_contexto(session, admin_token):
    """Descubre ids reales y las asignaciones de carlos/roberto."""
    ctx = {"tiendaPropia": None, "tiendaAjena": None, "cdPropio": None, "cdAjeno": None,
           "producto": None, "usuarioOtro": None, "idPorEmail": {}}

    usuarios = _get(session, f"{BASE_URL}/api/usuarios", admin_token)
    ctx["idPorEmail"] = {u["email"]: u["id"] for u in usuarios if "email" in u}

    carlos_id = ctx["idPorEmail"].get(CREDENCIALES["ENCARGADO_TIENDA"]["email"])
    roberto_id = ctx["idPorEmail"].get(CREDENCIALES["ENCARGADO_CD"]["email"])
    plan_id = ctx["idPorEmail"].get(CREDENCIALES["PLANIFICADOR"]["email"])

    def id_ajeno_a(propio):
        """Primer id de usuario que no sea `propio` (para forzar el 403 del IDOR)."""
        return next((u["id"] for u in usuarios if u["id"] != propio), None)

    ctx["usuarioOtro"] = {
        "ADMINISTRADOR": id_ajeno_a(ctx["idPorEmail"].get(CREDENCIALES["ADMINISTRADOR"]["email"])),
        "PLANIFICADOR": id_ajeno_a(plan_id),
        "ENCARGADO_TIENDA": id_ajeno_a(carlos_id),
        "ENCARGADO_CD": id_ajeno_a(roberto_id),
    }

    tiendas = _get(session, f"{BASE_URL}/api/tiendas", admin_token)
    propias = [t["id"] for t in tiendas if t.get("encargadoId") == carlos_id]
    ajenas = [t["id"] for t in tiendas if t.get("encargadoId") != carlos_id]
    ctx["tiendaPropia"] = propias[0] if propias else None
    ctx["tiendaAjena"] = ajenas[0] if ajenas else None

    cds = _get(session, f"{BASE_URL}/api/centros-distribucion", admin_token)
    propios = [c["id"] for c in cds if c.get("encargadoId") == roberto_id]
    ajenos = [c["id"] for c in cds if c.get("encargadoId") != roberto_id]
    ctx["cdPropio"] = propios[0] if propios else None
    ctx["cdAjeno"] = ajenos[0] if ajenos else None

    productos = _get(session, f"{BASE_URL}/api/productos", admin_token)
    ctx["producto"] = productos[0]["id"] if productos else None

    print(f"  admin ve: tiendaPropia(carlos)={ctx['tiendaPropia']} tiendaAjena={ctx['tiendaAjena']} "
          f"cdPropio(roberto)={ctx['cdPropio']} cdAjeno={ctx['cdAjeno']} producto={ctx['producto']}")
    print(f"  ids de usuario: {ctx['idPorEmail']}")
    print()
    return ctx


def main():
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  PRUEBA DE PERMISOS POR ROL - OptiWay Backend{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"\n{Colors.YELLOW}URL base: {BASE_URL}{Colors.END}")
    print(f"{Colors.YELLOW}Hora: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{Colors.END}\n")

    session = requests.Session()

    # PASO 1: tokens de todos los roles
    print_header("PASO 1: TOKENS POR ROL")
    tokens = {}
    for rol, cred in CREDENCIALES.items():
        tokens[rol] = login(session, rol, cred)

    if not tokens.get("ADMINISTRADOR"):
        print(f"{Colors.RED}No hay token de admin; no se puede descubrir el contexto. Abortando.{Colors.END}")
        return

    # PASO 2: descubrir ids y asignaciones
    print_header("PASO 2: CONTEXTO (ids reales y asignaciones)")
    ctx = descubrir_contexto(session, tokens["ADMINISTRADOR"])

    tienda_propia = ctx["tiendaPropia"]
    tienda_ajena = ctx["tiendaAjena"]
    cd_propio = ctx["cdPropio"]
    cd_ajeno = ctx["cdAjeno"]
    producto = ctx["producto"] or 1
    usuario_otro = ctx["usuarioOtro"]

    # PASO 3: matriz de permisos
    print_header("PASO 3: MATRIZ DE PERMISOS (se espera 403 donde no hay permiso)")

    # --- Gestión de usuarios: sólo ADMIN ---
    print(f"\n{Colors.BOLD}{Colors.CYAN}>>> Gestión de usuarios (sólo ADMINISTRADOR){Colors.END}")
    for rol in ["PLANIFICADOR", "ENCARGADO_TIENDA", "ENCARGADO_CD"]:
        tok = tokens.get(rol)
        if not tok:
            RESUMEN["omitido"] += 3
            continue
        probar(session, f"{rol} - GET /api/usuarios (403)", "GET", f"{BASE_URL}/api/usuarios", tok, esperado_fijo=403)
        probar(session, f"{rol} - POST /api/usuarios (403)", "POST", f"{BASE_URL}/api/usuarios", tok,
               {"nombre": "X", "email": "x@x.com", "rol": "ENCARGADO_TIENDA"}, esperado_fijo=403)
        # Se consulta el perfil de OTRO usuario (no el propio): cada usuario sí
        # puede ver el suyo, por eso hay que apuntar a un id ajeno para forzar 403.
        otro = usuario_otro.get(rol)
        if otro:
            probar(session, f"{rol} - GET /api/usuarios/{otro} de OTRO (403)", "GET",
                   f"{BASE_URL}/api/usuarios/{otro}", tok, esperado_fijo=403)
        else:
            RESUMEN["omitido"] += 1

    # --- Catálogo: sólo ADMIN y PLANIFICADOR ---
    print(f"\n{Colors.BOLD}{Colors.CYAN}>>> Crear catálogo (ADMIN y PLANIFICADOR, no encargados){Colors.END}")
    for rol in ["ENCARGADO_TIENDA", "ENCARGADO_CD"]:
        tok = tokens.get(rol)
        if not tok:
            RESUMEN["omitido"] += 3
            continue
        probar(session, f"{rol} - POST /api/productos (403)", "POST", f"{BASE_URL}/api/productos", tok,
               {"identificador": "TST-1", "nombre": "T", "categoria": "C", "precio": 1.0, "unidadMedida": "UNIDAD"},
               esperado_fijo=403)
        probar(session, f"{rol} - POST /api/tiendas (403)", "POST", f"{BASE_URL}/api/tiendas", tok,
               {"nombre": "T", "direccion": "D", "ciudad": "C"}, esperado_fijo=403)
        probar(session, f"{rol} - POST /api/centros-distribucion (403)", "POST",
               f"{BASE_URL}/api/centros-distribucion", tok,
               {"nombre": "C", "direccion": "D", "capacidad": 1}, esperado_fijo=403)

    if tokens.get("PLANIFICADOR"):
        print(f"\n{Colors.BOLD}{Colors.CYAN}>>> PLANIFICADOR sí puede gestionar logística (no 403){Colors.END}")
        probar(session, "PLANIFICADOR - POST /api/productos (permitido)", "POST",
               f"{BASE_URL}/api/productos", tokens["PLANIFICADOR"],
               {"identificador": f"PLAN-{datetime.now():%H%M%S}", "nombre": "Plan", "categoria": "C",
                "precio": 1.0, "unidadMedida": "UNIDAD"}, esperados=(201, 400, 409))

    # --- Inventario y ventas de tienda: sólo la propia ---
    print(f"\n{Colors.BOLD}{Colors.CYAN}>>> Tienda: el encargado sólo accede a LA SUYA{Colors.END}")
    tok_tienda = tokens.get("ENCARGADO_TIENDA")
    if tok_tienda and tienda_ajena:
        probar(session, "ENCARGADO_TIENDA - inventario de tienda AJENA (403)", "GET",
               f"{BASE_URL}/api/inventario?tiendaId={tienda_ajena}", tok_tienda, esperado_fijo=403)
        probar(session, "ENCARGADO_TIENDA - venta en tienda AJENA (403)", "POST",
               f"{BASE_URL}/api/tiendas/{tienda_ajena}/ventas", tok_tienda,
               {"productoId": producto, "cantidad": 1}, esperado_fijo=403)
    else:
        RESUMEN["omitido"] += 2
    if tok_tienda and tienda_propia:
        probar(session, "ENCARGADO_TIENDA - inventario de SU tienda (permitido)", "GET",
               f"{BASE_URL}/api/inventario?tiendaId={tienda_propia}", tok_tienda, esperados=(200,))
    else:
        RESUMEN["omitido"] += 1
    # El encargado de tienda tampoco puede tocar un CD
    tok_cd = tokens.get("ENCARGADO_CD")
    if tok_tienda and cd_propio:
        probar(session, "ENCARGADO_TIENDA - inventario de un CD (403)", "GET",
               f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_propio}",
               tok_tienda, esperado_fijo=403)
    else:
        RESUMEN["omitido"] += 1

    # --- Inventario e ingresos de CD: sólo el propio ---
    print(f"\n{Colors.BOLD}{Colors.CYAN}>>> CD: el encargado sólo accede a EL SUYO{Colors.END}")
    if tok_cd and cd_ajeno:
        probar(session, "ENCARGADO_CD - inventario de CD AJENO (403)", "GET",
               f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_ajeno}",
               tok_cd, esperado_fijo=403)
        probar(session, "ENCARGADO_CD - ingreso en CD AJENO (403)", "POST",
               f"{BASE_URL}/api/centros-distribucion/{cd_ajeno}/ingresos", tok_cd,
               {"productoId": producto, "cantidad": 1, "origen": "prueba"}, esperado_fijo=403)
    else:
        RESUMEN["omitido"] += 2
    if tok_cd and cd_propio:
        probar(session, "ENCARGADO_CD - inventario de SU CD (permitido)", "GET",
               f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_propio}",
               tok_cd, esperados=(200,))
    else:
        RESUMEN["omitido"] += 1
    # El encargado de CD tampoco puede tocar una tienda
    if tok_cd and tienda_ajena:
        probar(session, "ENCARGADO_CD - inventario de tienda (403)", "GET",
               f"{BASE_URL}/api/inventario?tiendaId={tienda_ajena}", tok_cd, esperado_fijo=403)
    else:
        RESUMEN["omitido"] += 1

    # --- Admin puede todo ---
    print(f"\n{Colors.BOLD}{Colors.CYAN}>>> ADMIN puede todo (control positivo){Colors.END}")
    tok_admin = tokens["ADMINISTRADOR"]
    probar(session, "ADMIN - GET /api/usuarios (permitido)", "GET", f"{BASE_URL}/api/usuarios", tok_admin, esperados=(200,))
    if tienda_ajena:
        probar(session, "ADMIN - inventario de cualquier tienda (permitido)", "GET",
               f"{BASE_URL}/api/inventario?tiendaId={tienda_ajena}", tok_admin, esperados=(200,))

    # RESUMEN
    print_header("RESUMEN")
    print(f"  {Colors.GREEN}✓ Cumplen lo esperado: {RESUMEN['ok']}{Colors.END}")
    print(f"  {Colors.RED}✗ No cumplen:          {RESUMEN['fail']}{Colors.END}")
    print(f"  {Colors.YELLOW}⚠ Omitidas:            {RESUMEN['omitido']}{Colors.END}")
    print(f"\n{Colors.YELLOW}Roles con token: {', '.join(r for r, t in tokens.items() if t) or 'ninguno'}{Colors.END}")
    print(f"{Colors.YELLOW}Para cubrir el PLANIFICADOR define PLANIFICADOR_PASSWORD. "
          f"Nota: la contraseña del CD termina en espacio.{Colors.END}\n")


if __name__ == "__main__":
    main()
