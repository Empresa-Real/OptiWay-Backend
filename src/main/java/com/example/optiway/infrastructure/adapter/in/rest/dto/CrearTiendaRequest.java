package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.domain.model.Tienda;

public class CrearTiendaRequest {
    private String nombre;
    private String direccion;
    private String ciudad;
    private Long encargadoId;

    public CrearTiendaRequest() {}

    public CrearTiendaRequest(String nombre, String direccion, String ciudad, Long encargadoId) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.encargadoId = encargadoId;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Long getEncargadoId() {
        return encargadoId;
    }

    public void setEncargadoId(Long encargadoId) {
        this.encargadoId = encargadoId;
    }

    public Tienda toDomain() {
        Tienda tienda = new Tienda();
        tienda.setNombre(this.nombre);
        tienda.setDireccion(this.direccion);
        tienda.setCiudad(this.ciudad);
        tienda.setEncargadoId(this.encargadoId);
        return tienda;
    }
}
