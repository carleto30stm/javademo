package com.productos.demo.exchange.messaging;

import com.productos.demo.exchange.domain.model.RateChangeLog;
import com.productos.demo.exchange.domain.repository.RateChangeLogRepository;
import com.productos.demo.exchange.messaging.dto.RateChangeEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateEventConsumer Unit Tests")
class ExchangeRateEventConsumerTest {

    @Mock
    private RateChangeLogRepository rateChangeLogRepository;

    @InjectMocks
    private ExchangeRateEventConsumer consumer;

    @Test
    @DisplayName("persiste el evento de variación en la base de datos")
    void persisteEventoEnBaseDeDatos() {
        RateChangeEvent evento = new RateChangeEvent(
            "USD", "EUR",
            new BigDecimal("0.90"),
            new BigDecimal("0.925"),
            new BigDecimal("2.78"),
            Instant.now()
        );

        consumer.consume(evento);

        ArgumentCaptor<RateChangeLog> captor = ArgumentCaptor.forClass(RateChangeLog.class);
        verify(rateChangeLogRepository).save(captor.capture());

        RateChangeLog log = captor.getValue();
        assertThat(log.getMonedaBase()).isEqualTo("USD");
        assertThat(log.getMonedaDestino()).isEqualTo("EUR");
        assertThat(log.getTasaAnterior()).isEqualByComparingTo("0.90");
        assertThat(log.getTasaNueva()).isEqualByComparingTo("0.925");
        assertThat(log.getVariacionPct()).isEqualByComparingTo("2.78");
    }
}
