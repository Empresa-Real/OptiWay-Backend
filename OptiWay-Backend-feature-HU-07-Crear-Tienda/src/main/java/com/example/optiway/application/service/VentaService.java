package com.example.optiway.application.service;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.in.RegistrarVentaUseCase;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Venta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService implements RegistrarVentaUseCase {

    private final VentaRepositoryPort ventaRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;

    public VentaService(VentaRepositoryPort ventaRepositoryPort,
                        InventarioRepositoryPort inventarioRepositoryPort,
                        TiendaRepositoryPort tiendaRepositoryPort) {
        this.ventaRepositoryPort = ventaRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
    }

    @Override
    @Transactional
    public Venta registrarVenta(RegistrarVentaCommand command) {
        // 1. Validar existencia de la tienda -> Retorna HTTP 404 si no existe
        if (!tiendaRepositoryPort.existsById(command.tiendaId())) {
            throw new RecursoNoEncontradoException("La tienda con ID " + command.tiendaId() + " no fue encontrada.");
        }

        // 2. Validar existencia del producto en la tienda -> Retorna HTTP 404 si no existe
        boolean productoEnTiendaExiste = inventarioRepositoryPort.existeProductoEnTienda(command.tiendaId(), command.productoId());
        if (!productoEnTiendaExiste) {
            throw new RecursoNoEncontradoException("El producto con ID " + command.productoId() + " no está registrado en la tienda.");
        }

        // 3. Validar stock disponible -> Retorna HTTP 400 Bad Request si es insuficiente
        int stockActual = inventarioRepositoryPort.obtenerStock(command.tiendaId(), command.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no disponible para el producto en esta tienda."));

        if (command.cantidad() > stockActual) {
            throw new StockInsuficienteException("Stock insuficiente. Stock disponible: " + stockActual + ", solicitado: " + command.cantidad());
        }

        // 4. Descontar stock y registrar la venta
        int nuevoStock = stockActual - command.cantidad();
        inventarioRepositoryPort.actualizarStock(command.tiendaId(), command.productoId(), nuevoStock);

        Venta venta = new Venta(
                command.tiendaId(),
                command.productoId(),
                command.cantidad(),
                command.fechaHora()
        );

        return ventaRepositoryPort.guardar(venta);
    }
}