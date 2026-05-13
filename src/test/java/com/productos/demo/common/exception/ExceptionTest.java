package com.productos.demo.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception Classes Unit Tests")
class ExceptionTest {

    @Test
    @DisplayName("ProductoNoEncontradoException contiene mensaje con el ID")
    void productoNoEncontradoException_contieneId() {
        ProductoNoEncontradoException ex = new ProductoNoEncontradoException(5L);

        assertThat(ex.getMessage()).contains("5");
        assertThat(ex.getCodigoError()).isEqualTo("PRODUCTO_NO_ENCONTRADO");
    }

    @Test
    @DisplayName("CodigoEanDuplicadoException contiene EAN y código de error")
    void codigoEanDuplicadoException_contieneEanYCodigo() {
        CodigoEanDuplicadoException ex = new CodigoEanDuplicadoException("1234567890123");

        assertThat(ex.getMessage()).contains("1234567890123");
        assertThat(ex.getCodigoError()).isEqualTo("CODIGO_EAN_DUPLICADO");
        assertThat(ex.getCodigoEan()).isEqualTo("1234567890123");
    }

    @Test
    @DisplayName("ErrorDetail almacena correctamente sus campos")
    void errorDetail_camposCorrectos() {
        ErrorDetail detail = new ErrorDetail("campo", "CODIGO", "mensaje de error");

        assertThat(detail.target()).isEqualTo("campo");
        assertThat(detail.code()).isEqualTo("CODIGO");
        assertThat(detail.message()).isEqualTo("mensaje de error");
    }
}
