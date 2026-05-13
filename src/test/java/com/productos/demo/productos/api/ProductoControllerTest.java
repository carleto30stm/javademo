package com.productos.demo.productos.api;

import com.productos.demo.common.util.TraceIdGenerator;
import com.productos.demo.productos.api.dto.*;
import com.productos.demo.productos.api.mapper.ProductoMapper;
import com.productos.demo.productos.domain.model.Producto;
import com.productos.demo.productos.domain.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoController Unit Tests")
class ProductoControllerTest {

    @Mock private ProductoService productoService;
    @Mock private ProductoMapper productoMapper;
    @Mock private TraceIdGenerator traceIdGenerator;

    @InjectMocks
    private ProductoController productoController;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private Producto productoBase;
    private ProductoResponse productoResponseBase;

    @BeforeEach
    void setUp() {
        // Inyectar el clock fijo mediante reflexión (campo final de Lombok)
        try {
            var field = ProductoController.class.getDeclaredField("clock");
            field.setAccessible(true);
            field.set(productoController, fixedClock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        productoBase = Producto.builder()
            .productoId(1L)
            .codigoEan("1234567890123")
            .nombre("Producto Test")
            .descripcion("Descripcion de prueba")
            .precio(new BigDecimal("10.99"))
            .stock(100)
            .build();

        productoResponseBase = new ProductoResponse(
            1L, "1234567890123", "Producto Test", "Descripcion de prueba",
            new BigDecimal("10.99"), 100, null, null
        );
    }

    @Nested
    @DisplayName("listarProductos")
    class ListarProductos {

        @Test
        @DisplayName("retorna 200 con lista paginada de productos")
        void listarProductos_retorna200ConPaginacion() {
            Page<Producto> page = new PageImpl<>(List.of(productoBase));
            when(productoService.listarProductos(0, 10, null, "nombre", "asc")).thenReturn(page);
            when(productoMapper.toResponse(productoBase)).thenReturn(productoResponseBase);
            when(traceIdGenerator.generate()).thenReturn("trace-123");

            ResponseEntity<ProductoListResponse> response =
                productoController.listarProductos(0, 10, null, "nombre", "asc");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data()).hasSize(1);
            assertThat(response.getBody().pagination().total()).isEqualTo(1);
            assertThat(response.getBody().traceId()).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("crearProducto")
    class CrearProducto {

        @Test
        @DisplayName("retorna 201 con el producto creado")
        void crearProducto_retorna201() {
            ProductoCreateRequest request = new ProductoCreateRequest(
                "1234567890123", "Nuevo Producto", "Descripcion nueva del producto",
                new BigDecimal("5.99"), 10
            );
            when(productoMapper.toDomain(request)).thenReturn(productoBase);
            when(productoService.crearProducto(productoBase)).thenReturn(productoBase);
            when(productoMapper.toResponse(productoBase)).thenReturn(productoResponseBase);

            ResponseEntity<ProductoResponse> response = productoController.crearProducto(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().productoId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("obtenerProductoPorId")
    class ObtenerProductoPorId {

        @Test
        @DisplayName("retorna 200 con el producto encontrado")
        void obtenerProductoPorId_retorna200() {
            when(productoService.obtenerProductoPorId(1L)).thenReturn(productoBase);
            when(productoMapper.toResponse(productoBase)).thenReturn(productoResponseBase);

            ResponseEntity<ProductoResponse> response = productoController.obtenerProductoPorId(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().codigoEan()).isEqualTo("1234567890123");
        }
    }

    @Nested
    @DisplayName("actualizarProducto")
    class ActualizarProducto {

        @Test
        @DisplayName("retorna 200 con el producto actualizado")
        void actualizarProducto_retorna200() {
            ProductoUpdateRequest request = new ProductoUpdateRequest(
                null, "Nombre Actualizado", null, null, null
            );
            when(productoService.actualizarProducto(1L, request)).thenReturn(productoBase);
            when(productoMapper.toResponse(productoBase)).thenReturn(productoResponseBase);

            ResponseEntity<ProductoResponse> response = productoController.actualizarProducto(1L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(productoService).actualizarProducto(1L, request);
        }
    }

    @Nested
    @DisplayName("eliminarProductoPorId")
    class EliminarProductoPorId {

        @Test
        @DisplayName("retorna 204 sin contenido")
        void eliminarProductoPorId_retorna204() {
            ResponseEntity<Void> response = productoController.eliminarProductoPorId(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
            verify(productoService).eliminarProducto(1L);
        }
    }
}
