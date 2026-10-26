package com.example.optiway.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pruebas unitarias para los manejadores de rechazo de autenticación y autorización
 * en SecurityConfig, asegurando que emitan siempre un JSON ErrorResponse y el código HTTP correcto.
 */
class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("AuthenticationEntryPoint devuelve 401 y un JSON ErrorResponse estándar")
    void authenticationEntryPoint_devuelve401YErrorResponseJson() throws Exception {
        AuthenticationEntryPoint entryPoint = securityConfig.authenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Token inválido o expirado"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"));

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertTrue(errorResponse.message().contains("Autenticación requerida"));
    }

    @Test
    @DisplayName("AccessDeniedHandler devuelve 403 y un JSON ErrorResponse estándar")
    void accessDeniedHandler_devuelve403YErrorResponseJson() throws Exception {
        AccessDeniedHandler deniedHandler = securityConfig.accessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        deniedHandler.handle(request, response, new AccessDeniedException("Permisos insuficientes"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().contains("application/json"));

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertTrue(errorResponse.message().contains("permisos suficientes"));
    }
}
