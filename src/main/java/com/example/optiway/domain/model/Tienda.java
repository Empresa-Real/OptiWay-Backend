package com.example.optiway.domain.model;

public class Tienda {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private String ciudad;
    private Boolean activo = true;
    private Long encargadoId;

    public Tienda() {}

    public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.activo = activo != null ? activo : true;
    }

    public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, String estado) {
        this(id, codigo, nombre, direccion, ciudad, !"Inactiva".equalsIgnoreCase(estado));
    }

    public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo, Long encargadoId) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.activo = activo != null ? activo : true;
        this.encargadoId = encargadoId;
    }

    public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, String estado, Long encargadoId) {
        this(id, codigo, nombre, direccion, ciudad, !"Inactiva".equalsIgnoreCase(estado), encargadoId);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Boolean getActivo() { return activo != null ? activo : true; }
    public void setActivo(Boolean activo) { this.activo = activo != null ? activo : true; }

    public String getEstado() { return activo != null && activo ? "Activa" : "Inactiva"; }
    public void setEstado(String estado) { this.activo = "Activa".equalsIgnoreCase(estado); }

    public Long getEncargadoId() { return encargadoId; }
    public void setEncargadoId(Long encargadoId) { this.encargadoId = encargadoId; }
}
