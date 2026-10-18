package com.example.optiway.domain.model;

import java.util.List;

public class Tienda {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private String ciudad;
    private Boolean activo = true;
    private Long encargadoId;
    private List<Long> centrosAbastecedoresIds = List.of();

    public Tienda() {}

    public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo, Long encargadoId) {
        this(id, codigo, nombre, direccion, ciudad, activo, encargadoId, List.of());
    }

    public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo, Long encargadoId, List<Long> centrosAbastecedoresIds) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.activo = activo != null ? activo : true;
        this.encargadoId = encargadoId;
        this.centrosAbastecedoresIds = centrosAbastecedoresIds != null ? centrosAbastecedoresIds : List.of();
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

    public Boolean isActivo() { return activo != null ? activo : true; }
    public Boolean getActivo() { return isActivo(); }
    public void setActivo(Boolean activo) { this.activo = activo != null ? activo : true; }

    public Long getEncargadoId() { return encargadoId; }
    public void setEncargadoId(Long encargadoId) { this.encargadoId = encargadoId; }

    public List<Long> getCentrosAbastecedoresIds() {
        return centrosAbastecedoresIds != null ? centrosAbastecedoresIds : List.of();
    }

    public void setCentrosAbastecedoresIds(List<Long> centrosAbastecedoresIds) {
        this.centrosAbastecedoresIds = centrosAbastecedoresIds != null ? centrosAbastecedoresIds : List.of();
    }
}
