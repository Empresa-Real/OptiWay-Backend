package com.example.optiway;

import com.example.optiway.application.service.AccesoDenegadoException;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.adapter.in.rest.GraphController;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphControllerTest {

    @Mock
    private AuthorizationService authorizationService;

    private GraphController graphController;

    @BeforeEach
    void setUp() {
        graphController = new GraphController(authorizationService);
    }

    @Test
    void verGrafo_CuandoUsuarioEsAdmin_RetornaOkConHtml() {
        UUID authUserId = UUID.randomUUID();
        Usuario admin = new Usuario(1L, authUserId, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        when(authorizationService.requireAdministrador(eq(authUserId), anyString())).thenReturn(admin);

        ResponseEntity<Resource> response = graphController.verGrafo(authUserId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/html"));
    }

    @Test
    void verGrafo_CuandoUsuarioNoEsAdmin_LanzaAccesoDenegado() {
        UUID authUserId = UUID.randomUUID();
        doThrow(new AccesoDenegadoException("Solo un administrador puede ver el grafo de arquitectura"))
                .when(authorizationService).requireAdministrador(eq(authUserId), anyString());

        assertThrows(AccesoDenegadoException.class, () -> graphController.verGrafo(authUserId));
    }
}
