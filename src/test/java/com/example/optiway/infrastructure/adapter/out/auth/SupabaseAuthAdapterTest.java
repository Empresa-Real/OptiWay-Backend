package com.example.optiway.infrastructure.adapter.out.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifica la extracción del mensaje natural ({@code msg}) del cuerpo de error
 * de Supabase. El adaptador es el único que conoce el formato del proveedor; la
 * aplicación sólo debe recibir texto legible, nunca el JSON crudo.
 */
class SupabaseAuthAdapterTest {

    @Test
    @DisplayName("Extrae 'msg' del cuerpo de error real de Supabase")
    void extraeMsg() {
        String body = "{\"code\":429,\"error_code\":\"over_email_send_rate_limit\","
                + "\"msg\":\"email rate limit exceeded\"}";

        assertEquals("email rate limit exceeded", SupabaseAuthAdapter.mensajeDelProveedor(body));
    }
}
