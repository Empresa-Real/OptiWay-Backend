package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearCentroDistribucionCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class CrearCentroDistribucionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que cero")
    private Integer capacidad;

    private Long encargadoId;

    private List<Long> tiendasAbastecidasIds;

    public CrearCentroDistribucionRequest() {}

    public CrearCentroDistribucionRequest(String nombre, String direccion, Integer capacidad,
                                          Long encargadoId, List<Long> tiendasAbastecidasIds) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidad = capacidad;
        this.encargadoId = encargadoId;
        this.tiendasAbastecidasIds = tiendasAbastecidasIds;
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

    public Long getEncargadoId() {
        return encargadoId;
    }

    public void setEncargadoId(Long encargadoId) {
        this.encargadoId = encargadoId;
    }

    public List<Long> getTiendasAbastecidasIds() {
        return tiendasAbastecidasIds;
    }

    public void setTiendasAbastecidasIds(List<Long> tiendasAbastecidasIds) {
        this.tiendasAbastecidasIds = tiendasAbastecidasIds;
    }

    public CrearCentroDistribucionCommand toCommand() {
        return new CrearCentroDistribucionCommand(this.nombre, this.direccion, this.capacidad,
                this.encargadoId, this.tiendasAbastecidasIds);
    }
}
