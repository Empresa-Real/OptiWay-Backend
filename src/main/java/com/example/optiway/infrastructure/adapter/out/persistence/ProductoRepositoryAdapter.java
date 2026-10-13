
package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.domain.model.Producto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository productoJpaRepository;

    public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository) {
        this.productoJpaRepository = productoJpaRepository;
    }

    @Override
    public Producto save(Producto producto) {

        ProductoJpaEntity entity;

        if (producto.getId() != null) {
            entity = productoJpaRepository.findById(producto.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "El producto no existe"));
        } else {
            entity = new ProductoJpaEntity();
        }

        entity.setNombre(producto.getNombre());
        entity.setCategoria(producto.getCategoria());
        entity.setActivo(producto.isActivo());

        return toDomain(productoJpaRepository.save(entity));
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return productoJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Producto> findByNombre(String nombre) {
        return productoJpaRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Producto> findByCategoria(String categoria) {
        return productoJpaRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Producto toDomain(ProductoJpaEntity entity) {
        boolean activo = entity.getActivo() == null || entity.getActivo();

        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getCategoria(),
                activo
        );
    }
}
