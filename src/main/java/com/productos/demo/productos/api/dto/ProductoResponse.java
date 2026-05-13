package com.productos.demo.productos.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductoResponse(
    @JsonProperty("producto_id")
    Long productoId,

    @JsonProperty("codigo_ean")
    String codigoEan,

    String nombre,

    String descripcion,

    BigDecimal precio,

    Integer stock,

    @JsonProperty("created_at")
    Instant createdAt,

    @JsonProperty("updated_at")
    Instant updatedAt
) {
}
