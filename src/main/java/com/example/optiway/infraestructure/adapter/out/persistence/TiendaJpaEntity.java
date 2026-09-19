package com.example.optiway.infraestructure.adapter.out.persistence;

import jakarta.persistence.*;

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

    @Column(nullable = false)
    private String estado;

    public TiendaJpaEntity() {}

    public TiendaJpaEntity(Long id, String codigo, String nombre, String direccion, String ciudad, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estado = estado;
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

    public String getEstado() { return estado;}
    public void setEstado(String estado) { this.estado = estado;}
}