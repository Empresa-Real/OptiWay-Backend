package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.CambiarEstadoProductoCommand;
import com.example.optiway.application.port.in.EditarProductoCommand;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.ProductoService;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductoServiceTest {

    private ProductoRepositoryPort productoRepositoryPort;
    private UsuarioRepositoryPort usuarioRepositoryPort;
    private InventarioRepositoryPort inventarioRepositoryPort;
    private AuthorizationPort authorizationPort;
    private ProductoService productoService;

    private final UUID authUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        productoRepositoryPort = mock(ProductoRepositoryPort.class);
        usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
        inventarioRepositoryPort = mock(InventarioRepositoryPort.class);
        authorizationPort = mock(AuthorizationPort.class);

        Usuario administrador = new Usuario(
                1L,
                authUserId,
                "Administrador",
                "admin@optiway.com",
                Rol.ADMINISTRADOR,
                null
        );

        when(authorizationPort.requireGestionLogistica(
                eq(authUserId), any(AuthorizedAction.class)))
                .thenReturn(administrador);

        productoService = new ProductoService(
                productoRepositoryPort,
                usuarioRepositoryPort,
                inventarioRepositoryPort,
                authorizationPort
        );
    }

    @Test
    @DisplayName("Editar producto inexistente debe lanzar RecursoNoEncontradoException")
    void editarProductoInexistente_debeLanzarRecursoNoEncontradoException() {
        Long productoId = 999L;

        when(productoRepositoryPort.findById(productoId))
                .thenReturn(Optional.empty());

        EditarProductoCommand command = new EditarProductoCommand(
                productoId,
                "Arroz Diana",
                null,
                null,
                null
        );

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.editarProducto(authUserId, command)
        );

        assertEquals(
                "Producto no encontrado con ID: 999",
                exception.getMessage()
        );

        verify(productoRepositoryPort).findById(productoId);
        verify(productoRepositoryPort, never()).update(any(Producto.class), anyLong());
        verify(productoRepositoryPort, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Cambiar estado de producto inexistente debe lanzar RecursoNoEncontradoException")
    void cambiarEstadoProductoInexistente_debeLanzarRecursoNoEncontradoException() {
        Long productoId = 999L;

        when(productoRepositoryPort.findById(productoId))
                .thenReturn(Optional.empty());

        CambiarEstadoProductoCommand command = new CambiarEstadoProductoCommand(
                productoId,
                false
        );

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.cambiarEstadoProducto(authUserId, command)
        );

        assertEquals(
                "El producto no existe con ID: 999",
                exception.getMessage()
        );

        verify(productoRepositoryPort).findById(productoId);
        verify(productoRepositoryPort, never()).save(any(Producto.class));
        verify(productoRepositoryPort, never()).update(any(Producto.class), anyLong());
    }
}
