"""
Prueba de endpoints de OptiWay Backend.

- Autentica contra Supabase Auth (header `apikey` obligatorio) y usa el JWT
  resultante contra la API local.
- Recorre los endpoints por rol e imprime status + cuerpo para ver los codigos
  de error y respuesta de cada caso.

Uso:
    python endpoints.py
"""

import json
import os
import sys
from datetime import datetime

import requests

# La consola de Windows usa cp1252 por defecto y rompe con emojis/acentos.
# Forzamos UTF-8 en la salida para que el script corra sin UnicodeEncodeError.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# ==================== CONFIGURACIÓN ==========================================
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
SUPABASE_URL = os.environ.get("SUPABASE_URL", "https://cehzzcvnkiuvkabglrzp.supabase.co")
# Clave publishable/anon: Supabase Auth la exige en el header `apikey`.
# Sin ella, /auth/v1/token responde 401 aunque las credenciales sean válidas.
SUPABASE_ANON_KEY = os.environ.get(
    "SUPABASE_ANON_KEY", ""
)
SUPABASE_LOGIN_URL = f"{SUPABASE_URL}/auth/v1/token?grant_type=password"
TIMEOUT = 15

# Credenciales de prueba. Los emails por defecto son los que existen en la BD
# (verificados vía GET /api/usuarios). Las contraseñas se pueden sobreescribir
# por variable de entorno para no versionar secretos:
#   ADMIN_PASSWORD, PLANIFICADOR_PASSWORD, TIENDA_PASSWORD, CD_PASSWORD
USUARIOS = {
    "ADMINISTRADOR": {
        "email": os.environ.get("ADMIN_EMAIL", "admin@empresa.com"),
        "password": os.environ.get("ADMIN_PASSWORD", ""),
    },
    "ENCARGADO_TIENDA": {
        "email": os.environ.get("TIENDA_EMAIL", "carlos@empresa.com"),
        "password": os.environ.get("TIENDA_PASSWORD", ""),
    },
    "ENCARGADO_CD": {
        "email": os.environ.get("CD_EMAIL", "roberto@empresa.com"),
        "password": os.environ.get("CD_PASSWORD", ""),
    },
}


# ==================== SALIDA EN CONSOLA ======================================
class Colors:
    GREEN = "\033[92m"
    RED = "\033[91m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    CYAN = "\033[96m"
    BOLD = "\033[1m"
    END = "\033[0m"


def print_header(texto):
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  {texto}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}\n")


def print_result(nombre, metodo, url, datos_envio, respuesta):
    print(f"\n{Colors.BLUE}[{nombre}]{Colors.END}")
    print(f"  {Colors.CYAN}Método:{Colors.END} {metodo} {url}")
    if datos_envio:
        print(f"  {Colors.CYAN}Body:{Colors.END} {json.dumps(datos_envio, indent=2, ensure_ascii=False)}")
    print(f"  {Colors.CYAN}Status:{Colors.END} {respuesta.status_code}")
    try:
        cuerpo = respuesta.json()
        print(f"  {Colors.CYAN}Body JSON:{Colors.END} {json.dumps(cuerpo, indent=2, ensure_ascii=False)}")
    except ValueError:
        print(f"  {Colors.CYAN}Body texto:{Colors.END} {respuesta.text[:500]}")
    print()


def probar_endpoint(session, nombre, metodo, url, datos=None, token=None, esperado=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    try:
        resp = session.request(
            metodo, url, json=datos, headers=headers, timeout=TIMEOUT
        )
    except requests.exceptions.ConnectionError:
        print(f"\n{Colors.RED}[{nombre}]{Colors.END}")
        print(f"  {Colors.CYAN}Método:{Colors.END} {metodo} {url}")
        print(f"  {Colors.RED}❌ No se pudo conectar al servidor en {BASE_URL}{Colors.END}")
        print(f"  {Colors.YELLOW}¿Está corriendo el servidor?{Colors.END}\n")
        return None
    except Exception as e:
        print(f"\n{Colors.RED}[{nombre}]{Colors.END}")
        print(f"  {Colors.CYAN}Método:{Colors.END} {metodo} {url}")
        print(f"  {Colors.RED}❌ Error: {e}{Colors.END}\n")
        return None

    print_result(nombre, metodo, url, datos, resp)

    if esperado is not None:
        if resp.status_code == esperado:
            print(f"  {Colors.GREEN}✓ OK {resp.status_code} (esperado){Colors.END}")
        else:
            print(f"  {Colors.YELLOW}⚠️  Se esperaba {esperado}, se obtuvo {resp.status_code}{Colors.END}")
    elif resp.status_code >= 400:
        print(f"  {Colors.YELLOW}⚠️  Error {resp.status_code}{Colors.END}")
    else:
        print(f"  {Colors.GREEN}✓ OK {resp.status_code}{Colors.END}")

    return resp


# ==================== AUTENTICACIÓN ==========================================
def hacer_login(session, nombre_rol, credenciales):
    if not credenciales["email"] or not credenciales["password"]:
        print(f"\n{Colors.YELLOW}[{nombre_rol}]{Colors.END}")
        print(f"  {Colors.YELLOW}⚠️  Sin credenciales (define {nombre_rol}_PASSWORD). Saltando...{Colors.END}\n")
        return None

    print(f"\n{Colors.BOLD}{Colors.CYAN}🔐 Iniciando sesión como {nombre_rol}: {credenciales['email']}{Colors.END}")

    try:
        resp = session.post(
            SUPABASE_LOGIN_URL,
            json={"email": credenciales["email"], "password": credenciales["password"]},
            headers={"apikey": SUPABASE_ANON_KEY, "Content-Type": "application/json"},
            timeout=TIMEOUT,
        )

        if resp.status_code == 200:
            token = resp.json().get("access_token")
            if token:
                print(f"  {Colors.GREEN}✓ Login exitoso - Token obtenido{Colors.END}")
                return token
            print(f"  {Colors.YELLOW}⚠️  Login exitoso pero no se encontró access_token{Colors.END}")
            print(f"  {Colors.CYAN}Respuesta: {resp.text[:200]}{Colors.END}")
            return None
        if resp.status_code == 401:
            print(f"  {Colors.RED}❌ Credenciales inválidas (401){Colors.END}")
            return None
        print(f"  {Colors.RED}❌ Error {resp.status_code}: {resp.text[:150]}{Colors.END}")
        return None
    except Exception as e:
        print(f"  {Colors.RED}❌ Error: {e}{Colors.END}")
        return None


# ==================== ENDPOINTS POR ROL ======================================
# Las rutas reflejan los controllers reales:
#   - Venta:   POST /api/tiendas/{id}/ventas          (el id va en la URL)
#   - Ingreso: POST /api/centros-distribucion/{id}/ingresos
#   - CD alias: /api/centros-distribucion y /api/cd
# Los IDs (tienda, CD, producto) se descubren en tiempo de ejecución; aquí sólo
# quedan valores de respaldo por si el descubrimiento falla.
IDS = {"tiendaId": None, "centroDistribucionId": None, "productoId": None}


def descubrir_ids(session, admin_token):
    """Obtiene IDs reales de tienda, CD y producto con el token del admin."""
    if not admin_token:
        print(f"{Colors.YELLOW}  ⚠️  Sin token de admin; usando IDs de respaldo 1.{Colors.END}")
        IDS.update({"tiendaId": 1, "centroDistribucionId": 1, "productoId": 1})
        return

    headers = {"Authorization": f"Bearer {admin_token}"}
    fuentes = [
        ("tiendaId", f"{BASE_URL}/api/tiendas"),
        ("centroDistribucionId", f"{BASE_URL}/api/centros-distribucion"),
        ("productoId", f"{BASE_URL}/api/productos"),
    ]
    for clave, url in fuentes:
        try:
            resp = session.get(url, headers=headers, timeout=TIMEOUT)
            lista = resp.json() if resp.status_code == 200 else []
            IDS[clave] = lista[0]["id"] if lista else 1
            print(f"  {Colors.GREEN}✓{Colors.END} {clave} = {IDS[clave]}  ({url.replace(BASE_URL, '')})")
        except Exception as e:
            IDS[clave] = 1
            print(f"  {Colors.YELLOW}⚠️  No se pudo descubrir {clave} ({e}); usando 1{Colors.END}")
    print()


def construir_endpoints(rol):
    """Arma la lista de (método, url[, body]) para un rol con los IDs actuales."""
    tienda_id = IDS["tiendaId"]
    cd_id = IDS["centroDistribucionId"]
    producto_id = IDS["productoId"]

    comunes_logistica = [
        ("GET", f"{BASE_URL}/api/productos"),
        ("GET", f"{BASE_URL}/api/tiendas"),
        ("GET", f"{BASE_URL}/api/centros-distribucion"),
    ]

    if rol == "ADMINISTRADOR":
        return [
            ("GET", f"{BASE_URL}/api/usuarios"),
            ("GET", f"{BASE_URL}/api/usuarios/me"),
            ("GET", f"{BASE_URL}/api/inventario?tiendaId={tienda_id}"),
            ("GET", f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_id}"),
            *comunes_logistica,
            ("POST", f"{BASE_URL}/api/tiendas/{tienda_id}/ventas", {"productoId": producto_id, "cantidad": 1}),
        ]

    if rol == "PLANIFICADOR":
        return [
            ("GET", f"{BASE_URL}/api/usuarios"),  # 403 esperado: no gestiona usuarios
            ("GET", f"{BASE_URL}/api/inventario?tiendaId={tienda_id}"),
            ("GET", f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_id}"),
            *comunes_logistica,
        ]

    if rol == "ENCARGADO_TIENDA":
        return [
            ("GET", f"{BASE_URL}/api/usuarios"),  # 403 esperado
            ("GET", f"{BASE_URL}/api/inventario?tiendaId={tienda_id}"),
            ("GET", f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_id}"),  # 403 esperado
            ("POST", f"{BASE_URL}/api/tiendas/{tienda_id}/ventas", {"productoId": producto_id, "cantidad": 1}),
        ]

    if rol == "ENCARGADO_CD":
        return [
            ("GET", f"{BASE_URL}/api/usuarios"),  # 403 esperado
            ("GET", f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId={cd_id}"),
            ("GET", f"{BASE_URL}/api/inventario?tiendaId={tienda_id}"),  # 403 esperado
            ("POST", f"{BASE_URL}/api/centros-distribucion/{cd_id}/ingresos",
             {"productoId": producto_id, "cantidad": 10, "origen": "Prueba script"}),
        ]

    return []


def main():
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  PRUEBA DE ENDPOINTS - OptiWay Backend{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"\n{Colors.YELLOW}URL base: {BASE_URL}{Colors.END}")
    print(f"{Colors.YELLOW}Hora: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{Colors.END}\n")

    session = requests.Session()
    tokens = {}

    # PASO 1: tokens
    print_header("PASO 1: OBTENCIÓN DE TOKENS DE AUTENTICACIÓN")
    for nombre_rol, credenciales in USUARIOS.items():
        tokens[nombre_rol] = hacer_login(session, nombre_rol, credenciales)

    # PASO 1.5: descubrir IDs reales con el token del admin, para no depender de
    # IDs fijos que pueden no existir en la base.
    print_header("PASO 1.5: DESCUBRIMIENTO DE IDS REALES")
    descubrir_ids(session, tokens.get("ADMINISTRADOR"))

    # PASO 2: endpoints por rol
    print_header("PASO 2: PRUEBAS DE ENDPOINTS CON AUTENTICACIÓN")
    for rol in USUARIOS:
        token = tokens.get(rol)
        if not token:
            print(f"\n{Colors.YELLOW}[{rol}]{Colors.END}")
            print(f"  {Colors.YELLOW}⚠️  No tiene token, saltando pruebas de este rol{Colors.END}\n")
            continue

        print(f"\n{Colors.BOLD}{Colors.CYAN}--- Probando como {rol} ({USUARIOS[rol]['email']}) ---{Colors.END}")
        for entrada in construir_endpoints(rol):
            metodo, url = entrada[0], entrada[1]
            datos = entrada[2] if len(entrada) > 2 else None
            probar_endpoint(
                session,
                f"{rol} - {metodo} {url.replace(BASE_URL, '')}",
                metodo,
                url,
                datos,
                token,
            )

    # PASO 3: sin token (debe ser 401)
    print_header("PASO 3: ENDPOINTS SIN TOKEN (se espera 401)")
    for metodo, url in [
        ("GET", f"{BASE_URL}/api/usuarios"),
        ("GET", f"{BASE_URL}/api/productos"),
        ("GET", f"{BASE_URL}/api/inventario?tiendaId={IDS['tiendaId']}"),
    ]:
        probar_endpoint(session, f"SIN TOKEN - {metodo} {url.replace(BASE_URL, '')}", metodo, url, None, None, esperado=401)

    # RESUMEN
    print_header("RESUMEN")
    print(f"{Colors.CYAN}Tokens obtenidos:{Colors.END}")
    for rol, token in tokens.items():
        if token:
            print(f"  {Colors.GREEN}✓ {rol}: {token[:20]}...{Colors.END}")
        else:
            print(f"  {Colors.RED}✗ {rol}: No se obtuvo token{Colors.END}")

    print(f"\n{Colors.YELLOW}Nota: si hay errores de conexión, verifica que el servidor esté corriendo en {BASE_URL}{Colors.END}\n")


if __name__ == "__main__":
    main()
