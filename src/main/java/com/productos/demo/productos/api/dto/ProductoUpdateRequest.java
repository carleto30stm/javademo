package com.productos.demo.productos.api.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record ProductoUpdateRequest(
    @Pattern(regexp = "^\\d{13}$", message = "El código EAN debe contener exactamente 13 dígitos")
    @JsonProperty("codigo_ean")
    String codigoEan,

    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String nombre,

    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    String descripcion,

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @JsonProperty("precio")
    BigDecimal precio,

    @Min(value = 0, message = "El stock no puede ser negativo")
    @JsonProperty("stock")
    Integer stock
) {
}
