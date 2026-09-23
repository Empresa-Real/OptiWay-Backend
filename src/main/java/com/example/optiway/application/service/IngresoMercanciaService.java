package com.example.optiway.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.IngresoMercancia;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;

@Service
public class IngresoMercanciaService implements RegistrarIngresoMercanciaUseCase {

    private final IngresoMercanciaRepositoryPort ingresoRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public IngresoMercanciaService(
            IngresoMercanciaRepositoryPort ingresoRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {

        this.ingresoRepositoryPort = ingresoRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    @Transactional
    public void registrarIngreso(
            UUID authUserId,
            Long centroDistribucionId,
            Long productoId,
            Integer cantidad,
            String origen) {

        // 1. Validar que la cantidad sea mayor que cero
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero");
        }

        // 2. Validar que el origen sea obligatorio
        if (origen == null || origen.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El origen es obligatorio");
        }

        // 3. Buscar al usuario que registra el ingreso
        Usuario usuario = usuarioRepositoryPort
                .obtenerPorAuthUserId(authUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"));

        // 4. Verificar que exista el centro de distribución
        CentroDistribucion centro = centroDistribucionRepositoryPort
                .obtenerPorId(centroDistribucionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Centro de distribución no encontrado"));

        // Validar permisos sobre el centro de distribución
        boolean esAdmin = usuario.getRol() == Rol.ADMINISTRADOR;
        boolean esPlanificador = usuario.getRol() == Rol.PLANIFICADOR;
        boolean esEncargado = usuario.getId() != null && usuario.getId().equals(centro.getEncargadoId());

        if (!esAdmin && !esPlanificador && !esEncargado) {
            throw new AccesoDenegadoException(
                    "No tiene permisos para registrar ingresos en este Centro de Distribución");
        }

        // 5. Verificar que exista el producto
        productoRepositoryPort
                .findById(productoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Producto no encontrado"));

        // 6. Buscar el inventario actual del producto en el centro
        Inventario inventario =
                inventarioRepositoryPort
                        .findByCentroDistribucionIdAndProductoId(
                                centro.getId(),
                                productoId)
                        .orElse(null);

        // 7. Si no existe inventario, crear uno nuevo
        if (inventario == null) {
            inventario = new Inventario();
            inventario.setCentroDistribucionId(centro.getId());
            inventario.setProductoId(productoId);
            inventario.setCantidadActual(cantidad);
            inventario.setStockMinimo(0);
        } else {
            // 8. Si ya existe, sumar la nueva cantidad
            inventario.setCantidadActual(
                    inventario.getCantidadActual() + cantidad);
        }

        // 9. Guardar el inventario actualizado
        inventarioRepositoryPort.save(inventario);

        // 10. Registrar el ingreso de mercancía
        IngresoMercancia ingreso = new IngresoMercancia();

        ingreso.setCentroDistribucionId(
                centro.getId());

        ingreso.setProductoId(
                productoId);

        ingreso.setCantidad(
                cantidad);

        ingreso.setOrigen(
                origen);

        ingreso.setUsuarioId(
                usuario.getId());

        ingreso.setFechaIngreso(
                LocalDateTime.now());

        ingresoRepositoryPort.guardar(ingreso);
    }
}