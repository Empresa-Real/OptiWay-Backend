
package com.example.optiway.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "ajustes_inventario")
public class AjusteInventarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tienda_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ajustes_tienda")
    )
    private TiendaJpaEntity tienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "producto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ajustes_producto")
    )
    private ProductoJpaEntity producto;

    @Column(name = "cantidad_anterior", nullable = false)
    private Integer cantidadAnterior;

    @Column(nullable = false)
    private Integer diferencia;

    @Column(name = "cantidad_nueva", nullable = false)
    private Integer cantidadNueva;

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_id",
            foreignKey = @ForeignKey(name = "fk_ajustes_usuario")
    )
    private UsuarioJpaEntity usuario;

    @Column(name = "fecha_ajuste", nullable = false)
    private LocalDateTime fechaAjuste;

    public AjusteInventarioJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TiendaJpaEntity getTienda() {
        return tienda;
    }

    public void setTienda(TiendaJpaEntity tienda) {
        this.tienda = tienda;
    }

    public ProductoJpaEntity getProducto() {
        return producto;
    }

    public void setProducto(ProductoJpaEntity producto) {
        this.producto = producto;
    }

    public Long getTiendaId() {
        return tienda != null ? tienda.getId() : null;
    }

    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
    }

    public Integer getCantidadAnterior() {
        return cantidadAnterior;
    }

    public void setCantidadAnterior(Integer cantidadAnterior) {
        this.cantidadAnterior = cantidadAnterior;
    }

    public Integer getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(Integer diferencia) {
        this.diferencia = diferencia;
    }

    public Integer getCantidadNueva() {
        return cantidadNueva;
    }

    public void setCantidadNueva(Integer cantidadNueva) {
        this.cantidadNueva = cantidadNueva;
    }

    public UsuarioJpaEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioJpaEntity usuario) {
        this.usuario = usuario;
    }

    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }

    public LocalDateTime getFechaAjuste() {
        return fechaAjuste;
    }

    public void setFechaAjuste(LocalDateTime fechaAjuste) {
        this.fechaAjuste = fechaAjuste;
    }
}
