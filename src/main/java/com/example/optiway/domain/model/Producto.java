
package com.example.optiway.domain.model;

public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private boolean activo = true;

    public Producto() {
    }

    public Producto(Long id, String nombre, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.activo = true;
    }

    public Producto(Long id, String nombre, String categoria, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.activo = activo;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
