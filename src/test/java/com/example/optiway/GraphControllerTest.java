package com.example.optiway;

import com.example.optiway.infrastructure.adapter.in.rest.GraphController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GraphControllerTest {

    private GraphController graphController;

    @BeforeEach
    void setUp() {
        graphController = new GraphController();
    }

    @Test
    void verGrafo_AccesoPublico_RetornaOkConHtml() {
        ResponseEntity<Resource> response = graphController.verGrafo();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/html"));
    }
}
