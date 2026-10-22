package com.example.optiway.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;
import com.example.optiway.application.port.in.dto.IngresoMercanciaResponse;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.IngresoMercancia;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.infrastructure.security.AccionAutorizada;

@Service
public class IngresoMercanciaService implements RegistrarIngresoMercanciaUseCase {

    private final IngresoMercanciaRepositoryPort ingresoRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final AuthorizationPort authorizationService;

    public IngresoMercanciaService(
            IngresoMercanciaRepositoryPort ingresoRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            AuthorizationPort authorizationService) {

        this.ingresoRepositoryPort = ingresoRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public IngresoMercanciaResponse registrarIngreso(UUID authUserId, RegistrarIngresoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Los datos de ingreso son obligatorios");
        }
        if (command.centroDistribucionId() == null) {
            throw new IllegalArgumentException("El Centro de Distribución es obligatorio");
        }
        if (command.productoId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (command.cantidad() == null || command.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (command.origen() == null || command.origen().isBlank()) {
            throw new IllegalArgumentException("El origen es obligatorio");
        }

        // 1. Verificar que exista el centro de distribución
        CentroDistribucion centro = centroDistribucionRepositoryPort
                .obtenerPorId(command.centroDistribucionId())
                .orElseThrow(() -> new IllegalArgumentException("Centro de distribución no encontrado"));

        // 2. Validar permisos del usuario sobre el centro de distribución
        Usuario usuario = authorizationService.requirePermisoCentroDistribucion(
                authUserId, centro, AccionAutorizada.REGISTRAR_INGRESO);

        // 3. Verificar que exista el producto
        productoRepositoryPort.findById(command.productoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // 4. Validar capacidad máxima del Centro de Distribución
        if (centro.getCapacidad() != null && centro.getCapacidad() > 0) {
            int stockTotalActual = inventarioRepositoryPort.findByCentroDistribucionId(centro.getId())
                    .stream()
                    .mapToInt(Inventario::getCantidadActual)
                    .sum();
            if (stockTotalActual + command.cantidad() > centro.getCapacidad()) {
                throw new IllegalArgumentException("El ingreso excede la capacidad máxima del centro de distribución ("
                        + centro.getCapacidad() + "). Capacidad disponible: " + Math.max(0, centro.getCapacidad() - stockTotalActual));
            }
        }

        // 5. Buscar o inicializar inventario del producto en el centro
        Inventario inventario = inventarioRepositoryPort
                .findByCentroDistribucionIdAndProductoId(centro.getId(), command.productoId())
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    nuevo.setCentroDistribucionId(centro.getId());
                    nuevo.setProductoId(command.productoId());
                    nuevo.setCantidadActual(0);
                    nuevo.setStockMinimo(0);
                    return nuevo;
                });

        // 6. Sumar stock y persistir actualización en inventario
        inventario.agregarStock(command.cantidad());
        inventarioRepositoryPort.save(inventario);

        // 7. Registrar la transacción histórica de ingreso
        IngresoMercancia ingreso = new IngresoMercancia();
        ingreso.setCentroDistribucionId(centro.getId());
        ingreso.setProductoId(command.productoId());
        ingreso.setCantidad(command.cantidad());
        ingreso.setOrigen(command.origen());
        ingreso.setUsuarioId(usuario.getId());
        ingreso.setFechaIngreso(LocalDateTime.now());

        return IngresoMercanciaResponse.from(ingresoRepositoryPort.guardar(ingreso));
    }
}