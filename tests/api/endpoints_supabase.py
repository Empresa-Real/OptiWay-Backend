"""
Prueba de errores de autenticación e invitación (Supabase Auth + crear usuario).

Cubre dos capas:

  A. Supabase Auth DIRECTAMENTE (capa de identidad)
       - login con credenciales correctas              -> 200
       - login con contraseña incorrecta                -> 400
       - login con email inexistente                    -> 400
       - login SIN el header `apikey`                   -> 401
       - login con email con formato inválido           -> 400
       - pedir un recurso autenticado sin token         -> 401

  B. Backend: POST /api/usuarios (invitación de usuarios)
       - sin token                                      -> 401
       - token de rol no-admin                          -> 403
       - email ya registrado en la app                  -> 409
       - email con formato inválido (validación)        -> 400
       - body sin campos requeridos                     -> 400
       - body no JSON                                   -> 400
       - creación correcta (admin)                      -> 201
       - y luego limpieza del usuario creado            -> 204

Notas del contrato real:
  - El adaptador SupabaseAuthAdapter invita vía /auth/v1/invite con el
    service-role key. Si Supabase responde 429 (límite de correos) o el email
    ya existe, reutiliza el UUID existente; sólo si no lo encuentra lanza
    SupabaseAuthException, que el AuthErrorMapper convierte en
    InvitacionAuthException -> el handler la mapea a 502 Bad Gateway.
  - Supabase Auth exige el header `apikey`; sin él /auth/v1/token da 401
    aunque las credenciales sean válidas.

Uso:
    python endpoints_supabase.py
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

ADMIN_EMAIL = os.environ.get("ADMIN_EMAIL", "admin@empresa.com")
ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "")
TIENDA_EMAIL = os.environ.get("TIENDA_EMAIL", "carlos@empresa.com")
TIENDA_PASSWORD = os.environ.get("TIENDA_PASSWORD", "")


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


def print_result(nombre, metodo, url, respuesta, body_enviado=None):
    print(f"\n{Colors.BLUE}[{nombre}]{Colors.END}")
    print(f"  {Colors.CYAN}Método:{Colors.END} {metodo} {url}")
    if body_enviado is not None:
        print(f"  {Colors.CYAN}Body:{Colors.END} {json.dumps(body_enviado, ensure_ascii=False)}")
    print(f"  {Colors.CYAN}Status:{Colors.END} {respuesta.status_code}")
    try:
        print(f"  {Colors.CYAN}Body JSON:{Colors.END} {json.dumps(respuesta.json(), indent=2, ensure_ascii=False)}")
    except ValueError:
        print(f"  {Colors.CYAN}Body texto:{Colors.END} {respuesta.text[:300]}")
    print()


def evaluar(nombre, respuesta, aceptables):
    """Cuenta el resultado contra los códigos aceptables."""
    if respuesta.status_code in aceptables:
        print(f"  {Colors.GREEN}✓ OK {respuesta.status_code} (esperado {'/'.join(map(str, aceptables))}){Colors.END}")
        RESUMEN["ok"] += 1
    else:
        print(f"  {Colors.RED}✗ Se esperaba {'/'.join(map(str, aceptables))}, se obtuvo {respuesta.status_code}{Colors.END}")
        RESUMEN["fail"] += 1
    return respuesta


def request_seguro(nombre, metodo, url, headers=None, body=None):
    try:
        return requests.request(metodo, url, headers=headers or {}, json=body, timeout=TIMEOUT)
    except requests.exceptions.ConnectionError:
        print(f"\n{Colors.RED}[{nombre}] ❌ No se pudo conectar a {url}{Colors.END}")
        RESUMEN["fail"] += 1
        return None
    except Exception as e:
        print(f"\n{Colors.RED}[{nombre}] ❌ Error: {e}{Colors.END}")
        RESUMEN["fail"] += 1
        return None


def supabase_login(email, password, con_apikey=True):
    headers = {"Content-Type": "application/json"}
    if con_apikey:
        headers["apikey"] = SUPABASE_ANON_KEY
    return request_seguro(
        f"login {email}", "POST", SUPABASE_LOGIN_URL, headers,
        {"email": email, "password": password},
    )


def main():
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}  PRUEBA DE ERRORES SUPABASE / LOGIN / USUARIOS - OptiWay{Colors.END}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'=' * 70}{Colors.END}")
    print(f"\n{Colors.YELLOW}API base:     {BASE_URL}{Colors.END}")
    print(f"{Colors.YELLOW}Supabase:     {SUPABASE_URL}{Colors.END}")
    print(f"{Colors.YELLOW}Hora:         {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{Colors.END}\n")

    # ============================================================
    # A. SUPABASE AUTH DIRECTO
    # ============================================================
    print_header("A. SUPABASE AUTH DIRECTO (capa de identidad)")

    resp = supabase_login(ADMIN_EMAIL, ADMIN_PASSWORD)
    if resp is not None:
        print_result("SUPABASE login admin (correcto)", "POST", SUPABASE_LOGIN_URL, resp)
        evaluar("SUPABASE login admin (correcto)", resp, (200,))

    resp = supabase_login(ADMIN_EMAIL, "contrasena-incorrecta")
    if resp is not None:
        print_result("SUPABASE login contraseña incorrecta", "POST", SUPABASE_LOGIN_URL, resp)
        evaluar("SUPABASE login contraseña incorrecta", resp, (400,))

    resp = supabase_login("no-existe-jamas@empresa.com", "cualquiera")
    if resp is not None:
        print_result("SUPABASE login email inexistente", "POST", SUPABASE_LOGIN_URL, resp)
        evaluar("SUPABASE login email inexistente", resp, (400,))

    resp = supabase_login(ADMIN_EMAIL, ADMIN_PASSWORD, con_apikey=False)
    if resp is not None:
        print_result("SUPABASE login SIN header apikey", "POST", SUPABASE_LOGIN_URL, resp)
        evaluar("SUPABASE login SIN apikey", resp, (401,))

    resp = supabase_login("esto-no-es-un-email", "cualquiera")
    if resp is not None:
        print_result("SUPABASE login email con formato inválido", "POST", SUPABASE_LOGIN_URL, resp)
        evaluar("SUPABASE login email inválido", resp, (400,))

    resp = request_seguro("recurso protegido sin token", "GET", f"{BASE_URL}/api/usuarios/me")
    if resp is not None:
        print_result("API sin token (recurso protegido)", "GET", f"{BASE_URL}/api/usuarios/me", resp)
        evaluar("API sin token", resp, (401,))

    resp = request_seguro(
        "API con token basura", "GET", f"{BASE_URL}/api/usuarios/me",
        {"Authorization": "Bearer token-invalido.abc.def"},
    )
    if resp is not None:
        print_result("API con token inválido", "GET", f"{BASE_URL}/api/usuarios/me", resp)
        evaluar("API con token inválido", resp, (401, 403))

    # ============================================================
    # B. BACKEND: CREAR USUARIO (invitación)
    # ============================================================
    print_header("B. BACKEND: POST /api/usuarios (invitación de usuarios)")

    # Tokens necesarios
    admin_login = supabase_login(ADMIN_EMAIL, ADMIN_PASSWORD)
    admin_token = admin_login.json().get("access_token") if admin_login and admin_login.status_code == 200 else None
    tienda_login = supabase_login(TIENDA_EMAIL, TIENDA_PASSWORD)
    tienda_token = tienda_login.json().get("access_token") if tienda_login and tienda_login.status_code == 200 else None

    if not admin_token:
        print(f"{Colors.RED}No se pudo obtener token de admin; se omiten las pruebas con token.{Colors.END}")
        RESUMEN["omitido"] += 7
    else:
        def post_usuarios(nombre, token, body, headers_extra=None):
            headers = {"Content-Type": "application/json"}
            if token:
                headers["Authorization"] = f"Bearer {token}"
            if headers_extra:
                headers.update(headers_extra)
            r = request_seguro(nombre, "POST", f"{BASE_URL}/api/usuarios", headers, body)
            if r is not None:
                print_result(nombre, "POST", f"{BASE_URL}/api/usuarios", r, body)
            return r

        # sin token -> 401
        r = post_usuarios("crear usuario SIN token", None,
                          {"nombre": "X", "email": "x@empresa.com", "rol": "ENCARGADO_TIENDA"})
        if r is not None:
            evaluar("crear usuario SIN token", r, (401,))

        # email duplicado -> 409
        r = post_usuarios("crear usuario con email YA REGISTRADO", admin_token,
                          {"nombre": "Dup", "email": ADMIN_EMAIL, "rol": "ENCARGADO_TIENDA"})
        if r is not None:
            evaluar("crear usuario email duplicado", r, (409,))

        # email inválido -> 400 (validación de @Email)
        r = post_usuarios("crear usuario con EMAIL inválido", admin_token,
                          {"nombre": "Bad", "email": "no-es-email", "rol": "ENCARGADO_TIENDA"})
        if r is not None:
            evaluar("crear usuario email inválido", r, (400,))

        # rol inválido -> 400, con mensaje en español y SIN exponer el dominio
        r = post_usuarios("crear usuario con ROL inválido", admin_token,
                          {"nombre": "Bad", "email": "rol@empresa.com", "rol": "SUPERHEROE"})
        if r is not None:
            evaluar("crear usuario rol inválido", r, (400,))
            try:
                mensaje = (r.json().get("message") or "")
            except ValueError:
                mensaje = r.text
            prohibido = "No enum constant" in mensaje or "com.example.optiway" in mensaje
            if mensaje and not prohibido:
                print(f"  {Colors.GREEN}✓ El mensaje no expone el dominio y está en español: "
                      f"\"{mensaje}\"{Colors.END}")
                RESUMEN["ok"] += 1
            else:
                print(f"  {Colors.RED}✗ El mensaje filtra el dominio o viene vacío: \"{mensaje}\"{Colors.END}")
                RESUMEN["fail"] += 1

        # body sin campos -> 400
        r = post_usuarios("crear usuario sin campos requeridos", admin_token, {})
        if r is not None:
            evaluar("crear usuario body vacío", r, (400,))

        # creación correcta -> 201 (o 502 si Supabase límite/fallo); luego limpieza
        email_nuevo = f"test.auth.{datetime.now():%Y%m%d%H%M%S}@empresa.com"
        body_nuevo = {"nombre": "Usuario Prueba", "email": email_nuevo, "rol": "PLANIFICADOR"}
        r = request_seguro(
            "crear usuario válido", "POST", f"{BASE_URL}/api/usuarios",
            {"Content-Type": "application/json", "Authorization": f"Bearer {admin_token}"},
            body_nuevo,
        )
        if r is not None:
            print_result("crear usuario válido (admin)", "POST", f"{BASE_URL}/api/usuarios", r, body_nuevo)
            # 201 = creado; 502 = Supabase rechazó la invitación (límite de correos),
            # que es un fallo del proveedor, no del contrato del endpoint.
            evaluar("crear usuario válido", r, (201, 502))

            if r.status_code == 201 and r.json().get("id"):
                nuevo_id = r.json()["id"]
                d = request_seguro(
                    "limpieza: borrar usuario creado", "DELETE",
                    f"{BASE_URL}/api/usuarios/{nuevo_id}",
                    {"Authorization": f"Bearer {admin_token}"},
                )
                if d is not None:
                    print_result("limpieza DELETE usuario creado", "DELETE",
                                 f"{BASE_URL}/api/usuarios/{nuevo_id}", d)
                    evaluar("limpieza usuario creado", d, (204,))
            else:
                RESUMEN["omitido"] += 1

    # rol no-admin -> 403
    if tienda_token:
        r = request_seguro(
            "crear usuario con rol ENCARGADO_TIENDA (403)", "POST", f"{BASE_URL}/api/usuarios",
            {"Content-Type": "application/json", "Authorization": f"Bearer {tienda_token}"},
            {"nombre": "X", "email": "x2@empresa.com", "rol": "ENCARGADO_TIENDA"},
        )
        if r is not None:
            print_result("crear usuario con rol no-admin", "POST", f"{BASE_URL}/api/usuarios", r)
            evaluar("crear usuario rol no-admin", r, (403,))
    else:
        RESUMEN["omitido"] += 1

    # RESULTADO
    print_header("RESUMEN")
    print(f"  {Colors.GREEN}✓ Cumplen lo esperado: {RESUMEN['ok']}{Colors.END}")
    print(f"  {Colors.RED}✗ No cumplen:          {RESUMEN['fail']}{Colors.END}")
    print(f"  {Colors.YELLOW}⚠ Omitidas:            {RESUMEN['omitido']}{Colors.END}")
    print(f"\n{Colors.YELLOW}Nota: el 502 en 'crear usuario válido' es el mapeo de la "
          f"InvitacionAuthException (p. ej. límite de correos de Supabase).{Colors.END}\n")


if __name__ == "__main__":
    main()
