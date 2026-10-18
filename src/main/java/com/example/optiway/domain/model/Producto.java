package com.example.optiway.domain.model;

import java.math.BigDecimal;

public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private String identificador;
    private BigDecimal precio;
    private String unidadMedida;
    private String estado;
    private Boolean activo = true;

    public Producto() {
    }

    public Producto(Long id, String nombre, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.activo = true;
        this.estado = "activo";
    }

    public Producto(Long id, String identificador, String nombre, String categoria,
                    BigDecimal precio, String unidadMedida, String estado) {
        this.id = id;
        this.identificador = identificador;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
        this.estado = estado;
        this.activo = !"inactivo".equalsIgnoreCase(estado);
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
        this.estado = this.activo ? "activo" : "inactivo";
    }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public String getEstado() { return estado != null ? estado : (isActivo() ? "activo" : "inactivo"); }
    public void setEstado(String estado) {
        this.estado = estado;
        this.activo = !"inactivo".equalsIgnoreCase(estado);
    }
    public Boolean isActivo() { return activo != null ? activo : true; }
    public Boolean getActivo() { return isActivo(); }
    public void setActivo(Boolean activo) {
        this.activo = activo != null ? activo : true;
        this.estado = this.activo ? "activo" : "inactivo";
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