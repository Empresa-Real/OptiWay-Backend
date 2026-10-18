# HUS-02 — Protección contra Ataques en el Inicio de Sesión

**Épica 1:** Usuarios y accesos  
**Feature 2:** Seguridad y control de acceso  
**Sprint:** Sprint 2  
**Estado:** `Configurado y Mitigado en Supabase Auth`

---

## Historia de Usuario de Seguridad

> **Como** Administrador  
> **Quiero** implementar un mecanismo de limitación de intentos en el endpoint de autenticación  
> **Para** prevenir ataques automatizados que intenten adivinar las contraseñas de los usuarios (fuerza bruta y credential stuffing).

---

## Criterios de Aceptación

1. **Limitación de intentos y bloqueo temporal (HTTP 429):**  
   Si un usuario o atacante realiza más de un número determinado de intentos fallidos consecutivos de inicio de sesión desde la misma dirección IP o cuenta de correo en un intervalo de tiempo corto (ej. más de 5 a 30 intentos en una ventana de 5 minutos), el sistema bloquea temporalmente el acceso a dicho usuario o dirección IP y retorna un código de estado `HTTP 429 Too Many Requests` junto con un mensaje indicando el tiempo de espera (`retry-after`).
2. **Restablecimiento tras inicio de sesión exitoso:**  
   Si el inicio de sesión se realiza con credenciales correctas dentro de los límites permitidos, el sistema procesa el acceso con éxito, emite el token JWT y restablece el contador de intentos fallidos para esa sesión.
3. **Manejo claro de respuesta en cliente:**  
   La interfaz cliente (Frontend) captura la respuesta `429` e informa al usuario sobre el bloqueo temporal por motivos de seguridad, impidiendo nuevos intentos hasta que venza el temporizador.

---

## Historia de Abuso

> **Como** atacante  
> **Quiero** ejecutar miles de peticiones por segundo contra el endpoint de inicio de sesión probando combinaciones masivas de contraseñas  
> **Para** vulnerar las cuentas de los usuarios legítimos mediante ataques de diccionario o fuerza bruta distribuida.

---

## Mitigación Arquitectónica

Dado que en la arquitectura de **OptiWay** la gestión de credenciales, hashing de contraseñas y emisión de sesiones está delegada al proveedor de identidad **Supabase Auth (GoTrue)**:

```text
Cliente (Frontend / App)
       │
       ▼  POST /auth/v1/token?grant_type=password
┌────────────────────────────────────────────────────────┐
│  SUPABASE AUTH (Módulo de Rate Limiting - HUS-02)      │
│  ¿Número de intentos fallidos <= límite configurado?   │
└────────────────────────────────────────────────────────┘
       │
       ├── NO ──► HTTP 429 Too Many Requests (Bloqueo IP temporal)
       │
       ▼ SÍ
┌────────────────────────────────────────────────────────┐
│  Validación de Credenciales (Bcrypt / Argon2)          │
│  ¿Contraseña correcta?                                 │
└────────────────────────────────────────────────────────┘
       │
       ├── NO ──► HTTP 400 Bad Request (Invalid credentials)
       │
       ▼ SÍ
Retorna Token JWT (Bearer) para consumir el Backend Spring Boot
```

### Medidas Implementadas:
1. **Rate Limiting Nativo por IP y Cuenta:**
   - Configurado en el Dashboard de Supabase en **Authentication** $\rightarrow$ **Rate Limits**.
   - Parámetro: `Rate limit for sign-ups and sign-ins` configurado por IP en ventana de 5 minutos.
   - Parámetro: `Rate limit for token verifications` configurado para mitigar ataques sobre tokens OTP.
2. **Respuesta Estándar HTTP 429:**
   - Supabase rechaza automáticamente cualquier solicitud adicional tras superar el umbral con código `429` y error `over_request_rate_limit`.
3. **Mecanismo de Desbloqueo / Verificación Alternativa:**
   - Supabase permite la recuperación segura mediante enlace de un solo uso o código OTP enviado al correo verificado del usuario afectado, mitigando bloqueos por denegación de servicio.
4. **Protección contra Bots (Opcional):**
   - Soporte para verificación CAPTCHA invisible (Cloudflare Turnstile / hCaptcha) activable en el panel de seguridad de Supabase.
