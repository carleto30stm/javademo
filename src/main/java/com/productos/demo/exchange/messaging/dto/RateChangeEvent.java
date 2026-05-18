package com.productos.demo.exchange.messaging.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record RateChangeEvent(
    String monedaBase,
    String monedaDestino,
    BigDecimal tasaAnterior,
    BigDecimal tasaNueva,
    BigDecimal variacionPct,
    Instant occurredAt
) implements Serializable {
}
