package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.optiway.infrastructure.security.AuthUserId;
import com.example.optiway.infrastructure.security.AuthUserIdArgumentResolver;
import com.example.optiway.infrastructure.security.AutenticacionRequeridaException;

/**
 * Contrato estricto del {@link AuthUserIdArgumentResolver}: un endpoint que
 * declare {@code @AuthUserId} exige identidad y falla con 401 en lugar de
 * entregar {@code null} silenciosamente.
 */
class AuthUserIdArgumentResolverTest {

    private final AuthUserIdArgumentResolver resolver = new AuthUserIdArgumentResolver();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    /** Portador del parámetro {@code @AuthUserId} para construir un MethodParameter real. */
    @SuppressWarnings("unused")
    private void endpointDePrueba(@AuthUserId UUID authUserId) {
    }

    private MethodParameter parametro() throws NoSuchMethodException {
        return new MethodParameter(
                AuthUserIdArgumentResolverTest.class.getDeclaredMethod("endpointDePrueba", UUID.class), 0);
    }

    private void autenticarComo(Object principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    @Test
    @DisplayName("Sin autenticación lanza 401 en vez de devolver null")
    void sinAutenticacion_lanza() throws Exception {
        SecurityContextHolder.clearContext();
        assertThrows(AutenticacionRequeridaException.class,
                () -> resolver.resolveArgument(parametro(), null, null, null));
    }

    @Test
    @DisplayName("Autenticación anónima lanza 401")
    void autenticacionAnonima_lanza() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymous",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        assertThrows(AutenticacionRequeridaException.class,
                () -> resolver.resolveArgument(parametro(), null, null, null));
    }

    @Test
    @DisplayName("Principal que no es JWT lanza 401")
    void principalNoJwt_lanza() throws Exception {
        autenticarComo("no-soy-un-jwt");
        assertThrows(AutenticacionRequeridaException.class,
                () -> resolver.resolveArgument(parametro(), null, null, null));
    }

    @Test
    @DisplayName("JWT sin claim 'sub' lanza 401")
    void jwtSinSub_lanza() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("scope", "api").build();
        autenticarComo(jwt);
        assertThrows(AutenticacionRequeridaException.class,
                () -> resolver.resolveArgument(parametro(), null, null, null));
    }

    @Test
    @DisplayName("JWT con 'sub' que no es UUID lanza 401")
    void jwtConSubInvalido_lanza() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("no-es-un-uuid").build();
        autenticarComo(jwt);
        assertThrows(AutenticacionRequeridaException.class,
                () -> resolver.resolveArgument(parametro(), null, null, null));
    }

    @Test
    @DisplayName("JWT con 'sub' UUID válido resuelve la identidad")
    void jwtValido_resuelveUuid() throws Exception {
        UUID authUserId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(authUserId.toString()).build();
        autenticarComo(jwt);
        assertEquals(authUserId, resolver.resolveArgument(parametro(), null, null, null));
    }
}
