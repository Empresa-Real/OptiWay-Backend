package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.service.ProductoDuplicadoException;
import com.example.optiway.domain.model.Producto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository productoJpaRepository;
    private final ProductoEdicionJpaRepository productoEdicionJpaRepository;

    public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository,
                                     ProductoEdicionJpaRepository productoEdicionJpaRepository) {
        this.productoJpaRepository = productoJpaRepository;
        this.productoEdicionJpaRepository = productoEdicionJpaRepository;
    }

    @Override
    public Producto save(Producto producto) {

        ProductoJpaEntity entity;
        if (producto.getId() != null) {
            entity = productoJpaRepository.findById(producto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + producto.getId()));
        } else {
            entity = new ProductoJpaEntity();
        }

        entity.setNombre(producto.getNombre());
        entity.setCategoria(producto.getCategoria());
        entity.setIdentificador(producto.getIdentificador());
        entity.setPrecio(producto.getPrecio());
        entity.setUnidadMedida(producto.getUnidadMedida());
        entity.setActivo(producto.isActivo());

        try {
            ProductoJpaEntity savedEntity = productoJpaRepository.save(entity);

            Producto savedProducto = new Producto();

            savedProducto.setId(savedEntity.getId());
            savedProducto.setNombre(savedEntity.getNombre());
            savedProducto.setCategoria(savedEntity.getCategoria());
            copiarDatosCatalogo(savedEntity, savedProducto);

            return savedProducto;
        } catch (DataIntegrityViolationException e) {
            throw new ProductoDuplicadoException("Ya existe un producto con ese identificador.");
        }
    }

    @Override
    @Transactional
    public Producto update(Producto cambios, UUID authUserId) {
        ProductoJpaEntity entity = productoJpaRepository.findById(cambios.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + cambios.getId()));

        entity.setNombre(cambios.getNombre());
        entity.setCategoria(cambios.getCategoria());
        entity.setPrecio(cambios.getPrecio());
        entity.setUnidadMedida(cambios.getUnidadMedida());
        ProductoJpaEntity savedEntity = productoJpaRepository.save(entity);
        productoEdicionJpaRepository.save(new ProductoEdicionJpaEntity(savedEntity.getId(), authUserId));
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByIdentificador(String identificador) {
        return productoJpaRepository.existsByIdentificadorIgnoreCase(identificador);
    }

    @Override
    public List<Producto> findAll() {
        return productoJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Producto> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productoJpaRepository.findAllById(ids).stream().map(this::toDomain).toList();
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
        Producto producto = new Producto();
        producto.setId(entity.getId());
        producto.setNombre(entity.getNombre());
        producto.setCategoria(entity.getCategoria());
        copiarDatosCatalogo(entity, producto);
        return producto;
    }

    private void copiarDatosCatalogo(ProductoJpaEntity entity, Producto producto) {
        producto.setIdentificador(entity.getIdentificador());
        producto.setPrecio(entity.getPrecio());
        producto.setUnidadMedida(entity.getUnidadMedida());
        producto.setActivo(entity.getActivo());
    }
}
