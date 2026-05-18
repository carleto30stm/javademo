package com.productos.demo.exchange.infrastructure.client;

import com.productos.demo.exchange.infrastructure.client.dto.ExchangeRateApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateClient {

    @Qualifier("exchangeRateWebClient")
    private final WebClient webClient;

    @Value("${exchange-rate.api.base-currency}")
    private String baseCurrency;

    public Mono<ExchangeRateApiResponse> fetchLatestRates() {
        return webClient.get()
            .uri("/{baseCurrency}", baseCurrency)
            .retrieve()
            .bodyToMono(ExchangeRateApiResponse.class)
            .timeout(Duration.ofSeconds(10))
            .doOnSuccess(r -> log.info("[ExchangeRate] Tasas actualizadas desde open.er-api.com, base={}", r.baseCode()))
            .doOnError(e -> log.error("[ExchangeRate] Error al consultar API externa: {}", e.getMessage()));
    }
}
