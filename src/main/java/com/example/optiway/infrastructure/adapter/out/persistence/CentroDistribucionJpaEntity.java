package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "centros_distribucion")
public class CentroDistribucionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private Integer capacidad;

    @ElementCollection
    @CollectionTable(
            name = "centro_distribucion_tiendas",
            joinColumns = @JoinColumn(name = "centro_id")
    )
    @Column(name = "tienda_id")
    private List<Long> tiendasAbastecidasIds;

    public CentroDistribucionJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public List<Long> getTiendasAbastecidasIds() {
        return tiendasAbastecidasIds;
    }

    public void setTiendasAbastecidasIds(
            List<Long> tiendasAbastecidasIds) {
        this.tiendasAbastecidasIds = tiendasAbastecidasIds;
    }
}