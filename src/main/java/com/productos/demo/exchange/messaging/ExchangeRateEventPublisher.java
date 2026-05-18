package com.productos.demo.exchange.messaging;

import com.productos.demo.common.config.RabbitMQConfig;
import com.productos.demo.exchange.messaging.dto.RateChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(RateChangeEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_RATE_EXCHANGE,
                RabbitMQConfig.EXCHANGE_RATE_ROUTING,
                event
            );
            log.info("[RabbitMQ] Evento publicado: {} → {} | variación={}%",
                event.monedaBase(), event.monedaDestino(), event.variacionPct());
        } catch (Exception e) {
            log.error("[RabbitMQ] Error al publicar evento de variación de tasa: {}", e.getMessage());
        }
    }
}
