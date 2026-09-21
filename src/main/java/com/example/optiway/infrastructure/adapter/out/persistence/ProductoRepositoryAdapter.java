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

        ProductoJpaEntity entity = new ProductoJpaEntity();

        entity.setNombre(producto.getNombre());
        entity.setCategoria(producto.getCategoria());

        ProductoJpaEntity savedEntity = productoJpaRepository.save(entity);

        Producto savedProducto = new Producto();

        savedProducto.setId(savedEntity.getId());
        savedProducto.setNombre(savedEntity.getNombre());
        savedProducto.setCategoria(savedEntity.getCategoria());

        return savedProducto;
    }

    @Override
    public Optional<Producto> findById(Long id) {

        return productoJpaRepository.findById(id)
                .map(entity -> {

                    Producto producto = new Producto();

                    producto.setId(entity.getId());
                    producto.setNombre(entity.getNombre());
                    producto.setCategoria(entity.getCategoria());

                    return producto;
                });
    }

    @Override
    public List<Producto> findByNombre(String nombre) {

        return productoJpaRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(entity -> {

                    Producto producto = new Producto();

                    producto.setId(entity.getId());
                    producto.setNombre(entity.getNombre());
                    producto.setCategoria(entity.getCategoria());

                    return producto;
                })
                .toList();
    }

    @Override
    public List<Producto> findByCategoria(String categoria) {

        return productoJpaRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(entity -> {

                    Producto producto = new Producto();

                    producto.setId(entity.getId());
                    producto.setNombre(entity.getNombre());
                    producto.setCategoria(entity.getCategoria());

                    return producto;
                })
                .toList();
    }
}