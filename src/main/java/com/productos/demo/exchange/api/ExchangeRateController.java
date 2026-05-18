package com.productos.demo.exchange.api;

import com.productos.demo.exchange.api.dto.ExchangeRateResponse;
import com.productos.demo.exchange.api.dto.ProductoPrecioConvertidoResponse;
import com.productos.demo.exchange.domain.service.ExchangeRateService;
import com.productos.demo.productos.domain.model.Producto;
import com.productos.demo.productos.domain.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Exchange Rates", description = "Consulta de tasas de cambio y conversión de precios de productos")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final ProductoService     productoService;

    @GetMapping("/exchange-rates/latest")
    @Operation(
        summary = "Obtener tasas de cambio actuales",
        description = "Devuelve las tasas de cambio cacheadas en Redis. " +
                      "Si no hay datos en caché, consulta open.er-api.com en tiempo real."
    )
    public ResponseEntity<ExchangeRateResponse> getLatestRates() {
        ExchangeRateResponse response = new ExchangeRateResponse(
            exchangeRateService.obtenerMonedaBase(),
            exchangeRateService.obtenerTodasLasTasas(),
            Instant.now()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/productos/{id}/precio-convertido")
    @Operation(
        summary = "Precio de producto convertido a otra moneda",
        description = "Obtiene el precio de un producto y lo convierte a la moneda solicitada " +
                      "usando la tasa de cambio cacheada en Redis."
    )
    public ResponseEntity<ProductoPrecioConvertidoResponse> getPrecioConvertido(
        @Parameter(description = "ID del producto") @PathVariable Long id,
        @Parameter(description = "Código de moneda destino (EUR, MXN, GBP, COP)")
        @RequestParam(defaultValue = "EUR") String moneda
    ) {
        Producto producto = productoService.obtenerProductoPorId(id);
        BigDecimal tasa   = exchangeRateService.obtenerTasa(moneda.toUpperCase());

        BigDecimal precioConvertido = producto.getPrecio()
            .multiply(tasa)
            .setScale(2, RoundingMode.HALF_UP);

        ProductoPrecioConvertidoResponse response = new ProductoPrecioConvertidoResponse(
            producto.getProductoId(),
            producto.getNombre(),
            producto.getPrecio(),
            "USD",
            precioConvertido,
            moneda.toUpperCase(),
            tasa
        );

        return ResponseEntity.ok(response);
    }
}
