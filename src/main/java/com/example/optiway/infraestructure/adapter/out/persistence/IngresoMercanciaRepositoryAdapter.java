package com.example.optiway.infraestructure.adapter.out.persistence;
import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.domain.model.IngresoMercancia;
import org.springframework.stereotype.Component;

@Component
public class IngresoMercanciaRepositoryAdapter
        implements IngresoMercanciaRepositoryPort {

    private final IngresoMercanciaJpaRepository repository;

    public IngresoMercanciaRepositoryAdapter(
            com.example.optiway.infraestructure.adapter.out.persistence.IngresoMercanciaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public IngresoMercancia guardar(IngresoMercancia ingreso) {

        IngresoMercanciaJpaEntity entity =
                new IngresoMercanciaJpaEntity();

        entity.setCentroDistribucionId(
                ingreso.getCentroDistribucionId());

        entity.setProductoId(
                ingreso.getProductoId());

        entity.setCantidad(
                ingreso.getCantidad());

        entity.setOrigen(
                ingreso.getOrigen());

        entity.setUsuarioId(
                ingreso.getUsuarioId());

        entity.setFechaIngreso(
                ingreso.getFechaIngreso());

        com.example.optiway.infraestructure.adapter.out.persistence.IngresoMercanciaJpaEntity saved =
                repository.save(entity);

        IngresoMercancia resultado =
                new IngresoMercancia();

        resultado.setId(saved.getId());
        resultado.setCentroDistribucionId(
                saved.getCentroDistribucionId());

        resultado.setProductoId(
                saved.getProductoId());

        resultado.setCantidad(
                saved.getCantidad());

        resultado.setOrigen(
                saved.getOrigen());

        resultado.setUsuarioId(
                saved.getUsuarioId());

        resultado.setFechaIngreso(
                saved.getFechaIngreso());

        return resultado;
    }
}