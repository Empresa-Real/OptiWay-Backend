package com.example.optiway.domain.model;

import java.time.LocalDateTime;

public class AjusteInventario {

    private Long id;
    private Long tiendaId;
    private Long centroDistribucionId;
    private Long productoId;
    private Integer cantidadAnterior;
    private Integer diferencia;
    private Integer cantidadNueva;
    private Long usuarioId;
    private LocalDateTime fechaAjuste;

    public AjusteInventario() {
    }

    public AjusteInventario(
            Long id,
            Long tiendaId,
            Long centroDistribucionId,
            Long productoId,
            Integer cantidadAnterior,
            Integer diferencia,
            Integer cantidadNueva,
            Long usuarioId,
            LocalDateTime fechaAjuste
    ) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.centroDistribucionId = centroDistribucionId;
        this.productoId = productoId;
        this.cantidadAnterior = cantidadAnterior;
        this.diferencia = diferencia;
        this.cantidadNueva = cantidadNueva;
        this.usuarioId = usuarioId;
        this.fechaAjuste = fechaAjuste;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTiendaId() {
        return tiendaId;
    }

    public void setTiendaId(Long tiendaId) {
        this.tiendaId = tiendaId;
    }

    public Long getCentroDistribucionId() {
        return centroDistribucionId;
    }

    public void setCentroDistribucionId(Long centroDistribucionId) {
        this.centroDistribucionId = centroDistribucionId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaAjuste() {
        return fechaAjuste;
    }

    public void setFechaAjuste(LocalDateTime fechaAjuste) {
        this.fechaAjuste = fechaAjuste;
    }
}
