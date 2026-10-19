package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.EditarProductoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Schema(description = "Petición para modificar los atributos comerciales de un producto existente")
public class EditarProductoRequest {

    @Schema(description = "Nuevo nombre comercial del producto", example = "Arroz Diana 1kg Premium")
    private String nombre;

    @Schema(description = "Nueva categoría del producto", example = "Abarrotes")
    private String categoria;

    @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor que cero")
    @Schema(description = "Nuevo precio de venta (debe ser mayor que cero)", example = "4800.00")
    private BigDecimal precio;

    @Schema(description = "Nueva unidad de medida", example = "KG")
    private String unidadMedida;

    public EditarProductoRequest() {}

    public EditarProductoRequest(String nombre, String categoria, BigDecimal precio, String unidadMedida) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public EditarProductoCommand toCommand(Long id) {
        return new EditarProductoCommand(id, this.nombre, this.categoria, this.precio, this.unidadMedida);
    }
}
