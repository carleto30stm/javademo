package com.productos.demo.exchange.messaging;

import com.productos.demo.exchange.domain.model.RateChangeLog;
import com.productos.demo.exchange.domain.repository.RateChangeLogRepository;
import com.productos.demo.exchange.messaging.dto.RateChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.productos.demo.common.config.RabbitMQConfig.EXCHANGE_RATE_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateEventConsumer {

    private final RateChangeLogRepository rateChangeLogRepository;

    @RabbitListener(queues = EXCHANGE_RATE_QUEUE)
    public void consume(RateChangeEvent event) {
        log.info("[RabbitMQ] Evento recibido: {} → {} | anterior={} nueva={} variación={}%",
            event.monedaBase(), event.monedaDestino(),
            event.tasaAnterior(), event.tasaNueva(), event.variacionPct());

        RateChangeLog changeLog = RateChangeLog.builder()
            .monedaBase(event.monedaBase())
            .monedaDestino(event.monedaDestino())
            .tasaAnterior(event.tasaAnterior())
            .tasaNueva(event.tasaNueva())
            .variacionPct(event.variacionPct())
            .build();

        rateChangeLogRepository.save(changeLog);
    }
}
