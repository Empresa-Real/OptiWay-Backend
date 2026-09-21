package com.example.optiway.domain.model;

public class Inventario {

    private Long id;
    private Long tiendaId;
    private Long productoId;
    private Integer cantidadActual;
    private Integer stockMinimo;

    private String nombreProducto;
    private String categoriaProducto;
    private boolean stockBajo;

    public Inventario() {}

    public Inventario(Long id, Long tiendaId, Long productoId,
                      Integer cantidadActual, Integer stockMinimo) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
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

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(Integer cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getCategoriaProducto() {
        return categoriaProducto;
    }

    public void setCategoriaProducto(String categoriaProducto) {
        this.categoriaProducto = categoriaProducto;
    }

    public boolean isStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(boolean stockBajo) {
        this.stockBajo = stockBajo;
    }
}