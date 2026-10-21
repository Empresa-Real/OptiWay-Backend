package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.in.CrearUsuarioCommand;
import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.AuthErrorMapper;
import com.example.optiway.application.exception.InvitacionAuthException;
import com.example.optiway.application.service.UsuarioService;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.adapter.out.auth.SupabaseAuthException;
import com.example.optiway.infrastructure.security.AuthorizationService;

/**
 * Pruebas del comportamiento de {@link UsuarioService} cuando el proveedor de
 * identidad (Supabase Auth) falla. Verifica que los fallos técnicos se traducen
 * a mensajes de negocio y que no se persiste nada inconsistente en la BD local.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceAuthTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private AuthPort authPort;

    private UsuarioService usuarioService;

    private UUID adminAuthId;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        AuthorizationService authorizationService = new AuthorizationService(usuarioRepositoryPort);
        usuarioService = new UsuarioService(usuarioRepositoryPort, authPort, authorizationService,
                new AuthErrorMapper());
        adminAuthId = UUID.randomUUID();
        admin = new Usuario(1L, adminAuthId, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
    }

    @Nested
    @DisplayName("crearUsuario: fallos de invitar en Supabase")
    class CrearUsuarioFallos {

        private CrearUsuarioCommand command() {
            return new CrearUsuarioCommand("Nuevo", "nuevo@optiway.com", Rol.ENCARGADO_TIENDA);
        }

        @Test
        @DisplayName("Caso feliz: invita y guarda en la BD local")
        void casoFeliz_guarda() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.existePorEmail("nuevo@optiway.com")).thenReturn(false);
            UUID nuevoAuthId = UUID.randomUUID();
            when(authPort.invitar("nuevo@optiway.com")).thenReturn(nuevoAuthId);
            when(usuarioRepositoryPort.guardar(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

            usuarioService.crearUsuario(adminAuthId, command());

            verify(usuarioRepositoryPort).guardar(any(Usuario.class));
        }

        @Test
        @DisplayName("Fallo de límite de correos: se traduce a mensaje de negocio y NO se guarda")
        void rateLimit_traducidoYNoGuarda() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.existePorEmail("nuevo@optiway.com")).thenReturn(false);
            when(authPort.invitar("nuevo@optiway.com"))
                    .thenThrow(new SupabaseAuthException("tecnico", 429, "{}"));

            InvitacionAuthException e = assertThrows(InvitacionAuthException.class,
                    () -> usuarioService.crearUsuario(adminAuthId, command()));

            assertTrue(e.getMessage().contains("límite de correos"));
            verify(usuarioRepositoryPort, never()).guardar(any(Usuario.class));
        }

        @Test
        @DisplayName("Fallo genérico del proveedor: se traduce y NO se guarda")
        void falloGenerico_traducidoYNoGuarda() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.existePorEmail("nuevo@optiway.com")).thenReturn(false);
            when(authPort.invitar("nuevo@optiway.com"))
                    .thenThrow(new SupabaseAuthException("tecnico", 500, "internal error"));

            InvitacionAuthException e = assertThrows(InvitacionAuthException.class,
                    () -> usuarioService.crearUsuario(adminAuthId, command()));

            assertTrue(e.getMessage().contains("servicio de autenticación"));
            verify(usuarioRepositoryPort, never()).guardar(any(Usuario.class));
        }

        @Test
        @DisplayName("Correo duplicado en la BD local no llega a invitar")
        void correoDuplicado_noInvita() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.existePorEmail("nuevo@optiway.com")).thenReturn(true);

            assertThrows(RuntimeException.class, () -> usuarioService.crearUsuario(adminAuthId, command()));
            verify(authPort, never()).invitar(any());
        }
    }

    @Nested
    @DisplayName("eliminarUsuario: fallos al eliminar en Supabase")
    class EliminarUsuarioFallos {

        private Usuario objetivo() {
            return new Usuario(2L, UUID.randomUUID(), "Objetivo", "obj@optiway.com",
                    Rol.ENCARGADO_TIENDA, adminAuthId);
        }

        @Test
        @DisplayName("Si Supabase falla al eliminar, se traduce el error y NO se borra de la BD local")
        void falloSupabase_traducidoYNoBorraLocal() {
            Usuario objetivo = objetivo();
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(objetivo));
            org.mockito.Mockito.doThrow(new SupabaseAuthException("tecnico", 500, "boom"))
                    .when(authPort).eliminarUsuarioAuth(objetivo.getAuthUserId());

            InvitacionAuthException e = assertThrows(InvitacionAuthException.class,
                    () -> usuarioService.eliminarUsuario(adminAuthId, 2L));

            assertTrue(e.getMessage().contains("servicio de autenticación"));
            verify(usuarioRepositoryPort, never()).eliminar(any());
        }

        @Test
        @DisplayName("Usuario sin authUserId: no llama a Supabase y borra solo en la BD local")
        void sinAuthUserId_noLlamaSupabase() {
            Usuario objetivo = new Usuario(3L, null, "SinAuth", "sin@optiway.com", Rol.PLANIFICADOR, adminAuthId);
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.obtenerPorId(3L)).thenReturn(Optional.of(objetivo));

            usuarioService.eliminarUsuario(adminAuthId, 3L);

            verify(authPort, never()).eliminarUsuarioAuth(any());
            verify(usuarioRepositoryPort).eliminar(3L);
        }

        @Test
        @DisplayName("Usuario inexistente lanza IllegalArgumentException y no toca Supabase")
        void usuarioInexistente_lanza() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(admin));
            when(usuarioRepositoryPort.obtenerPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> usuarioService.eliminarUsuario(adminAuthId, 99L));
            assertEquals("Usuario no encontrado con ID: 99", e.getMessage());
            verify(authPort, never()).eliminarUsuarioAuth(any());
            verify(usuarioRepositoryPort, never()).eliminar(any());
        }
    }
}
