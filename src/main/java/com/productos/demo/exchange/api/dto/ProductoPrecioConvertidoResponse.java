package com.productos.demo.exchange.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductoPrecioConvertidoResponse(

    @JsonProperty("producto_id")
    Long productoId,

    String nombre,

    @JsonProperty("precio_original")
    BigDecimal precioOriginal,

    @JsonProperty("moneda_original")
    String monedaOriginal,

    @JsonProperty("precio_convertido")
    BigDecimal precioConvertido,

    String moneda,

    BigDecimal tasa
) {
}
