package com.productos.demo.productos.api.mapper;

import com.productos.demo.productos.api.dto.ProductoCreateRequest;
import com.productos.demo.productos.api.dto.ProductoResponse;
import com.productos.demo.productos.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductoMapper Unit Tests")
class ProductoMapperTest {

    private ProductoMapper productoMapper;

    @BeforeEach
    void setUp() {
        productoMapper = new ProductoMapper();
    }

    @Test
    @DisplayName("toDomain convierte correctamente ProductoCreateRequest a Producto")
    void toDomain_requestValido_retornaProducto() {
        ProductoCreateRequest request = new ProductoCreateRequest(
            "1234567890123",
            "Producto Test",
            "Descripcion larga del producto de prueba",
            new BigDecimal("19.99"),
            50
        );

        Producto result = productoMapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getCodigoEan()).isEqualTo("1234567890123");
        assertThat(result.getNombre()).isEqualTo("Producto Test");
        assertThat(result.getDescripcion()).isEqualTo("Descripcion larga del producto de prueba");
        assertThat(result.getPrecio()).isEqualByComparingTo("19.99");
        assertThat(result.getStock()).isEqualTo(50);
        assertThat(result.getProductoId()).isNull();
    }

    @Test
    @DisplayName("toResponse convierte correctamente Producto a ProductoResponse")
    void toResponse_productoCompleto_retornaResponse() {
        Instant now = Instant.now();
        Producto producto = Producto.builder()
            .productoId(1L)
            .codigoEan("1234567890123")
            .nombre("Producto Test")
            .descripcion("Descripcion de prueba")
            .precio(new BigDecimal("9.99"))
            .stock(20)
            .createdAt(now)
            .updatedAt(now)
            .build();

        ProductoResponse result = productoMapper.toResponse(producto);

        assertThat(result).isNotNull();
        assertThat(result.productoId()).isEqualTo(1L);
        assertThat(result.codigoEan()).isEqualTo("1234567890123");
        assertThat(result.nombre()).isEqualTo("Producto Test");
        assertThat(result.descripcion()).isEqualTo("Descripcion de prueba");
        assertThat(result.precio()).isEqualByComparingTo("9.99");
        assertThat(result.stock()).isEqualTo(20);
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toResponse maneja fechas nulas correctamente")
    void toResponse_sinFechas_retornaResponseConFechasNulas() {
        Producto producto = Producto.builder()
            .productoId(2L)
            .codigoEan("9876543210987")
            .nombre("Sin Fechas")
            .descripcion("Producto sin timestamps")
            .precio(new BigDecimal("5.00"))
            .stock(0)
            .build();

        ProductoResponse result = productoMapper.toResponse(producto);

        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
    }
}
