package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "centros_distribucion")
public class CentroDistribucionJpaEntity {

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
    private Integer capacidad;

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encargado_id", foreignKey = @ForeignKey(name = "fk_centros_encargado"))
    private UsuarioJpaEntity encargado;

    @ManyToMany
    @JoinTable(
        name = "centro_distribucion_tiendas",
        joinColumns = @JoinColumn(name = "centro_id", foreignKey = @ForeignKey(name = "fk_cdt_centro")),
        inverseJoinColumns = @JoinColumn(name = "tienda_id", foreignKey = @ForeignKey(name = "fk_cdt_tienda"))
    )
    private List<TiendaJpaEntity> tiendasAbastecidas;

    public CentroDistribucionJpaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public UsuarioJpaEntity getEncargado() { return encargado; }
    public void setEncargado(UsuarioJpaEntity encargado) { this.encargado = encargado; }

    public Long getEncargadoId() { return encargado != null ? encargado.getId() : null; }

    public List<TiendaJpaEntity> getTiendasAbastecidas() { return tiendasAbastecidas; }
    public void setTiendasAbastecidas(List<TiendaJpaEntity> tiendasAbastecidas) { this.tiendasAbastecidas = tiendasAbastecidas; }
}
