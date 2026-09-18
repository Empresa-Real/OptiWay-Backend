package com.example.optiway.domain.model;

import java.util.UUID;

public class Usuario {
    private Long id;
    private UUID authUserId;
    private String nombre;
    private String email;
    private Rol rol;
    private UUID creadoPor;

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String email, Rol rol, UUID creadoPor) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.creadoPor = creadoPor; // UUID del Admin que creo el usuario de la tabal Users
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(UUID authUserId) {
        this.authUserId = authUserId;
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

    public UUID getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(UUID creadoPor) {
        this.creadoPor = creadoPor;
    }
}
