package com.productos.demo.exchange.domain.service;

import com.productos.demo.exchange.infrastructure.client.ExchangeRateClient;
import com.productos.demo.exchange.messaging.ExchangeRateEventPublisher;
import com.productos.demo.exchange.messaging.dto.RateChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final String REDIS_RATES_KEY = "exchange:rates:latest";
    private static final String REDIS_BASE_KEY  = "exchange:base";

    private final ExchangeRateClient         exchangeRateClient;
    private final ExchangeRateEventPublisher  publisher;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${exchange-rate.alert.threshold-pct}")
    private double thresholdPct;

    @Value("${exchange-rate.cache.ttl-minutes}")
    private long ttlMinutes;

    @Value("${exchange-rate.supported-currencies:EUR,MXN,GBP,COP}")
    private List<String> supportedCurrencies;

    // ---------------------------------------------------------------
    // Job programado: se ejecuta cada 30 minutos (configurable en yml)
    // ---------------------------------------------------------------
    @Scheduled(cron = "${exchange-rate.schedule.cron}")
    public void actualizarTasas() {
        log.info("[ExchangeRate] Iniciando actualización de tasas de cambio...");
        exchangeRateClient.fetchLatestRates()
            .subscribe(
                response -> {
                    Map<String, BigDecimal> tasasAntiguas = getTasasFromRedis();
                    Map<String, BigDecimal> tasasNuevas   = response.rates();

                    guardarEnRedis(response.baseCode(), tasasNuevas);
                    verificarVariacionesYPublicar(response.baseCode(), tasasAntiguas, tasasNuevas);
                },
                error -> log.error("[ExchangeRate] Falló la actualización de tasas: {}", error.getMessage())
            );
    }

    // ---------------------------------------------------------------
    // Devuelve la tasa de cambio de una moneda desde Redis
    // Si Redis no tiene datos, llama a la API de forma bloqueante como fallback
    // ---------------------------------------------------------------
    public BigDecimal obtenerTasa(String moneda) {
        Map<String, BigDecimal> tasas = getTasasFromRedis();

        if (!tasas.isEmpty()) {
            BigDecimal tasa = tasas.get(moneda.toUpperCase());
            if (tasa != null) {
                return tasa;
            }
        }

        // Fallback: llama a la API de forma síncrona si Redis está vacío
        log.warn("[ExchangeRate] Redis vacío, consultando API directamente para {}", moneda);
        return exchangeRateClient.fetchLatestRates()
            .map(r -> {
                guardarEnRedis(r.baseCode(), r.rates());
                return Optional.ofNullable(r.rates().get(moneda.toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Moneda no soportada: " + moneda));
            })
            .block();
    }

    // ---------------------------------------------------------------
    // Devuelve todas las tasas disponibles desde Redis
    // ---------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Map<String, BigDecimal> obtenerTodasLasTasas() {
        Map<String, BigDecimal> tasas = getTasasFromRedis();
        if (tasas.isEmpty()) {
            actualizarTasas();
            return getTasasFromRedis();
        }
        return tasas;
    }

    public String obtenerMonedaBase() {
        Object base = redisTemplate.opsForValue().get(REDIS_BASE_KEY);
        return base != null ? base.toString() : "USD";
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getTasasFromRedis() {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_RATES_KEY);
            if (value instanceof Map) {
                return (Map<String, BigDecimal>) value;
            }
        } catch (Exception e) {
            log.error("[ExchangeRate] Error leyendo Redis: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }

    private void guardarEnRedis(String baseCode, Map<String, BigDecimal> rates) {
        redisTemplate.opsForValue().set(REDIS_RATES_KEY, rates, Duration.ofMinutes(ttlMinutes));
        redisTemplate.opsForValue().set(REDIS_BASE_KEY,  baseCode, Duration.ofMinutes(ttlMinutes));
        log.info("[ExchangeRate] Tasas guardadas en Redis (TTL={}min, monedas={})", ttlMinutes, rates.size());
    }

    private void verificarVariacionesYPublicar(String base,
                                               Map<String, BigDecimal> antiguas,
                                               Map<String, BigDecimal> nuevas) {
        if (antiguas.isEmpty()) {
            log.info("[ExchangeRate] Primera carga, sin tasas anteriores para comparar.");
            return;
        }

        for (String moneda : supportedCurrencies) {
            BigDecimal anterior = antiguas.get(moneda);
            BigDecimal nueva    = nuevas.get(moneda);

            if (anterior == null || nueva == null) continue;

            BigDecimal variacion = nueva.subtract(anterior)
                .abs()
                .divide(anterior, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

            if (variacion.compareTo(BigDecimal.valueOf(thresholdPct)) >= 0) {
                log.warn("[ExchangeRate] Variación significativa detectada: {} → {} = {}%", base, moneda, variacion);
                publisher.publish(new RateChangeEvent(base, moneda, anterior, nueva, variacion, Instant.now()));
            }
        }
    }
}
