package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearTiendaCommand;
import jakarta.validation.constraints.NotBlank;

public class CrearTiendaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "La ciudad es obligatoria")
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

    public CrearTiendaCommand toCommand() {
        return new CrearTiendaCommand(this.nombre, this.direccion, this.ciudad, this.encargadoId);
    }
}
