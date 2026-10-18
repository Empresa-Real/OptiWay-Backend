package com.example.optiway.domain.model;

public class Inventario {

    private Long id;
    private Long tiendaId;                 // Nullable
    private Long centroDistribucionId;     // Nullable
    private Long productoId;
    private Integer cantidadActual;
    private Integer stockMinimo;

    private String nombreProducto;
    private String categoriaProducto;
    private boolean stockBajo;

    public Inventario() {}

    public Inventario(Long id, Long tiendaId, Long centroDistribucionId, Long productoId, Integer cantidadActual, Integer stockMinimo) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.centroDistribucionId = centroDistribucionId;
        this.productoId = productoId;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
        validarUbicacionExclusiva();
    }

    public void validarUbicacionExclusiva() {
        boolean tieneTienda = (this.tiendaId != null);
        boolean tieneCd = (this.centroDistribucionId != null);

        if (tieneTienda && tieneCd) {
            throw new IllegalArgumentException(
                    "Un registro de inventario no puede pertenecer a una tienda y a un centro de distribución al mismo tiempo.");
        }
        if (!tieneTienda && !tieneCd) {
            throw new IllegalArgumentException(
                    "El registro de inventario debe estar asociado obligatoriamente a una tienda o a un centro de distribución.");
        }
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getTiendaId() {return tiendaId;}
    public void setTiendaId(Long tiendaId) {this.tiendaId = tiendaId;}

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

    /**
     * Calcula si el nivel de existencias actual está por debajo del umbral mínimo requerido.
     *
     * @return true si la cantidad actual es menor que el stock mínimo configurado
     */
    public boolean calcularStockBajo() {
        return this.cantidadActual != null && this.stockMinimo != null && this.cantidadActual < this.stockMinimo;
    }

    /**
     * Enriquece el registro de inventario con los atributos del producto asociado
     * y recalcula automáticamente el estado de stock bajo.
     *
     * @param producto producto del catálogo asociado a este inventario
     * @return esta misma instancia para encadenamiento fluido
     */
    public Inventario enriquecerConProducto(Producto producto) {
        if (producto != null) {
            this.nombreProducto = producto.getNombre();
            this.categoriaProducto = producto.getCategoria();
        }
        this.stockBajo = calcularStockBajo();
        return this;
    }

    /**
     * Incrementa la cantidad de existencias en el inventario y actualiza el estado de stock bajo.
     *
     * @param cantidad cantidad a ingresar (debe ser mayor que cero)
     */
    public void agregarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        int actual = this.cantidadActual != null ? this.cantidadActual : 0;
        this.cantidadActual = actual + cantidad;
        this.stockBajo = calcularStockBajo();
    }

    /**
     * Verifica si el inventario cuenta con suficiente stock disponible para la cantidad solicitada.
     *
     * @param cantidad cantidad requerida
     * @return true si el stock actual es mayor o igual a la cantidad
     */
    public boolean tieneStockDisponible(int cantidad) {
        int actual = this.cantidadActual != null ? this.cantidadActual : 0;
        return actual >= cantidad;
    }

    /**
     * Descuenta existencias del inventario tras validar que la cantidad sea positiva.
     * Actualiza automáticamente el estado de stock bajo.
     *
     * @param cantidad cantidad a descontar (debe ser mayor que cero)
     */
    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de venta debe ser mayor a cero.");
        }
        int actual = this.cantidadActual != null ? this.cantidadActual : 0;
        if (actual < cantidad) {
            throw new IllegalStateException("Stock insuficiente para realizar el descuento: disponible " + actual + ", solicitado " + cantidad);
        }
        this.cantidadActual = actual - cantidad;
        this.stockBajo = calcularStockBajo();
    }
}