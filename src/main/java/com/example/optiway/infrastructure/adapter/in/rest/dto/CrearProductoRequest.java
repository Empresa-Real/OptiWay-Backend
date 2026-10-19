package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearProductoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Schema(description = "Petición para registrar un nuevo producto en el catálogo")
public class CrearProductoRequest {

    @NotBlank(message = "El identificador es obligatorio")
    @Schema(description = "Código o identificador único comercial del producto", example = "PROD-001")
    private String identificador;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre comercial del producto", example = "Arroz Diana 1kg")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    @Schema(description = "Categoría o clasificación del producto", example = "Granos")
    private String categoria;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Schema(description = "Precio unitario de venta", example = "4500.00")
    private BigDecimal precio;

    @Schema(description = "Unidad de medida (ej: KG, UNIDAD, LITRO)", example = "KG")
    private String unidadMedida;

    public CrearProductoRequest() {}

    public CrearProductoRequest(String identificador, String nombre, String categoria,
                                BigDecimal precio, String unidadMedida) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public CrearProductoCommand toCommand() {
        return new CrearProductoCommand(this.identificador, this.nombre, this.categoria, this.precio, this.unidadMedida);
    }
}
