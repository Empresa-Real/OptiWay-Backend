package com.example.optiway.domain.model;

public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private String creadoPor;

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String email, Rol rol, String creadoPor) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.creadoPor = creadoPor; // UUID del Admin que creo el usuario
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
}
