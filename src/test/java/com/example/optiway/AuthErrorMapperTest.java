package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.optiway.application.service.AuthErrorMapper;
import com.example.optiway.application.exception.InvitacionAuthException;
import com.example.optiway.infrastructure.adapter.out.auth.SupabaseAuthException;

/**
 * Pruebas del mapeo de errores técnicos de Supabase Auth a mensajes de negocio.
 * La política de qué mensaje se muestra según el código HTTP o el cuerpo de la
 * respuesta vive en la capa de aplicación, así que se fija aquí.
 */
class AuthErrorMapperTest {

    private AuthErrorMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuthErrorMapper();
    }

    @Test
    @DisplayName("HTTP 429 se mapea a mensaje de límite de correos")
    void status429_mensajeDeLimite() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", 429, "{}"));
        assertTrue(resultado.getMessage().contains("límite de correos"));
        assertEquals(429, resultado.getStatusCode());
    }

    @Test
    @DisplayName("Cuerpo con over_email_send_rate_limit se mapea a límite aunque el status no sea 429")
    void cuerpoRateLimit_mensajeDeLimite() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", 400, "{\"error_code\":\"over_email_send_rate_limit\"}"));
        assertTrue(resultado.getMessage().contains("límite de correos"));
    }

    @Test
    @DisplayName("HTTP 422 se mapea a correo inválido")
    void status422_mensajeDeCorreoInvalido() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", 422, "{}"));
        assertTrue(resultado.getMessage().contains("correo electrónico"));
        assertEquals(422, resultado.getStatusCode());
    }

    @Test
    @DisplayName("Cuerpo con email_address_invalid se mapea a correo inválido")
    void cuerpoEmailInvalido_mensajeDeCorreoInvalido() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", 400, "{\"error_code\":\"email_address_invalid\"}"));
        assertTrue(resultado.getMessage().contains("correo electrónico"));
    }

    @Test
    @DisplayName("Cuerpo con 'invalid email' se mapea a correo inválido")
    void cuerpoInvalidEmail_mensajeDeCorreoInvalido() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", 500, "invalid email"));
        assertTrue(resultado.getMessage().contains("correo electrónico"));
    }

    @Test
    @DisplayName("Un error genérico se mapea a mensaje de conexión")
    void generico_mensajeDeConexion() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", 500, "internal error"));
        assertTrue(resultado.getMessage().contains("servicio de autenticación"));
        assertEquals(500, resultado.getStatusCode());
    }

    @Test
    @DisplayName("Un error sin status ni cuerpo se mapea a mensaje de conexión sin status")
    void sinDatos_mensajeDeConexion() {
        InvitacionAuthException resultado = mapper.mapear(
                new SupabaseAuthException("tecnico", null, null));
        assertTrue(resultado.getMessage().contains("servicio de autenticación"));
        assertNull(resultado.getStatusCode());
    }
}
