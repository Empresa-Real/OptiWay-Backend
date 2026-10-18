package com.example.optiway.domain.model;

import java.math.BigDecimal;

public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private String identificador;
    private BigDecimal precio;
    private String unidadMedida;
    private Boolean activo = true;

    public Producto() {
    }


    public Producto(Long id, String identificador, String nombre, String categoria,
                    BigDecimal precio, String unidadMedida, Boolean activo) {
        this.id = id;
        this.identificador = identificador;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
        this.activo = activo != null ? activo : true;
    }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public Boolean isActivo() { return activo != null ? activo : true; }
    public Boolean getActivo() { return isActivo(); }
    public void setActivo(Boolean activo) {
        this.activo = activo != null ? activo : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
