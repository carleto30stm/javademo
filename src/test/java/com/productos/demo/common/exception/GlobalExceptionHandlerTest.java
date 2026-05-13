package com.productos.demo.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/productos");
    }

    @Nested
    @DisplayName("handleValidationException")
    class HandleValidationException {

        @Test
        @DisplayName("retorna 400 con detalles de validación")
        void handleValidation_retorna400ConDetalles() {
            FieldError fieldError = new FieldError("productoCreateRequest", "nombre", "El nombre no puede estar vacío");
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().codigo()).isEqualTo("ERROR_VALIDACION");
            assertThat(response.getBody().details()).hasSize(1);
            assertThat(response.getBody().details().get(0).target()).isEqualTo("nombre");
            assertThat(response.getBody().details().get(0).code()).isEqualTo("VALIDATION_ERROR");
        }
    }

    @Nested
    @DisplayName("handleProductoNoEncontrado")
    class HandleProductoNoEncontrado {

        @Test
        @DisplayName("retorna 404 con mensaje del producto no encontrado")
        void handleProductoNoEncontrado_retorna404() {
            ProductoNoEncontradoException ex = new ProductoNoEncontradoException(42L);

            ResponseEntity<ErrorResponse> response = handler.handleProductoNoEncontrado(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().codigo()).isEqualTo("PRODUCTO_NO_ENCONTRADO");
            assertThat(response.getBody().message()).contains("42");
            assertThat(response.getBody().path()).isEqualTo("/api/v1/productos");
            assertThat(response.getBody().timestamp()).isNotNull();
            assertThat(response.getBody().traceId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("handleCodigoEanDuplicado")
    class HandleCodigoEanDuplicado {

        @Test
        @DisplayName("retorna 409 con detalle del EAN duplicado")
        void handleCodigoEanDuplicado_retorna409() {
            CodigoEanDuplicadoException ex = new CodigoEanDuplicadoException("1234567890123");

            ResponseEntity<ErrorResponse> response = handler.handleCodigoEanDuplicado(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().codigo()).isEqualTo("CODIGO_EAN_DUPLICADO");
            assertThat(response.getBody().details()).hasSize(1);
            assertThat(response.getBody().details().get(0).target()).isEqualTo("codigo_ean");
            assertThat(response.getBody().message()).contains("1234567890123");
        }
    }

    @Nested
    @DisplayName("handleGenericException")
    class HandleGenericException {

        @Test
        @DisplayName("retorna 500 para excepciones genéricas")
        void handleGeneric_retorna500() {
            Exception ex = new RuntimeException("Error inesperado");

            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().codigo()).isEqualTo("ERROR_INTERNO");
            assertThat(response.getBody().details()).isNull();
        }
    }
}
