package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Venta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final VentaJpaRepository ventaJpaRepository;

    public VentaRepositoryAdapter(VentaJpaRepository ventaJpaRepository) {
        this.ventaJpaRepository = ventaJpaRepository;
    }

    @Override
    public Venta save(Venta venta) {
        VentaJpaEntity entity = toEntity(venta);
        VentaJpaEntity savedEntity = ventaJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Venta guardar(Venta venta) {
        return save(venta);
    }

    @Override
    public java.util.Optional<Venta> findById(Long id) {
        return ventaJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Venta> findAll() {
        return ventaJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Venta> findByTiendaId(Long tiendaId) {
        return ventaJpaRepository.findByTiendaId(tiendaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private VentaJpaEntity toEntity(Venta venta) {
        return new VentaJpaEntity(
                venta.getId(),
                venta.getTiendaId(),
                venta.getProductoId(),
                venta.getCantidad(),
                venta.getFechaHora()
        );
    }

    private Venta toDomain(VentaJpaEntity entity) {
        return new Venta(
                entity.getId(),
                entity.getTiendaId(),
                entity.getProductoId(),
                entity.getCantidad(),
                entity.getFechaHora()
        );
    }
}
