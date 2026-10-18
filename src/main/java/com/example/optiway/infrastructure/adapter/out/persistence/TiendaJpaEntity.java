package com.example.optiway.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tiendas")
public class TiendaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean activo = true;

    @Column(name = "encargado_id")
    private Long encargadoId;

    public TiendaJpaEntity() {}

    public TiendaJpaEntity(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo, Long encargadoId) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.activo = activo != null ? activo : true;
        this.encargadoId = encargadoId;
    }

    public Long getId() { return id;}
    public void setId(Long id) { this.id = id;}

    public String getCodigo() { return codigo;}
    public void setCodigo(String codigo) { this.codigo = codigo;}

    public String getNombre() { return nombre;}
    public void setNombre(String nombre) { this.nombre = nombre;}

    public String getDireccion() { return direccion;}
    public void setDireccion(String direccion) { this.direccion = direccion;}

    public String getCiudad() { return ciudad;}
    public void setCiudad(String ciudad) { this.ciudad = ciudad;}

    public Boolean getActivo() { return activo != null ? activo : true; }
    public void setActivo(Boolean activo) { this.activo = activo != null ? activo : true; }

    public Long getEncargadoId() { return encargadoId; }
    public void setEncargadoId(Long encargadoId) { this.encargadoId = encargadoId; }
}