package com.example.optiway.domain.model;

public class Tienda {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String estado;
    private Long encargadoId;
    public Tienda() {}

    public Tienda(Long id, String codigo, String nombre, String direccion,
                  String ciudad, String estado, Long encargadoId) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = estado;
        this.encargadoId = encargadoId;
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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getEncargadoId() { return encargadoId; }
    public void setEncargadoId(Long encargadoId) {
        this.encargadoId = encargadoId;
    }
}