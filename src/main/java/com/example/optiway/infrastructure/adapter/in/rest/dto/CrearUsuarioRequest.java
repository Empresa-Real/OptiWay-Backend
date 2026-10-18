package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;

public class CrearUsuarioRequest {
    private String nombre;
    private String email;
    private Rol rol;

    public CrearUsuarioRequest() {}

    public CrearUsuarioRequest(String nombre, String email, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
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

    public Usuario toDomain() {
        Usuario usuario = new Usuario();
        usuario.setNombre(this.nombre);
        usuario.setEmail(this.email);
        usuario.setRol(this.rol);
        return usuario;
    }
}
