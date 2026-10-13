package com.example.optiway.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;

public class EditarProductoRequest {
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private String unidadMedida;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
}
