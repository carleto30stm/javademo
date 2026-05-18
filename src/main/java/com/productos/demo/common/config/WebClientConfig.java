package com.productos.demo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${exchange-rate.api.base-url}")
    private String exchangeRateBaseUrl;

    @Bean("exchangeRateWebClient")
    public WebClient exchangeRateWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
            .build();

        return WebClient.builder()
            .baseUrl(exchangeRateBaseUrl)
            .exchangeStrategies(strategies)
            .build();
    }
}
