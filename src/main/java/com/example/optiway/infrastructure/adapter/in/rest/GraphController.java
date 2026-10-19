package com.example.optiway.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@Tag(name = "Grafo de Arquitectura", description = "Visualización interactiva de la arquitectura del backend")
public class GraphController {

    @GetMapping(value = {"/graph", "/graph/", "/graph.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Obtener HTML del grafo interactivo", description = "Retorna la visualización interactiva del grafo de arquitectura (acceso público).")
    public ResponseEntity<Resource> verGrafo() {
        Resource resource = new ClassPathResource("static/graph.html");
        if (!resource.exists()) {
            File fallback = new File("graphify-out/graph.html");
            if (fallback.exists()) {
                resource = new FileSystemResource(fallback);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"graph.html\"")
                .body(resource);
    }
}
