package com.productos.demo.exchange.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rate_change_log")
public class RateChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moneda_base", nullable = false, length = 3)
    private String monedaBase;

    @Column(name = "moneda_destino", nullable = false, length = 3)
    private String monedaDestino;

    @Column(name = "tasa_anterior", nullable = false, precision = 10, scale = 6)
    private BigDecimal tasaAnterior;

    @Column(name = "tasa_nueva", nullable = false, precision = 10, scale = 6)
    private BigDecimal tasaNueva;

    @Column(name = "variacion_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal variacionPct;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;
}
