package com.example.optiway.infrastructure.adapter.in.rest.dto;

import java.util.List;

public class CrearCentroDistribucionRequest {
    private String nombre;
    private String direccion;
    private Integer capacidad;
    private List<Long> tiendasAbastecidasIds;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public List<Long> getTiendasAbastecidasIds() { return tiendasAbastecidasIds; }
    public void setTiendasAbastecidasIds(List<Long> tiendasAbastecidasIds) { this.tiendasAbastecidasIds = tiendasAbastecidasIds; }
}
