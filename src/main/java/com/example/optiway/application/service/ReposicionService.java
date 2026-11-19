package com.example.optiway.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.SolicitarReposicionUseCase;
import com.example.optiway.application.port.in.dto.SolicitudReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.SolicitudReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.SolicitudReposicion;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearSolicitudReposicionRequest;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@Service
public class ReposicionService implements SolicitarReposicionUseCase {

    private final SolicitudReposicionRepositoryPort solicitudRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final AuthorizationPort authorizationService;

    public ReposicionService(
            SolicitudReposicionRepositoryPort solicitudRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            AuthorizationPort authorizationService) {
        this.solicitudRepositoryPort = solicitudRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public SolicitudReposicionResponse crearSolicitud(UUID authUserId, Long tiendaId, CrearSolicitudReposicionRequest request) {
        if (request == null || request.getProductoId() == null || request.getCantidad() == null || request.getCantidad() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId y cantidad (>= 1) son obligatorios.");
        }

        Tienda tienda = tiendaRepositoryPort.obtenerPorId(tiendaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda no encontrada con ID: " + tiendaId));

        Usuario usuario = authorizationService.requirePermisoTienda(authUserId, tienda, AuthorizedAction.SOLICITAR_REPOSICION);
        if (!usuario.esEncargadoTienda()) {
            throw new AccesoDenegadoException("Solo un encargado de tienda puede solicitar reposición manual.");
        }

        if (Boolean.FALSE.equals(tienda.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede solicitar reposición para una tienda inactiva.");
        }

        Producto producto = productoRepositoryPort.findById(request.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + request.getProductoId()));

        SolicitudReposicion nuevaSolicitud = new SolicitudReposicion(
                null,
                tienda.getId(),
                producto.getId(),
                request.getCantidad(),
                "PENDIENTE",
                usuario.getId(),
                LocalDateTime.now()
        );

        SolicitudReposicion guardada = solicitudRepositoryPort.save(nuevaSolicitud);

        return new SolicitudReposicionResponse(
                guardada.getId(),
                guardada.getTiendaId(),
                guardada.getProductoId(),
                producto.getNombre(),
                guardada.getCantidad(),
                guardada.getEstado(),
                guardada.getUsuarioId(),
                guardada.getFechaSolicitud()
        );
    }

    @Override
    public List<SolicitudReposicionResponse> obtenerTodasLasSolicitudes(UUID authUserId) {
        Usuario usuario = authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.LISTAR_SOLICITUDES_REPOSICION);
        if (usuario.esAdministrador()) {
            throw new AccesoDenegadoException("Solo un planificador puede consultar las solicitudes de reposición.");
        }

        return solicitudRepositoryPort.findAll().stream()
                .map(solicitud -> {
                    String nombreProducto = productoRepositoryPort.findById(solicitud.getProductoId())
                            .map(Producto::getNombre)
                            .orElse("Producto #" + solicitud.getProductoId());

                    return new SolicitudReposicionResponse(
                            solicitud.getId(),
                            solicitud.getTiendaId(),
                            solicitud.getProductoId(),
                            nombreProducto,
                            solicitud.getCantidad(),
                            solicitud.getEstado(),
                            solicitud.getUsuarioId(),
                            solicitud.getFechaSolicitud()
                    );
                })
                .toList();
    }
}