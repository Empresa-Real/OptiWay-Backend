package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.domain.model.Producto;
import java.math.BigDecimal;

public class EditarProductoRequest {
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private String unidadMedida;

    public EditarProductoRequest() {}

    public EditarProductoRequest(String nombre, String categoria, BigDecimal precio, String unidadMedida) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public Producto toDomain() {
        Producto producto = new Producto();
        producto.setNombre(this.nombre);
        producto.setCategoria(this.categoria);
        producto.setPrecio(this.precio);
        producto.setUnidadMedida(this.unidadMedida);
        return producto;
    }
}
