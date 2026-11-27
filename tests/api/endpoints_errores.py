"""
Prueba de códigos de error de OptiWay Backend.

Verifica el comportamiento de la API ante:
  - peticiones sin autenticación      -> 401
  - POST sin cuerpo o con datos inválidos (con token) -> 400
  - IDs inexistentes (con token)      -> 400/404
  - rutas inexistentes                -> 404

Notas del contrato real del backend:
  - SecurityConfig usa `anyRequest().permitAll()`: la seguridad HTTP no bloquea.
    El 401 lo produce el resolver estricto de `@AuthUserId` cuando falta el JWT.
  - Por eso, sin token TODO endpoint con @AuthUserId responde 401 (aunque el
    cuerpo sea inválido o falte): la autenticación se resuelve antes de validar.
  - No existen rutas como GET /api/ventas, POST /api/ventas ni
    POST /api/ingresos-mercancia. Las reales son:
        POST /api/tiendas/{id}/ventas
        POST /api/centros-distribucion/{id}/ingresos   (alias /api/cd/{id}/ingresos)
  - No existen GET /api/productos/{id}, GET /api/tiendas/{id},
    GET /api/centros-distribucion/{id}: sólo sub-rutas.

Uso:
    python endpoints_errores.py
"""

import json
import os
import sys
from datetime import datetime

import requests

# La consola de Windows usa cp1252 y rompe con emojis/acentos.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# ==================== CONFIGURACIÓN ==========================================
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
SUPABASE_URL = os.environ.get("SUPABASE_URL", "https://cehzzcvnkiuvkabglrzp.supabase.co")
# Supabase Auth exige el header `apikey`; sin él responde 401 aunque las
# credenciales sean válidas.
SUPABASE_ANON_KEY = os.environ.get(
    "SUPABASE_ANON_KEY", ""
)
SUPABASE_LOGIN_URL = f"{SUPABASE_URL}/auth/v1/token?grant_type=password"
TIMEOUT = 10

ADMIN_EMAIL = os.environ.get("ADMIN_EMAIL", "admin@empresa.com")
ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "")

# IDs usados para las pruebas con token.
ID_INEXISTENTE = 99999
TIENDA_ID = 1
CENTRO_DISTRIBUCION_ID = 1
PRODUCTO_ID = 1


class Colors:
    GREEN = "\033[92m"
    RED = "\033[91m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    CYAN = "\033[96m"
    BOLD = "\033[1m"
    END = "\033[0m"


# Contador global de asertciones
RESUMEN = {"ok": 0, "fail": 0, "omitido": 0}


def print_header(texto):
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  {texto}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}\n")


def print_result(nombre, metodo, url, datos_envio, respuesta):
    print(f"\n{Colors.BLUE}[{nombre}]{Colors.END}")
    print(f"  {Colors.CYAN}Método:{Colors.END} {metodo} {url}")
    if datos_envio is not None:
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
        resp = session.request(metodo, url, json=datos, headers=headers, timeout=TIMEOUT)
    except requests.exceptions.ConnectionError:
        print(f"\n{Colors.RED}[{nombre}]{Colors.END}")
        print(f"  {Colors.RED}❌ No se pudo conectar al servidor en {BASE_URL}{Colors.END}")
        print(f"  {Colors.YELLOW}¿Está corriendo el servidor?{Colors.END}\n")
        RESUMEN["fail"] += 1
        return None
    except Exception as e:
        print(f"\n{Colors.RED}[{nombre}]{Colors.END}")
        print(f"  {Colors.RED}❌ Error: {e}{Colors.END}\n")
        RESUMEN["fail"] += 1
        return None

    print_result(nombre, metodo, url, datos, resp)

    if esperado is None:
        return resp

    if resp.status_code == esperado:
        print(f"  {Colors.GREEN}✓ OK {resp.status_code} (esperado){Colors.END}")
        RESUMEN["ok"] += 1
    else:
        print(f"  {Colors.RED}✗ Se esperaba {esperado}, se obtuvo {resp.status_code}{Colors.END}")
        RESUMEN["fail"] += 1
    return resp


def obtener_token_admin(session):
    """Login del admin contra Supabase para las pruebas que requieren token."""
    resp = session.post(
        SUPABASE_LOGIN_URL,
        json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
        headers={"apikey": SUPABASE_ANON_KEY, "Content-Type": "application/json"},
        timeout=TIMEOUT,
    )
    if resp.status_code == 200:
        return resp.json().get("access_token")
    print(f"{Colors.YELLOW}  ⚠️  No se pudo autenticar el admin ({resp.status_code}). "
          f"Las pruebas con token se omitirán.{Colors.END}")
    return None


def main():
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  PRUEBA DE CÓDIGOS DE ERROR - OptiWay Backend{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"\n{Colors.YELLOW}URL base: {BASE_URL}{Colors.END}")
    print(f"{Colors.YELLOW}Hora: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{Colors.END}\n")

    session = requests.Session()

    # ============================================================
    # 1. SIN AUTENTICACIÓN -> 401
    # ============================================================
    print_header("1. SIN AUTENTICACIÓN (se espera 401)")
    endpoints_sin_token = [
        ("GET /api/productos", "GET", f"{BASE_URL}/api/productos"),
        ("GET /api/inventario?tiendaId=1", "GET", f"{BASE_URL}/api/inventario?tiendaId=1"),
        ("GET /api/inventario/centro-distribucion", "GET",
         f"{BASE_URL}/api/inventario/centro-distribucion?centroDistribucionId=1"),
        ("GET /api/tiendas", "GET", f"{BASE_URL}/api/tiendas"),
        ("GET /api/usuarios", "GET", f"{BASE_URL}/api/usuarios"),
        ("GET /api/centros-distribucion", "GET", f"{BASE_URL}/api/centros-distribucion"),
        ("POST /api/productos", "POST", f"{BASE_URL}/api/productos"),
        ("POST /api/tiendas", "POST", f"{BASE_URL}/api/tiendas"),
        ("POST /api/usuarios", "POST", f"{BASE_URL}/api/usuarios"),
        ("POST /api/tiendas/1/ventas", "POST", f"{BASE_URL}/api/tiendas/{TIENDA_ID}/ventas"),
        ("POST /api/centros-distribucion/1/ingresos", "POST",
         f"{BASE_URL}/api/centros-distribucion/{CENTRO_DISTRIBUCION_ID}/ingresos"),
    ]
    for nombre, metodo, url in endpoints_sin_token:
        probar_endpoint(session, f"SIN TOKEN - {nombre}", metodo, url, None, None, esperado=401)

    # ============================================================
    # 2. RUTAS INEXISTENTES -> 404
    # ============================================================
    print_header("2. RUTAS INEXISTENTES (se espera 404)")
    rutas_inexistentes = [
        ("GET /api/ventas", "GET", f"{BASE_URL}/api/ventas"),
        ("GET /api/endpoint-inexistente", "GET", f"{BASE_URL}/api/endpoint-inexistente"),
        ("POST /api/ruta-que-no-existe", "POST", f"{BASE_URL}/api/ruta-que-no-existe"),
        ("GET /api/ingresos-mercancia", "GET", f"{BASE_URL}/api/ingresos-mercancia"),
        # No existe GET /api/productos/{id}, pero SÍ existe PATCH /api/productos/{id};
        # Spring ve el path mapeado con otro método -> 405 (no 404).
        ("GET /api/productos/99999 (solo PATCH existe)", "GET", f"{BASE_URL}/api/productos/{ID_INEXISTENTE}"),
        ("GET /api/tiendas/99999", "GET", f"{BASE_URL}/api/tiendas/{ID_INEXISTENTE}"),
        ("GET /api/centros-distribucion/99999", "GET",
         f"{BASE_URL}/api/centros-distribucion/{ID_INEXISTENTE}"),
    ]
    for nombre, metodo, url in rutas_inexistentes:
        esperado = 405 if "solo PATCH existe" in nombre else 404
        probar_endpoint(session, nombre, metodo, url, None, None, esperado=esperado)

    # ============================================================
    # 3. CON TOKEN: CUERPOS INVÁLIDOS -> 400
    # ============================================================
    print_header("3. CON TOKEN ADMIN - DATOS INVÁLIDOS (se espera 400)")
    token = obtener_token_admin(session)
    if not token:
        RESUMEN["omitido"] += 8
    else:
        probar_endpoint(
            session, "POST /api/productos (sin body)", "POST",
            f"{BASE_URL}/api/productos", None, token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/productos (datos inválidos)", "POST",
            f"{BASE_URL}/api/productos",
            {"identificador": "", "nombre": "", "categoria": "", "precio": -10, "unidadMedida": ""},
            token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/tiendas (datos inválidos)", "POST",
            f"{BASE_URL}/api/tiendas",
            {"nombre": "", "direccion": "", "ciudad": ""}, token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/centros-distribucion (sin body)", "POST",
            f"{BASE_URL}/api/centros-distribucion", None, token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/usuarios (datos inválidos)", "POST",
            f"{BASE_URL}/api/usuarios",
            {"nombre": "", "email": "no-es-email", "rol": ""}, token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/tiendas/1/ventas (datos inválidos)", "POST",
            f"{BASE_URL}/api/tiendas/{TIENDA_ID}/ventas",
            {"productoId": None, "cantidad": -5}, token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/centros-distribucion/1/ingresos (datos inválidos)", "POST",
            f"{BASE_URL}/api/centros-distribucion/{CENTRO_DISTRIBUCION_ID}/ingresos",
            {"productoId": None, "cantidad": 0, "origen": ""}, token, esperado=400,
        )
        probar_endpoint(
            session, "POST /api/productos (body no JSON)", "POST",
            f"{BASE_URL}/api/productos", None, token, esperado=400,
        )

    # ============================================================
    # 4. CON TOKEN: RECURSOS INEXISTENTES -> 400 (mapping actual)
    # ============================================================
    print_header("4. CON TOKEN ADMIN - RECURSOS INEXISTENTES (se espera 400)")
    if not token:
        RESUMEN["omitido"] += 2
    else:
        # obtenerUsuarioPorId lanza IllegalArgumentException -> 400 (no 404)
        probar_endpoint(
            session, f"GET /api/usuarios/{ID_INEXISTENTE}", "GET",
            f"{BASE_URL}/api/usuarios/{ID_INEXISTENTE}", None, token, esperado=400,
        )
        # PATCH /{id}/estado con id inexistente -> 400
        probar_endpoint(
            session, f"PATCH /api/productos/{ID_INEXISTENTE}/estado", "PATCH",
            f"{BASE_URL}/api/productos/{ID_INEXISTENTE}/estado", {"activo": True}, token, esperado=400,
        )

    # ============================================================
    # RESUMEN
    # ============================================================
    print_header("RESUMEN")
    print(f"  {Colors.GREEN}✓ Cumplen lo esperado: {RESUMEN['ok']}{Colors.END}")
    print(f"  {Colors.RED}✗ No cumplen:          {RESUMEN['fail']}{Colors.END}")
    print(f"  {Colors.YELLOW}⚠ Omitidas (sin token): {RESUMEN['omitido']}{Colors.END}")
    print(f"\n{Colors.YELLOW}Nota: si hay errores de conexión, verifica que el servidor esté corriendo en {BASE_URL}{Colors.END}\n")


if __name__ == "__main__":
    main()
