
package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.AjusteInventarioRepositoryPort;
import com.example.optiway.domain.model.AjusteInventario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AjusteInventarioRepositoryAdapter
        implements AjusteInventarioRepositoryPort {

    private final AjusteInventarioJpaRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public AjusteInventarioRepositoryAdapter(
            AjusteInventarioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AjusteInventario guardar(AjusteInventario ajuste) {

        AjusteInventarioJpaEntity entity =
                new AjusteInventarioJpaEntity();

        if (ajuste.getTiendaId() != null) {
            entity.setTienda(entityManager.getReference(
                    TiendaJpaEntity.class,
                    ajuste.getTiendaId()
            ));
        }

        if (ajuste.getProductoId() != null) {
            entity.setProducto(entityManager.getReference(
                    ProductoJpaEntity.class,
                    ajuste.getProductoId()
            ));
        }

        if (ajuste.getUsuarioId() != null) {
            entity.setUsuario(entityManager.getReference(
                    UsuarioJpaEntity.class,
                    ajuste.getUsuarioId()
            ));
        }

        entity.setCantidadAnterior(ajuste.getCantidadAnterior());
        entity.setDiferencia(ajuste.getDiferencia());
        entity.setCantidadNueva(ajuste.getCantidadNueva());
        entity.setFechaAjuste(ajuste.getFechaAjuste());

        AjusteInventarioJpaEntity guardado = repository.save(entity);

        return toDomain(guardado);
    }

    @Override
    public List<AjusteInventario> findByTiendaId(Long tiendaId) {
        return repository.findByTienda_Id(tiendaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private AjusteInventario toDomain(
            AjusteInventarioJpaEntity entity) {

        AjusteInventario ajuste = new AjusteInventario();

        ajuste.setId(entity.getId());
        ajuste.setTiendaId(entity.getTiendaId());
        ajuste.setProductoId(entity.getProductoId());
        ajuste.setCantidadAnterior(entity.getCantidadAnterior());
        ajuste.setDiferencia(entity.getDiferencia());
        ajuste.setCantidadNueva(entity.getCantidadNueva());
        ajuste.setUsuarioId(entity.getUsuarioId());
        ajuste.setFechaAjuste(entity.getFechaAjuste());

        return ajuste;
    }
}
