package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Venta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import com.example.optiway.application.port.out.VentaProductoResumen;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class VentaRepositoryAdapter implements VentaRepositoryPort {

    private final VentaJpaRepository ventaJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
    public Optional<Venta> findById(Long id) {
        return ventaJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Venta> findAll() {
        return ventaJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Venta> findByTiendaId(Long tiendaId) {
        return ventaJpaRepository.findByTienda_Id(tiendaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }
    @Override
    public List<Venta> buscarVentasPorProductoYTiendasEnPeriodo(
            Long productoId,
            List<Long> tiendaIds,
            LocalDateTime desdeInclusive,
            LocalDateTime hastaExclusivo) {
        return ventaJpaRepository.buscarVentasPorProductoYTiendasEnPeriodo(
                        productoId, tiendaIds, desdeInclusive, hastaExclusivo)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<VentaProductoResumen> resumirUnidadesVendidasPorProducto(
            Long tiendaId, LocalDateTime desdeInclusive, LocalDateTime hastaExclusive) {
        return ventaJpaRepository.resumirUnidadesVendidasPorProducto(tiendaId, desdeInclusive, hastaExclusive);
    }
    private VentaJpaEntity toEntity(Venta venta) {
        VentaJpaEntity entity = new VentaJpaEntity();
        entity.setId(venta.getId());
        if (venta.getTiendaId() != null) {
            entity.setTienda(entityManager.getReference(TiendaJpaEntity.class, venta.getTiendaId()));
        }
        if (venta.getProductoId() != null) {
            entity.setProducto(entityManager.getReference(ProductoJpaEntity.class, venta.getProductoId()));
        }
        entity.setCantidad(venta.getCantidad());
        entity.setFechaHora(venta.getFechaHora());
        return entity;
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
