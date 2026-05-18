package com.productos.demo.exchange.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record ExchangeRateResponse(

    @JsonProperty("base")
    String base,

    @JsonProperty("rates")
    Map<String, BigDecimal> rates,

    @JsonProperty("last_updated")
    Instant lastUpdated
) {
}
