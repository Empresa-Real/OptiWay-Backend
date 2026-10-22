package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.example.optiway.application.port.in.ActualizarRolCommand;
import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.service.AuthErrorMapper;
import com.example.optiway.application.service.UsuarioService;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private AuthPort authPort;

    private UsuarioService usuarioService;

    private UUID adminAuthId;
    private Usuario adminUsuario;

    @BeforeEach
    void setUp() {
        AuthorizationService authService = new AuthorizationService(usuarioRepositoryPort);
        usuarioService = new UsuarioService(usuarioRepositoryPort, authPort, authService,
                new AuthErrorMapper());
        adminAuthId = UUID.randomUUID();
        adminUsuario = new Usuario(1L, adminAuthId, "Admin User", "admin@optiway.com", Rol.ADMINISTRADOR, null);
    }

    @Nested
    @DisplayName("HU-03: Pruebas de Eliminación de Usuarios (eliminarUsuario)")
    class EliminarUsuarioTests {

        @Test
        @DisplayName("Eliminar usuario exitosamente cuando el solicitante es Administrador")
        void eliminarUsuario_exitoso() {
            Long usuarioId = 2L;
            UUID targetAuthId = UUID.randomUUID();
            Usuario usuarioAEliminar = new Usuario(usuarioId, targetAuthId, "Encargado", "encargado@optiway.com", Rol.ENCARGADO_TIENDA, adminAuthId);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(usuarioId)).thenReturn(Optional.of(usuarioAEliminar));

            usuarioService.eliminarUsuario(adminAuthId, usuarioId);

            // Debe eliminar en Supabase Auth y en el repositorio local
            verify(authPort, times(1)).eliminarUsuarioAuth(targetAuthId);
            verify(usuarioRepositoryPort, times(1)).eliminar(usuarioId);
        }

        @Test
        @DisplayName("Eliminar usuario sin authUserId en Supabase (solo BD local)")
        void eliminarUsuario_sinAuthUserId_eliminaSoloEnBdLocal() {
            Long usuarioId = 3L;
            Usuario usuarioSinAuth = new Usuario(usuarioId, null, "Invitado Pendiente", "invitado@optiway.com", Rol.PLANIFICADOR, adminAuthId);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(usuarioId)).thenReturn(Optional.of(usuarioSinAuth));

            usuarioService.eliminarUsuario(adminAuthId, usuarioId);

            verify(authPort, never()).eliminarUsuarioAuth(any());
            verify(usuarioRepositoryPort, times(1)).eliminar(usuarioId);
        }

        @Test
        @DisplayName("Rechazar eliminación si el solicitante no es Administrador (HTTP 403)")
        void eliminarUsuario_solicitanteNoEsAdmin_lanzaAccesoDenegado() {
            UUID planificadorAuthId = UUID.randomUUID();
            Usuario planificador = new Usuario(5L, planificadorAuthId, "Planificador", "plan@optiway.com", Rol.PLANIFICADOR, null);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(planificadorAuthId)).thenReturn(Optional.of(planificador));

            AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.eliminarUsuario(planificadorAuthId, 2L)
            );

            assertEquals("Solo un administrador puede eliminar usuarios", exception.getMessage());
            verify(usuarioRepositoryPort, never()).eliminar(any());
            verify(authPort, never()).eliminarUsuarioAuth(any());
        }

        @Test
        @DisplayName("Rechazar eliminación si el token es nulo o usuario no registrado")
        void eliminarUsuario_adminAuthIdNulo_lanzaAccesoDenegado() {
            assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.eliminarUsuario(null, 2L)
            );
            verify(usuarioRepositoryPort, never()).eliminar(any());
        }

        @Test
        @DisplayName("Regla 1: Un administrador no puede eliminarse a sí mismo (HTTP 400)")
        void eliminarUsuario_autoEliminacion_lanzaIllegalArgumentException() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(1L)).thenReturn(Optional.of(adminUsuario));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    usuarioService.eliminarUsuario(adminAuthId, 1L)
            );

            assertEquals("Un administrador no puede eliminarse a sí mismo", exception.getMessage());
            verify(usuarioRepositoryPort, never()).eliminar(any());
            verify(authPort, never()).eliminarUsuarioAuth(any());
        }

        @Test
        @DisplayName("Regla 2: Un administrador no puede eliminar a otro administrador (HTTP 403)")
        void eliminarUsuario_eliminarOtroAdmin_lanzaAccesoDenegado() {
            Long otroAdminId = 99L;
            UUID otroAdminAuthId = UUID.randomUUID();
            Usuario otroAdmin = new Usuario(otroAdminId, otroAdminAuthId, "Otro Admin", "admin2@optiway.com", Rol.ADMINISTRADOR, null);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(otroAdminId)).thenReturn(Optional.of(otroAdmin));

            AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.eliminarUsuario(adminAuthId, otroAdminId)
            );

            assertEquals("No está permitido eliminar a otro usuario con rol ADMINISTRADOR", exception.getMessage());
            verify(usuarioRepositoryPort, never()).eliminar(any());
            verify(authPort, never()).eliminarUsuarioAuth(any());
        }

        @Test
        @DisplayName("Rechazar eliminación si el usuario objetivo no existe (HTTP 400)")
        void eliminarUsuario_usuarioNoExiste_lanzaIllegalArgumentException() {
            Long idInexistente = 999L;
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(idInexistente)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    usuarioService.eliminarUsuario(adminAuthId, idInexistente)
            );

            assertEquals("Usuario no encontrado con ID: " + idInexistente, exception.getMessage());
            verify(usuarioRepositoryPort, never()).eliminar(any());
            verify(authPort, never()).eliminarUsuarioAuth(any());
        }
    }

    @Nested
    @DisplayName("Reglas de cambio de rol (actualizarRol)")
    class ActualizarRolTests {

        @Test
        @DisplayName("Cambiar el rol de un usuario no administrador exitosamente")
        void actualizarRol_aUsuarioNormal_exitoso() {
            Long usuarioId = 7L;
            Usuario encargado = new Usuario(usuarioId, UUID.randomUUID(), "Encargado", "enc@optiway.com", Rol.ENCARGADO_TIENDA, adminAuthId);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(usuarioId)).thenReturn(Optional.of(encargado));
            when(usuarioRepositoryPort.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            usuarioService.actualizarRol(adminAuthId, new ActualizarRolCommand(usuarioId, Rol.PLANIFICADOR));

            assertEquals(Rol.PLANIFICADOR, encargado.getRol());
            verify(usuarioRepositoryPort, times(1)).guardar(encargado);
        }

        @Test
        @DisplayName("Rechazar cambio de rol de un usuario ADMINISTRADOR")
        void actualizarRol_aUsuarioAdministrador_lanzaAccesoDenegado() {
            Long otroAdminId = 99L;
            Usuario otroAdmin = new Usuario(otroAdminId, UUID.randomUUID(), "Otro Admin", "admin2@optiway.com", Rol.ADMINISTRADOR, null);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(otroAdminId)).thenReturn(Optional.of(otroAdmin));

            AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.actualizarRol(adminAuthId, new ActualizarRolCommand(otroAdminId, Rol.ENCARGADO_TIENDA))
            );

            assertEquals("No está permitido cambiar el rol de un usuario con rol ADMINISTRADOR", exception.getMessage());
            verify(usuarioRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Rechazar otorgar el rol ADMINISTRADOR a otro usuario")
        void actualizarRol_otorgarAdmin_lanzaAccesoDenegado() {
            Long usuarioId = 8L;
            Usuario encargado = new Usuario(usuarioId, UUID.randomUUID(), "Encargado", "enc@optiway.com", Rol.ENCARGADO_TIENDA, adminAuthId);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorId(usuarioId)).thenReturn(Optional.of(encargado));

            AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.actualizarRol(adminAuthId, new ActualizarRolCommand(usuarioId, Rol.ADMINISTRADOR))
            );

            assertEquals("No está permitido otorgar el rol ADMINISTRADOR", exception.getMessage());
            assertEquals(Rol.ENCARGADO_TIENDA, encargado.getRol());
            verify(usuarioRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Rechazar si el solicitante no es Administrador")
        void actualizarRol_solicitanteNoEsAdmin_lanzaAccesoDenegado() {
            UUID planificadorAuthId = UUID.randomUUID();
            Usuario planificador = new Usuario(5L, planificadorAuthId, "Planificador", "plan@optiway.com", Rol.PLANIFICADOR, null);

            when(usuarioRepositoryPort.obtenerPorAuthUserId(planificadorAuthId)).thenReturn(Optional.of(planificador));

            AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.actualizarRol(planificadorAuthId, new ActualizarRolCommand(7L, Rol.ENCARGADO_TIENDA))
            );

            assertEquals("Solo un administrador puede actualizar roles de usuario", exception.getMessage());
            verify(usuarioRepositoryPort, never()).guardar(any());
        }
    }
}
