package com.example.optiway.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class ProductoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    @Column(unique = true, length = 100)
    private String identificador;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean activo = true;

    public ProductoJpaEntity() {
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Boolean getActivo() { return activo != null ? activo : true; }
    public void setActivo(Boolean activo) {
        this.activo = activo != null ? activo : true;
    }
}