package com.example.optiway.infrastructure.adapter.out;

import org.springframework.stereotype.Component;

import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.adapter.out.persistence.VentaJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.VentaJpaRepository;

@Component
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final VentaJpaRepository ventaJpaRepository;

    public VentaRepositoryAdapter(VentaJpaRepository ventaJpaRepository) {
        this.ventaJpaRepository = ventaJpaRepository;
    }

    @Override
    public Venta guardar(Venta venta) {
        VentaJpaEntity entity = toEntity(venta);
        VentaJpaEntity savedEntity = ventaJpaRepository.save(entity);
        return toDomain(savedEntity);
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