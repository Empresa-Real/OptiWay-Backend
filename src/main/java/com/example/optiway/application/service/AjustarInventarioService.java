package com.example.optiway.application.service;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.AjustarInventarioCommand;
import com.example.optiway.application.port.in.AjustarInventarioUseCase;
import com.example.optiway.application.port.in.dto.AjusteInventarioResponse;
import com.example.optiway.application.port.out.AjusteInventarioRepositoryPort;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.AjusteInventario;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AjustarInventarioService implements AjustarInventarioUseCase {

    private final AjusteInventarioRepositoryPort ajusteRepository;
    private final InventarioRepositoryPort inventarioRepository;
    private final ProductoRepositoryPort productoRepository;
    private final TiendaRepositoryPort tiendaRepository;
    private final CentroDistribucionRepositoryPort centroDistribucionRepository;
    private final AuthorizationPort authorizationService;

    public AjustarInventarioService(
            AjusteInventarioRepositoryPort ajusteRepository,
            InventarioRepositoryPort inventarioRepository,
            ProductoRepositoryPort productoRepository,
            TiendaRepositoryPort tiendaRepository,
            CentroDistribucionRepositoryPort centroDistribucionRepository,
            AuthorizationPort authorizationService) {
        this.ajusteRepository = ajusteRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.tiendaRepository = tiendaRepository;
        this.centroDistribucionRepository = centroDistribucionRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public AjusteInventarioResponse ajustarInventario(
            UUID authUserId, AjustarInventarioCommand command) {

        validarCommand(command);

        boolean esTienda = command.tiendaId() != null;

        Usuario usuario;
        Inventario inventario;

        if (esTienda) {
            Tienda tienda = tiendaRepository.obtenerPorId(command.tiendaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "La tienda con ID " + command.tiendaId() + " no fue encontrada."));

            usuario = authorizationService.requirePermisoTienda(
                    authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA);

            verificarProductoExiste(command.productoId());

            inventario = inventarioRepository
                    .findByTiendaIdAndProductoId(command.tiendaId(), command.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "El producto no está registrado en el inventario de esta tienda."));
        } else {
            CentroDistribucion centro = centroDistribucionRepository.obtenerPorId(command.centroDistribucionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "El centro de distribución con ID " + command.centroDistribucionId() + " no fue encontrado."));

            usuario = authorizationService.requireEncargadoDelCentro(
                    authUserId, centro, AuthorizedAction.AJUSTAR_INVENTARIO_CENTRO_DISTRIBUCION);

            verificarProductoExiste(command.productoId());

            inventario = inventarioRepository
                    .findByCentroDistribucionIdAndProductoId(command.centroDistribucionId(), command.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "El producto no está registrado en el inventario de este centro de distribución."));
        }

        int cantidadAnterior = inventario.getCantidadActual() != null
                ? inventario.getCantidadActual() : 0;
        int cantidadNueva = command.nuevaCantidad();
        int diferencia = cantidadNueva - cantidadAnterior;
        LocalDateTime ahora = LocalDateTime.now();

        inventario.setCantidadActual(cantidadNueva);
        inventario.setFechaActualizacion(ahora);
        inventario.setUsuarioActualizacionId(usuario.getId());
        inventarioRepository.save(inventario);

        AjusteInventario ajuste = new AjusteInventario(
                null,
                command.tiendaId(),
                command.centroDistribucionId(),
                command.productoId(),
                cantidadAnterior,
                diferencia,
                cantidadNueva,
                usuario.getId(),
                ahora
        );

        AjusteInventario ajusteGuardado = ajusteRepository.guardar(ajuste);
        return AjusteInventarioResponse.from(ajusteGuardado);
    }

    private void validarCommand(AjustarInventarioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Los datos del ajuste son obligatorios.");
        }

        boolean tieneTienda = command.tiendaId() != null;
        boolean tieneCD = command.centroDistribucionId() != null;

        if (tieneTienda == tieneCD) {
            throw new IllegalArgumentException(
                    "Debe especificar exactamente una ubicación: tiendaId o centroDistribucionId.");
        }

        if (tieneTienda && command.tiendaId() <= 0) {
            throw new IllegalArgumentException("El ID de la tienda debe ser válido.");
        }

        if (tieneCD && command.centroDistribucionId() <= 0) {
            throw new IllegalArgumentException("El ID del centro de distribución debe ser válido.");
        }

        if (command.productoId() == null || command.productoId() <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser válido.");
        }

        if (command.nuevaCantidad() == null || command.nuevaCantidad() < 0) {
            throw new IllegalArgumentException("La nueva cantidad debe ser mayor o igual a cero.");
        }
    }

    private void verificarProductoExiste(Long productoId) {
        productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto con ID " + productoId + " no fue encontrado."));
    }
}
