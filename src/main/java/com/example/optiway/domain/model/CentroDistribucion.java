package com.example.optiway.domain.model;

import java.util.List;

public class CentroDistribucion {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private Integer capacidad;
    private Long encargadoId;
    private List<Long> tiendasAbastecidasIds; // IDs de las tiendas asociadas a su zona de cobertura

    public CentroDistribucion() {}

    public CentroDistribucion(Long id, String codigo, String nombre, String direccion, Integer capacidad, List<Long> tiendasAbastecidasIds) {
        this(id, codigo, nombre, direccion, capacidad, null, tiendasAbastecidasIds);
    }

    public CentroDistribucion(Long id, String codigo, String nombre, String direccion, Integer capacidad, Long encargadoId, List<Long> tiendasAbastecidasIds) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidad = capacidad;
        this.encargadoId = encargadoId;
        this.tiendasAbastecidasIds = tiendasAbastecidasIds;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Long getEncargadoId() { return encargadoId; }
    public void setEncargadoId(Long encargadoId) { this.encargadoId = encargadoId; }

    public List<Long> getTiendasAbastecidasIds() { return tiendasAbastecidasIds; }
    public void setTiendasAbastecidasIds(List<Long> tiendasAbastecidasIds) { this.tiendasAbastecidasIds = tiendasAbastecidasIds; }
}