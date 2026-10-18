package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.AccesoDenegadoException;
import com.example.optiway.application.service.UsuarioService;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
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
        usuarioService = new UsuarioService(usuarioRepositoryPort, authPort);
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
            when(usuarioRepositoryPort.obtenerPorID(usuarioId)).thenReturn(Optional.of(usuarioAEliminar));

            usuarioService.eliminarUsuario(usuarioId, adminAuthId);

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
            when(usuarioRepositoryPort.obtenerPorID(usuarioId)).thenReturn(Optional.of(usuarioSinAuth));

            usuarioService.eliminarUsuario(usuarioId, adminAuthId);

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
                    usuarioService.eliminarUsuario(2L, planificadorAuthId)
            );

            assertEquals("Solo un administrador puede eliminar usuarios", exception.getMessage());
            verify(usuarioRepositoryPort, never()).eliminar(any());
            verify(authPort, never()).eliminarUsuarioAuth(any());
        }

        @Test
        @DisplayName("Rechazar eliminación si el token es nulo o usuario no registrado")
        void eliminarUsuario_adminAuthIdNulo_lanzaAccesoDenegado() {
            assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.eliminarUsuario(2L, null)
            );
            verify(usuarioRepositoryPort, never()).eliminar(any());
        }

        @Test
        @DisplayName("Regla 1: Un administrador no puede eliminarse a sí mismo (HTTP 400)")
        void eliminarUsuario_autoEliminacion_lanzaIllegalArgumentException() {
            when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepositoryPort.obtenerPorID(1L)).thenReturn(Optional.of(adminUsuario));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    usuarioService.eliminarUsuario(1L, adminAuthId)
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
            when(usuarioRepositoryPort.obtenerPorID(otroAdminId)).thenReturn(Optional.of(otroAdmin));

            AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class, () ->
                    usuarioService.eliminarUsuario(otroAdminId, adminAuthId)
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
            when(usuarioRepositoryPort.obtenerPorID(idInexistente)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    usuarioService.eliminarUsuario(idInexistente, adminAuthId)
            );

            assertEquals("Usuario no encontrado con ID: " + idInexistente, exception.getMessage());
            verify(usuarioRepositoryPort, never()).eliminar(any());
            verify(authPort, never()).eliminarUsuarioAuth(any());
        }
    }
}
