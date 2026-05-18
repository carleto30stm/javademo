package com.productos.demo.exchange.domain.service;

import com.productos.demo.exchange.infrastructure.client.ExchangeRateClient;
import com.productos.demo.exchange.infrastructure.client.dto.ExchangeRateApiResponse;
import com.productos.demo.exchange.messaging.ExchangeRateEventPublisher;
import com.productos.demo.exchange.messaging.dto.RateChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService Unit Tests")
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private ExchangeRateEventPublisher publisher;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeRateService, "thresholdPct",      2.0);
        ReflectionTestUtils.setField(exchangeRateService, "ttlMinutes",         60L);
        ReflectionTestUtils.setField(exchangeRateService, "supportedCurrencies", List.of("EUR", "MXN"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ─── obtenerTasa ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerTasa")
    class ObtenerTasa {

        @Test
        @DisplayName("devuelve tasa desde Redis cuando está disponible")
        void devuelveTasaDesdeRedis() {
            Map<String, BigDecimal> rates = Map.of("EUR", new BigDecimal("0.92"));
            when(valueOperations.get("exchange:rates:latest")).thenReturn(rates);

            BigDecimal result = exchangeRateService.obtenerTasa("EUR");

            assertThat(result).isEqualByComparingTo("0.92");
            verify(exchangeRateClient, never()).fetchLatestRates();
        }

        @Test
        @DisplayName("llama a la API cuando Redis está vacío (fallback)")
        void llamaApiCuandoRedisVacio() {
            when(valueOperations.get("exchange:rates:latest")).thenReturn(null);

            ExchangeRateApiResponse apiResp = new ExchangeRateApiResponse(
                "success", "USD", 1234567890L,
                Map.of("EUR", new BigDecimal("0.91"))
            );
            when(exchangeRateClient.fetchLatestRates()).thenReturn(Mono.just(apiResp));

            BigDecimal result = exchangeRateService.obtenerTasa("EUR");

            assertThat(result).isEqualByComparingTo("0.91");
            verify(exchangeRateClient).fetchLatestRates();
        }

        @Test
        @DisplayName("lanza IllegalArgumentException cuando la moneda no existe en el API")
        void lanzaExcepcionMonedaNoSoportada() {
            when(valueOperations.get("exchange:rates:latest")).thenReturn(null);

            ExchangeRateApiResponse apiResp = new ExchangeRateApiResponse(
                "success", "USD", 1234567890L, Map.of("EUR", new BigDecimal("0.91"))
            );
            when(exchangeRateClient.fetchLatestRates()).thenReturn(Mono.just(apiResp));

            assertThatThrownBy(() -> exchangeRateService.obtenerTasa("XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XYZ");
        }
    }

    // ─── verificarVariacionesYPublicar ────────────────────────────────────────

    @Nested
    @DisplayName("actualizarTasas - publicación de eventos")
    class ActualizarTasas {

        @Test
        @DisplayName("publica evento cuando la variación supera el umbral")
        void publicaEventoCuandoVariacionSuperaUmbral() {
            // Tasas antiguas en Redis
            Map<String, BigDecimal> tasasAntiguas = Map.of(
                "EUR", new BigDecimal("0.90"),
                "MXN", new BigDecimal("17.00")
            );
            when(valueOperations.get("exchange:rates:latest"))
                .thenReturn(tasasAntiguas)   // primera lectura (tasas antiguas)
                .thenReturn(tasasAntiguas);  // getMonedaBase

            // Respuesta de la API con variación de >2% en EUR (0.90 → 0.925 = 2.78%)
            ExchangeRateApiResponse apiResp = new ExchangeRateApiResponse(
                "success", "USD", 1234567890L,
                Map.of("EUR", new BigDecimal("0.925"), "MXN", new BigDecimal("17.05"))
            );
            when(exchangeRateClient.fetchLatestRates()).thenReturn(Mono.just(apiResp));

            exchangeRateService.actualizarTasas();

            ArgumentCaptor<RateChangeEvent> captor = ArgumentCaptor.forClass(RateChangeEvent.class);
            verify(publisher, atLeastOnce()).publish(captor.capture());

            RateChangeEvent evento = captor.getValue();
            assertThat(evento.monedaDestino()).isEqualTo("EUR");
            assertThat(evento.tasaAnterior()).isEqualByComparingTo("0.90");
            assertThat(evento.tasaNueva()).isEqualByComparingTo("0.925");
        }

        @Test
        @DisplayName("no publica evento cuando la variación no supera el umbral")
        void noPublicaEventoCuandoVariacionBaja() {
            Map<String, BigDecimal> tasasAntiguas = Map.of(
                "EUR", new BigDecimal("0.920"),
                "MXN", new BigDecimal("17.00")
            );
            when(valueOperations.get("exchange:rates:latest")).thenReturn(tasasAntiguas);

            // Variación de EUR: 0.920 → 0.921 = 0.11% (por debajo del umbral 2%)
            ExchangeRateApiResponse apiResp = new ExchangeRateApiResponse(
                "success", "USD", 1234567890L,
                Map.of("EUR", new BigDecimal("0.921"), "MXN", new BigDecimal("17.01"))
            );
            when(exchangeRateClient.fetchLatestRates()).thenReturn(Mono.just(apiResp));

            exchangeRateService.actualizarTasas();

            verify(publisher, never()).publish(any());
        }

        @Test
        @DisplayName("no publica evento en la primera carga (sin tasas anteriores)")
        void noPublicaEnPrimeraCarga() {
            when(valueOperations.get("exchange:rates:latest")).thenReturn(null);

            ExchangeRateApiResponse apiResp = new ExchangeRateApiResponse(
                "success", "USD", 1234567890L,
                Map.of("EUR", new BigDecimal("0.92"), "MXN", new BigDecimal("17.10"))
            );
            when(exchangeRateClient.fetchLatestRates()).thenReturn(Mono.just(apiResp));

            exchangeRateService.actualizarTasas();

            verify(publisher, never()).publish(any());
        }
    }
}
