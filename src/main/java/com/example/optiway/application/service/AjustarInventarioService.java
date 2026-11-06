package com.example.optiway.application.service;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.AjustarInventarioCommand;
import com.example.optiway.application.port.in.AjustarInventarioUseCase;
import com.example.optiway.application.port.in.dto.AjusteInventarioResponse;
import com.example.optiway.application.port.out.AjusteInventarioRepositoryPort;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.AjusteInventario;
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
    private final AuthorizationPort authorizationService;

    public AjustarInventarioService(
            AjusteInventarioRepositoryPort ajusteRepository,
            InventarioRepositoryPort inventarioRepository,
            ProductoRepositoryPort productoRepository,
            TiendaRepositoryPort tiendaRepository,
            AuthorizationPort authorizationService) {
        this.ajusteRepository = ajusteRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.tiendaRepository = tiendaRepository;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public AjusteInventarioResponse ajustarInventario(
            UUID authUserId, AjustarInventarioCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("Los datos del ajuste son obligatorios.");
        }
        if (command.tiendaId() == null || command.tiendaId() <= 0) {
            throw new IllegalArgumentException("El ID de la tienda debe ser válido.");
        }
        if (command.productoId() == null || command.productoId() <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser válido.");
        }
        if (command.diferencia() == null || command.diferencia() == 0) {
            throw new IllegalArgumentException("La diferencia debe ser distinta de cero.");
        }

        Tienda tienda = tiendaRepository.obtenerPorId(command.tiendaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La tienda con ID " + command.tiendaId() + " no fue encontrada."));

        Usuario usuario = authorizationService.requirePermisoTienda(
                authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA);

        productoRepository.findById(command.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto con ID " + command.productoId() + " no fue encontrado."));

        Inventario inventario = inventarioRepository
                .findByTiendaIdAndProductoId(command.tiendaId(), command.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto no está registrado en el inventario de esta tienda."));

        int cantidadAnterior = inventario.getCantidadActual() != null
                ? inventario.getCantidadActual() : 0;

        int cantidadNueva;
        try {
            cantidadNueva = Math.addExact(cantidadAnterior, command.diferencia());
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("La cantidad resultante está fuera del rango permitido.");
        }

        if (cantidadNueva < 0) {
            throw new IllegalArgumentException(
                    "El ajuste no puede dejar el inventario en una cantidad negativa. Stock actual: "
                            + cantidadAnterior + ", diferencia: " + command.diferencia());
        }

        inventario.setCantidadActual(cantidadNueva);
        inventarioRepository.save(inventario);

        AjusteInventario ajuste = new AjusteInventario(
                null,
                command.tiendaId(),
                command.productoId(),
                cantidadAnterior,
                command.diferencia(),
                cantidadNueva,
                usuario.getId(),
                LocalDateTime.now()
        );

        AjusteInventario ajusteGuardado = ajusteRepository.guardar(ajuste);
        return AjusteInventarioResponse.from(ajusteGuardado);
    }
}
