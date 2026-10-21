package com.example.optiway.application.service;

import org.springframework.stereotype.Component;

import com.example.optiway.application.exception.InvitacionAuthException;
import com.example.optiway.infrastructure.adapter.out.auth.SupabaseAuthException;

/**
 * Mapea los fallos técnicos del proveedor de identidad (Supabase Auth) a errores
 * de negocio. La política de "qué mensaje mostrar según el código HTTP" pertenece
 * a la capa de aplicación; el adaptador de infraestructura sólo reporta el fallo
 * crudo. No es una traducción de idioma, sino una conversión de error técnico a
 * error de aplicación.
 */
@Component
public class AuthErrorMapper {

    /**
     * Convierte una {@link SupabaseAuthException} en una
     * {@link InvitacionAuthException} con un mensaje apto para el usuario final.
     */
    public InvitacionAuthException mapear(SupabaseAuthException exception) {
        Integer status = exception.getStatusCode();
        String body = exception.getResponseBody();
        String errorSupabase = (body != null && !body.isBlank()) ? body : exception.getMessage();

        if (status != null && status == 429
                || (body != null && body.contains("over_email_send_rate_limit"))) {
            return new InvitacionAuthException(
                    "Se ha superado el límite de correos de invitación por hora de Supabase Auth. "
                            + "Por favor espera unos minutos antes de registrar otro usuario.",
                    status, errorSupabase);
        }

        if (status != null && status == 422
                || (body != null && (body.contains("email_address_invalid")
                        || body.contains("invalid email")))) {
            return new InvitacionAuthException(
                    "El correo electrónico no tiene un formato válido o fue rechazado por el proveedor de autenticación.",
                    status, errorSupabase);
        }

        return new InvitacionAuthException(
                "No fue posible conectar con el servicio de autenticación. Intenta nuevamente.",
                status, errorSupabase);
    }
}
