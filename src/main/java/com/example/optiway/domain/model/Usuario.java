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

    /**
     * Verifica si este usuario es Administrador.
     * El rol ADMINISTRADOR es el único que puede gestionar usuarios y roles.
     *
     * @return true si el rol es ADMINISTRADOR
     */
    public boolean esAdministrador() {
        return this.rol == Rol.ADMINISTRADOR;
    }

    /**
     * Verifica si este usuario puede gestionar el dominio logístico (catálogo, tiendas,
     * centros, productos, inventario, ventas e ingresos) sin restricción de ubicación.
     * Pueden hacerlo ADMINISTRADOR y PLANIFICADOR; los encargados quedan limitados a
     * las ubicaciones que tienen asignadas.
     *
     * @return true si el usuario tiene visión y gestión logística global
     */
    public boolean puedeGestionarTodo() {
        return esAdministrador() || this.rol == Rol.PLANIFICADOR;
    }

    /**
     * Verifica si este usuario es Encargado de Tienda (por rol).
     * Para comprobar además que está asignado a una tienda concreta, usa
     * {@link #esEncargadoDe(Tienda)}.
     *
     * @return true si el rol es ENCARGADO_TIENDA
     */
    public boolean esEncargadoTienda() {
        return this.rol == Rol.ENCARGADO_TIENDA;
    }

    /**
     * Verifica si este usuario es Encargado de Centro de Distribución (por rol).
     * Para comprobar además que está asignado a un CD concreto, usa
     * {@link #esEncargadoDe(CentroDistribucion)}.
     *
     * @return true si el rol es ENCARGADO_CD
     */
    public boolean esEncargadoCD() {
        return this.rol == Rol.ENCARGADO_CD;
    }

    /**
     * Verifica si este usuario puede operar sobre una tienda dada: o puede gestionar
     * todo el dominio logístico, o es el Encargado de Tienda asignado a esa tienda.
     *
     * @param tienda la tienda sobre la que se quiere operar
     * @return true si el usuario tiene permiso para operar la tienda
     */
    public boolean puedeOperarTienda(Tienda tienda) {
        return puedeGestionarTodo() || esEncargadoDe(tienda);
    }

    /**
     * Verifica si este usuario puede operar sobre un Centro de Distribución dado: o puede
     * gestionar todo el dominio logístico, o es el Encargado de CD asignado a ese centro.
     *
     * @param centroDistribucion el centro de distribución sobre el que se quiere operar
     * @return true si el usuario tiene permiso para operar el centro
     */
    public boolean puedeOperarCentroDistribucion(CentroDistribucion centroDistribucion) {
        return puedeGestionarTodo() || esEncargadoDe(centroDistribucion);
    }

    /**
     * Verifica si este usuario es el Encargado de Tienda asignado a la tienda dada.
     * Comprueba rol y asignación: no basta con ser encargado, hay que estar asignado.
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
     * Verifica si este usuario es el Encargado de CD asignado al centro dado.
     * Comprueba rol y asignación: no basta con ser encargado, hay que estar asignado.
     *
     * @param centroDistribucion el centro de distribución a verificar
     * @return true si es Encargado de CD y su ID coincide con el encargado del centro
     */
    public boolean esEncargadoDe(CentroDistribucion centroDistribucion) {
        if (centroDistribucion == null || this.id == null || !esEncargadoCD()) {
            return false;
        }
        return this.id.equals(centroDistribucion.getEncargadoId());
    }

    /**
     * Verifica si este usuario puede recibir la asignación de una ubicación (tienda o CD).
     * Solo los encargados se asignan a ubicaciones; Administrador y Planificador gestionan
     * todo el catálogo y no se vinculan a una ubicación concreta.
     *
     * @return true si el rol admite una asignación de ubicación
     */
    public boolean admiteAsignacionDeUbicacion() {
        return esEncargadoTienda() || esEncargadoCD();
    }
}
