package com.example.optiway.domain.model;

import java.util.UUID;

import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Tienda;

public class Usuario {
    private Long id;
    private UUID authUserId;
    private String nombre;
    private String email;
    private Rol rol;
    private UUID creadoPor;

    public Usuario() {
    }

    public Usuario(Long id, UUID authUserId, String nombre, String email, Rol rol, UUID creadoPor) {
        this.id = id;
        this.authUserId = authUserId;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.creadoPor = creadoPor;
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

    public boolean esAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }

    public boolean esPlanificador() {
        return this.rol == Rol.PLANIFICADOR;
    }

    public boolean esEncargadoTienda() {
        return this.rol == Rol.ENCARGADO_TIENDA;
    }

    public boolean esEncargadoCD() {
        return this.rol == Rol.ENCARGADO_CD;
    }

    public boolean esEncargado() {
        return esEncargadoTienda() || esEncargadoCD();
    }

    /**
     * Verifica si este usuario puede operar sobre una tienda dada.
     * Puede hacerlo si es Administrador, o si es Encargado de tienda y está asignado a esa tienda.
     *
     * @param tienda la tienda sobre la que se quiere operar
     * @return true si el usuario tiene permiso para operar la tienda
     */
    public boolean puedeOperarTienda(Tienda tienda) {
        if (tienda == null) {
            return false;
        }
        return esAdministrador() || esEncargadoDe(tienda);
    }

    /**
     * Verifica si este usuario es el Encargado de Tienda asignado a la tienda dada.
     *
     * @param tienda la tienda a verificar
     * @return true si es Encargado de Tienda y su ID coincide con el encargado de la tienda
     */
    public boolean esEncargadoDe(Tienda tienda) {
        if (tienda == null || this.id == null || !esEncargadoTienda()) {
            return false;
        }
        return this.id.equals(tienda.getEncargadoId());
    }

    /**
     * Verifica si este usuario es el Encargado de Centro de Distribución asignado al CD dado.
     *
     * @param centroDistribucion el centro de distribución a verificar
     * @return true si es Encargado de CD y su ID coincide con el encargado del CD
     */
    public boolean esEncargadoDe(CentroDistribucion centroDistribucion) {
        if (centroDistribucion == null || this.id == null || !esEncargadoCD()) {
            return false;
        }
        return this.id.equals(centroDistribucion.getEncargadoId());
    }

    /**
     * Verifica si este usuario puede ver todo el catálogo logístico (tiendas, centros, etc.).
     * Puede hacerlo si es Administrador o Planificador.
     *
     * @return true si puede ver todo el catálogo logístico
     */
    public boolean puedeVerTodoElCatalogoLogistico() {
        return esAdministrador() || esPlanificador();
    }

    private boolean tieneAccesoLogisticoOEncargado(boolean esEncargado) {
        return puedeVerTodoElCatalogoLogistico() || esEncargado;
    }

    /**
     * Verifica si este usuario puede consultar el inventario de una tienda.
     * Puede hacerlo si tiene visión logística global o es el Encargado asignado a esa tienda.
     *
     * @param tienda la tienda a consultar
     * @return true si tiene permisos para consultar
     */
    public boolean puedeConsultarTienda(Tienda tienda) {
        return tienda != null && tieneAccesoLogisticoOEncargado(esEncargadoDe(tienda));
    }

    /**
     * Verifica si este usuario puede operar o consultar un Centro de Distribución.
     * Puede hacerlo si tiene visión logística global o es el Encargado asignado a ese CD.
     *
     * @param centroDistribucion el centro de distribución a verificar
     * @return true si tiene permisos
     */
    public boolean puedeOperarCentroDistribucion(CentroDistribucion centroDistribucion) {
        return centroDistribucion != null && tieneAccesoLogisticoOEncargado(esEncargadoDe(centroDistribucion));
    }
}
