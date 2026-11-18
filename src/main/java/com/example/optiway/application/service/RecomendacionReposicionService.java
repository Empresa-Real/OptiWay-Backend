package com.example.optiway.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.exception.StockInsuficienteException;
import com.example.optiway.application.port.in.AprobarRecomendacionCommand;
import com.example.optiway.application.port.in.AprobarRecomendacionUseCase;
import com.example.optiway.application.port.in.ConsultarRecomendacionesReposicionUseCase;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionCommand;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionUseCase;
import com.example.optiway.application.port.in.RechazarRecomendacionCommand;
import com.example.optiway.application.port.in.RechazarRecomendacionUseCase;
import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.RecomendacionReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.OrdenReposicion;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.RecomendacionReposicion;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@Service
@Transactional
public class RecomendacionReposicionService implements
        CrearRecomendacionReposicionUseCase,
        ConsultarRecomendacionesReposicionUseCase,
        AprobarRecomendacionUseCase,
        RechazarRecomendacionUseCase {

    private static final String ESTADO_PROPUESTA = "PROPUESTA";
    private static final String ESTADO_APROBADA = "APROBADA";
    private static final String ESTADO_RECHAZADA = "RECHAZADA";

    private final AuthorizationPort authorizationPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final CentroDistribucionRepositoryPort centroRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final RecomendacionReposicionRepositoryPort recomendacionRepositoryPort;
    private final OrdenReposicionRepositoryPort ordenRepositoryPort;

    public RecomendacionReposicionService(
            AuthorizationPort authorizationPort,
            ProductoRepositoryPort productoRepositoryPort,
            CentroDistribucionRepositoryPort centroRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort,
            RecomendacionReposicionRepositoryPort recomendacionRepositoryPort,
            OrdenReposicionRepositoryPort ordenRepositoryPort) {

        this.authorizationPort = authorizationPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.centroRepositoryPort = centroRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.recomendacionRepositoryPort = recomendacionRepositoryPort;
        this.ordenRepositoryPort = ordenRepositoryPort;
    }

    @Override
    public RecomendacionReposicionResponse crear(
            UUID authUserId,
            CrearRecomendacionReposicionCommand command) {

        Usuario usuario = authorizationPort.requirePlanificador(
                authUserId, AuthorizedAction.CREAR_RECOMENDACION_REPOSICION);

        validarCommand(command);

        Producto producto = productoRepositoryPort.findById(command.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el producto indicado."));

        if (!producto.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede recomendar la reposición de un producto inactivo.");
        }

        Tienda tienda = tiendaRepositoryPort.obtenerPorId(command.tiendaDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la tienda de destino."));

        if (!tienda.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede crear una recomendación para una tienda inactiva.");
        }

        CentroDistribucion centro = validarCentroYDisponibilidad(
                command.centroDistribucionOrigenId(), producto.getId(), command.cantidad());

        boolean fueraDeCobertura = esFueraDeCobertura(centro, tienda.getId());

        if (fueraDeCobertura
                && !Boolean.TRUE.equals(command.confirmarFueraDeCobertura())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La tienda no pertenece a la zona de cobertura del Centro de Distribución "
                            + "seleccionado. Confirme la operación enviando confirmarFueraDeCobertura: true "
                            + "o seleccione otro Centro de Distribución.");
        }

        RecomendacionReposicion recomendacion = new RecomendacionReposicion();
        recomendacion.setProductoId(producto.getId());
        recomendacion.setNombreProducto(producto.getNombre());
        recomendacion.setCantidad(command.cantidad());
        recomendacion.setCentroDistribucionOrigenId(centro.getId());
        recomendacion.setTiendaDestinoId(tienda.getId());
        recomendacion.setEstado(ESTADO_PROPUESTA);
        recomendacion.setFueraDeCobertura(fueraDeCobertura);
        recomendacion.setCreadoPor(usuario.getAuthUserId());
        recomendacion.setFechaCreacion(LocalDateTime.now());

        // Una propuesta no ejecuta el traslado ni descuenta existencias.
        RecomendacionReposicion guardada =
                recomendacionRepositoryPort.guardar(recomendacion);

        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecomendacionReposicionResponse> consultar(UUID authUserId) {

        authorizationPort.requirePlanificador(
                authUserId, AuthorizedAction.LISTAR_RECOMENDACIONES_REPOSICION);

        return recomendacionRepositoryPort.obtenerTodas()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrdenReposicionResponse aprobar(
            UUID authUserId,
            AprobarRecomendacionCommand command) {

        Usuario usuario = authorizationPort.requirePlanificador(
                authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION);

        if (command == null || command.recomendacionId() == null || command.recomendacionId() <= 0) {
            throw new IllegalArgumentException("El ID de la recomendación debe ser válido.");
        }

        RecomendacionReposicion recomendacion = obtenerPropuestaPendiente(command.recomendacionId());

        boolean ajustada = false;

        if (command.cantidadAjustada() != null) {
            if (command.cantidadAjustada() <= 0) {
                throw new IllegalArgumentException("La cantidad ajustada debe ser mayor que cero.");
            }
            if (!Objects.equals(recomendacion.getCantidad(), command.cantidadAjustada())) {
                recomendacion.setCantidad(command.cantidadAjustada());
                ajustada = true;
            }
        }

        if (command.centroDistribucionOrigenId() != null) {
            if (command.centroDistribucionOrigenId() <= 0) {
                throw new IllegalArgumentException("El ID del Centro de Distribución debe ser válido.");
            }
            if (!Objects.equals(recomendacion.getCentroDistribucionOrigenId(), command.centroDistribucionOrigenId())) {
                recomendacion.setCentroDistribucionOrigenId(command.centroDistribucionOrigenId());
                ajustada = true;
            }
        }

        if (ajustada) {
            CentroDistribucion centro = validarCentroYDisponibilidad(
                    recomendacion.getCentroDistribucionOrigenId(),
                    recomendacion.getProductoId(),
                    recomendacion.getCantidad());
            recomendacion.setFueraDeCobertura(
                    esFueraDeCobertura(centro, recomendacion.getTiendaDestinoId()));
        }

        LocalDateTime ahora = LocalDateTime.now();
        recomendacion.setEstado(ESTADO_APROBADA);
        recomendacion.setResueltoPorUsuarioId(usuario.getAuthUserId());
        recomendacion.setAccionTomada(ajustada ? "AJUSTADA_Y_APROBADA" : "APROBADA");
        recomendacion.setFechaResolucion(ahora);
        recomendacionRepositoryPort.guardar(recomendacion);

        OrdenReposicion orden = new OrdenReposicion();
        orden.setRecomendacionId(recomendacion.getId());
        orden.setProductoId(recomendacion.getProductoId());
        orden.setNombreProducto(recomendacion.getNombreProducto());
        orden.setCantidad(recomendacion.getCantidad());
        orden.setCentroDistribucionOrigenId(recomendacion.getCentroDistribucionOrigenId());
        orden.setTiendaDestinoId(recomendacion.getTiendaDestinoId());
        orden.setEstado(ESTADO_APROBADA);
        orden.setCreadoPor(usuario.getAuthUserId());
        orden.setFechaCreacion(ahora);

        return OrdenReposicionResponse.from(ordenRepositoryPort.guardar(orden));
    }

    @Override
    public RecomendacionReposicionResponse rechazar(
            UUID authUserId,
            RechazarRecomendacionCommand command) {

        Usuario usuario = authorizationPort.requirePlanificador(
                authUserId, AuthorizedAction.RECHAZAR_RECOMENDACION_REPOSICION);

        if (command == null || command.recomendacionId() == null || command.recomendacionId() <= 0) {
            throw new IllegalArgumentException("El ID de la recomendación debe ser válido.");
        }

        if (command.motivo() == null || command.motivo().isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio.");
        }

        RecomendacionReposicion recomendacion = obtenerPropuestaPendiente(command.recomendacionId());

        recomendacion.setEstado(ESTADO_RECHAZADA);
        recomendacion.setMotivoRechazo(command.motivo().trim());
        recomendacion.setResueltoPorUsuarioId(usuario.getAuthUserId());
        recomendacion.setAccionTomada("RECHAZADA");
        recomendacion.setFechaResolucion(LocalDateTime.now());

        return toResponse(recomendacionRepositoryPort.guardar(recomendacion));
    }

    private RecomendacionReposicion obtenerPropuestaPendiente(Long recomendacionId) {
        RecomendacionReposicion recomendacion = recomendacionRepositoryPort.obtenerPorId(recomendacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la recomendación de reposición con ID: " + recomendacionId));

        if (!ESTADO_PROPUESTA.equalsIgnoreCase(recomendacion.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La recomendación ya fue resuelta con estado: " + recomendacion.getEstado());
        }

        return recomendacion;
    }

    private CentroDistribucion validarCentroYDisponibilidad(
            Long centroId,
            Long productoId,
            int cantidad) {

        CentroDistribucion centro = centroRepositoryPort.obtenerPorId(centroId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el Centro de Distribución de origen."));

        if (!centro.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede operar con un Centro de Distribución inactivo.");
        }

        Inventario inventario = inventarioRepositoryPort
                .findByCentroDistribucionIdAndProductoId(centro.getId(), productoId)
                .orElseThrow(() -> new StockInsuficienteException(
                        "El Centro de Distribución seleccionado no tiene inventario registrado para este producto."));

        int stockDisponible = inventario.getCantidadActual() != null
                ? inventario.getCantidadActual() : 0;

        if (stockDisponible < cantidad) {
            throw new StockInsuficienteException(
                    "Stock insuficiente en el Centro de Distribución. "
                            + "Disponible: " + stockDisponible
                            + ". Cantidad solicitada: " + cantidad + ".");
        }

        return centro;
    }

    private boolean esFueraDeCobertura(CentroDistribucion centro, Long tiendaId) {
        return centro.getTiendasAbastecidasIds() == null
                || !centro.getTiendasAbastecidasIds().contains(tiendaId);
    }

    private void validarCommand(CrearRecomendacionReposicionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Debe enviar los datos de la recomendación.");
        }

        if (command.productoId() == null || command.productoId() <= 0
                || command.centroDistribucionOrigenId() == null
                || command.centroDistribucionOrigenId() <= 0
                || command.tiendaDestinoId() == null
                || command.tiendaDestinoId() <= 0) {
            throw new IllegalArgumentException(
                    "Los identificadores de producto, Centro de Distribución y tienda son obligatorios y deben ser válidos.");
        }

        if (command.cantidad() == null || command.cantidad() <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad solicitada debe ser un número mayor que cero.");
        }
    }

    private RecomendacionReposicionResponse toResponse(
            RecomendacionReposicion recomendacion) {

        return new RecomendacionReposicionResponse(
                recomendacion.getId(),
                recomendacion.getProductoId(),
                recomendacion.getNombreProducto(),
                recomendacion.getCantidad(),
                recomendacion.getCentroDistribucionOrigenId(),
                recomendacion.getTiendaDestinoId(),
                recomendacion.getEstado(),
                recomendacion.isFueraDeCobertura(),
                recomendacion.getCreadoPor(),
                recomendacion.getFechaCreacion(),
                recomendacion.getResueltoPorUsuarioId(),
                recomendacion.getAccionTomada(),
                recomendacion.getFechaResolucion(),
                recomendacion.getMotivoRechazo()
        );
    }
}
