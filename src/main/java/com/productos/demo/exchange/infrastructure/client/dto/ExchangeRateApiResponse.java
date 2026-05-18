package com.productos.demo.exchange.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeRateApiResponse(

    @JsonProperty("result")
    String result,

    @JsonProperty("base_code")
    String baseCode,

    @JsonProperty("time_last_update_unix")
    Long timeLastUpdateUnix,

    @JsonProperty("rates")
    Map<String, BigDecimal> rates
) {
}
