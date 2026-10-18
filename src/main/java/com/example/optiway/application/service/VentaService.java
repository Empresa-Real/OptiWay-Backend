package com.example.optiway.application.service;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.in.RegistrarVentaUseCase;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VentaService implements RegistrarVentaUseCase {

    private final VentaRepositoryPort ventaRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final AuthorizationService authorizationService;

    public VentaService(
            VentaRepositoryPort ventaRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            AuthorizationService authorizationService) {
        this.ventaRepositoryPort = ventaRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public Venta registrarVenta(RegistrarVentaCommand command, UUID authUserId) {
        if (command == null) {
            throw new IllegalArgumentException("Los datos de la venta son obligatorios.");
        }
        if (command.tiendaId() == null) {
            throw new IllegalArgumentException("El ID de la tienda es obligatorio.");
        }
        if (command.productoId() == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio.");
        }
        if (command.cantidad() == null || command.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad de venta debe ser mayor a cero.");
        }

        // 1. Validar existencia de la tienda
        Tienda tienda = tiendaRepositoryPort.obtenerPorId(command.tiendaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La tienda con ID " + command.tiendaId() + " no fue encontrada."));

        // 2. Control de acceso y roles si se proporciona identidad
        if (authUserId != null) {
            authorizationService.requirePermisoTienda(authUserId, tienda, "registrar ventas en la tienda especificada.");
        }

        // 3. Validar existencia y estado del producto
        Producto producto = productoRepositoryPort.findById(command.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto con ID " + command.productoId() + " no fue encontrado en el catálogo."));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new IllegalArgumentException(
                    "El producto con ID " + command.productoId() + " se encuentra inactivo y no puede comercializarse.");
        }

        // 4. Validar existencia del producto en el inventario de la tienda
        Inventario inventario = inventarioRepositoryPort.findByTiendaIdAndProductoId(command.tiendaId(), command.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto con ID " + command.productoId() + " no está registrado en el inventario de la tienda."));

        // 5. Validar disponibilidad de existencias
        int stockActual = inventario.getCantidadActual() != null ? inventario.getCantidadActual() : 0;
        if (command.cantidad() > stockActual) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Stock disponible: " + stockActual + ", solicitado: " + command.cantidad());
        }

        // 6. Descontar stock y persistir actualización en inventario
        inventario.setCantidadActual(stockActual - command.cantidad());
        inventarioRepositoryPort.save(inventario);

        // 7. Registrar transacción histórica de la venta
        Venta venta = new Venta(
                null,
                command.tiendaId(),
                command.productoId(),
                command.cantidad(),
                command.fechaHora() != null ? command.fechaHora() : LocalDateTime.now()
        );

        return ventaRepositoryPort.guardar(venta);
    }
}
