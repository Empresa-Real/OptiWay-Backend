package com.example.optiway.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.exception.StockInsuficienteException;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionCommand;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionUseCase;
import com.example.optiway.application.port.in.ConsultarRecomendacionesReposicionUseCase;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.RecomendacionReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.RecomendacionReposicion;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@Service
@Transactional
public class RecomendacionReposicionService implements
        CrearRecomendacionReposicionUseCase,
        ConsultarRecomendacionesReposicionUseCase {

    private final AuthorizationPort authorizationPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final CentroDistribucionRepositoryPort centroRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final RecomendacionReposicionRepositoryPort recomendacionRepositoryPort;

    public RecomendacionReposicionService(
            AuthorizationPort authorizationPort,
            ProductoRepositoryPort productoRepositoryPort,
            CentroDistribucionRepositoryPort centroRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort,
            RecomendacionReposicionRepositoryPort recomendacionRepositoryPort) {

        this.authorizationPort = authorizationPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.centroRepositoryPort = centroRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.recomendacionRepositoryPort = recomendacionRepositoryPort;
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

        CentroDistribucion centro = centroRepositoryPort.obtenerPorId(
                        command.centroDistribucionOrigenId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el Centro de Distribución de origen."));

        Tienda tienda = tiendaRepositoryPort.obtenerPorId(command.tiendaDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la tienda de destino."));

        if (!tienda.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede crear una recomendación para una tienda inactiva.");
        }

        Inventario inventario = inventarioRepositoryPort
                .findByCentroDistribucionIdAndProductoId(
                        centro.getId(), producto.getId())
                .orElseThrow(() -> new StockInsuficienteException(
                        "El Centro de Distribución seleccionado no tiene inventario registrado para este producto."));

        int stockDisponible = inventario.getCantidadActual() != null
                ? inventario.getCantidadActual() : 0;

        if (stockDisponible < command.cantidad()) {
            throw new StockInsuficienteException(
                    "Stock insuficiente en el Centro de Distribución. "
                            + "Disponible: " + stockDisponible
                            + ". Cantidad solicitada: " + command.cantidad() + ".");
        }

        boolean fueraDeCobertura = centro.getTiendasAbastecidasIds() == null
                || !centro.getTiendasAbastecidasIds().contains(tienda.getId());

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
        recomendacion.setEstado("PROPUESTA");
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
                recomendacion.getFechaCreacion()
        );
    }
}