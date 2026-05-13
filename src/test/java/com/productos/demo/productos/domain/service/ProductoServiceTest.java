package com.productos.demo.productos.domain.service;

import com.productos.demo.common.exception.CodigoEanDuplicadoException;
import com.productos.demo.common.exception.ProductoNoEncontradoException;
import com.productos.demo.productos.api.dto.ProductoUpdateRequest;
import com.productos.demo.productos.domain.model.Producto;
import com.productos.demo.productos.domain.repository.ProductoRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService Unit Tests")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoBase;

    @BeforeEach
    void setUp() {
        productoBase = Producto.builder()
            .productoId(1L)
            .codigoEan("1234567890123")
            .nombre("Producto Test")
            .descripcion("Descripcion de prueba del producto")
            .precio(new BigDecimal("10.99"))
            .stock(100)
            .build();
    }

    // ─── listarProductos ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarProductos")
    class ListarProductos {

        @Test
        @DisplayName("sin búsqueda retorna todos los productos paginados")
        void listarProductos_sinBusqueda_retornaTodos() {
            Page<Producto> page = new PageImpl<>(List.of(productoBase));
            when(productoRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<Producto> result = productoService.listarProductos(0, 10, null, "nombre", "asc");

            assertThat(result.getContent()).hasSize(1);
            verify(productoRepository).findAll(any(Pageable.class));
            verify(productoRepository, never())
                .findByNombreContainingIgnoreCaseOrCodigoEanContaining(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("con búsqueda vacía retorna todos los productos")
        void listarProductos_busquedaVacia_retornaTodos() {
            Page<Producto> page = new PageImpl<>(List.of(productoBase));
            when(productoRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<Producto> result = productoService.listarProductos(0, 10, "  ", "nombre", "asc");

            assertThat(result.getContent()).hasSize(1);
            verify(productoRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("con búsqueda filtra por nombre o código EAN")
        void listarProductos_conBusqueda_filtraPorNombreOEan() {
            Page<Producto> page = new PageImpl<>(List.of(productoBase));
            when(productoRepository.findByNombreContainingIgnoreCaseOrCodigoEanContaining(
                anyString(), anyString(), any(Pageable.class)
            )).thenReturn(page);

            Page<Producto> result = productoService.listarProductos(0, 10, "test", "nombre", "desc");

            assertThat(result.getContent()).hasSize(1);
            verify(productoRepository)
                .findByNombreContainingIgnoreCaseOrCodigoEanContaining(eq("test"), eq("test"), any(Pageable.class));
        }

        @Test
        @DisplayName("retorna página vacía cuando no hay resultados")
        void listarProductos_sinResultados_retornaPaginaVacia() {
            when(productoRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            Page<Producto> result = productoService.listarProductos(0, 10, null, "nombre", "asc");

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─── obtenerProductoPorId ──────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerProductoPorId")
    class ObtenerProductoPorId {

        @Test
        @DisplayName("retorna el producto cuando existe")
        void obtenerProductoPorId_existente_retornaProducto() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));

            Producto result = productoService.obtenerProductoPorId(1L);

            assertThat(result).isNotNull();
            assertThat(result.getProductoId()).isEqualTo(1L);
            assertThat(result.getNombre()).isEqualTo("Producto Test");
        }

        @Test
        @DisplayName("lanza ProductoNoEncontradoException cuando no existe")
        void obtenerProductoPorId_noExistente_lanzaExcepcion() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.obtenerProductoPorId(99L))
                .isInstanceOf(ProductoNoEncontradoException.class)
                .hasMessageContaining("99");
        }
    }

    // ─── crearProducto ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("crearProducto")
    class CrearProducto {

        @Test
        @DisplayName("crea y retorna el producto cuando el EAN no existe")
        void crearProducto_eanNuevo_guardaYRetorna() {
            when(productoRepository.existsByCodigoEan("1234567890123")).thenReturn(false);
            when(productoRepository.save(productoBase)).thenReturn(productoBase);

            Producto result = productoService.crearProducto(productoBase);

            assertThat(result).isEqualTo(productoBase);
            verify(productoRepository).save(productoBase);
        }

        @Test
        @DisplayName("lanza CodigoEanDuplicadoException cuando el EAN ya existe")
        void crearProducto_eanDuplicado_lanzaExcepcion() {
            when(productoRepository.existsByCodigoEan("1234567890123")).thenReturn(true);

            assertThatThrownBy(() -> productoService.crearProducto(productoBase))
                .isInstanceOf(CodigoEanDuplicadoException.class)
                .hasMessageContaining("1234567890123");

            verify(productoRepository, never()).save(any());
        }
    }

    // ─── actualizarProducto ────────────────────────────────────────────────────

    @Nested
    @DisplayName("actualizarProducto")
    class ActualizarProducto {

        @Test
        @DisplayName("actualiza solo los campos no nulos del request")
        void actualizarProducto_camposValidos_actualizaCampos() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));
            when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductoUpdateRequest request = new ProductoUpdateRequest(
                null, "Nombre Actualizado", null, new BigDecimal("25.00"), null
            );

            Producto result = productoService.actualizarProducto(1L, request);

            assertThat(result.getNombre()).isEqualTo("Nombre Actualizado");
            assertThat(result.getPrecio()).isEqualByComparingTo("25.00");
            assertThat(result.getCodigoEan()).isEqualTo("1234567890123"); // no cambió
            assertThat(result.getStock()).isEqualTo(100); // no cambió
        }

        @Test
        @DisplayName("actualiza el EAN cuando es distinto y no existe duplicado")
        void actualizarProducto_nuevoEan_actualizaEan() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));
            when(productoRepository.existsByCodigoEan("9999999999999")).thenReturn(false);
            when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductoUpdateRequest request = new ProductoUpdateRequest(
                "9999999999999", null, null, null, null
            );

            Producto result = productoService.actualizarProducto(1L, request);

            assertThat(result.getCodigoEan()).isEqualTo("9999999999999");
        }

        @Test
        @DisplayName("lanza CodigoEanDuplicadoException cuando el nuevo EAN ya existe")
        void actualizarProducto_eanDuplicado_lanzaExcepcion() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));
            when(productoRepository.existsByCodigoEan("9999999999999")).thenReturn(true);

            ProductoUpdateRequest request = new ProductoUpdateRequest(
                "9999999999999", null, null, null, null
            );

            assertThatThrownBy(() -> productoService.actualizarProducto(1L, request))
                .isInstanceOf(CodigoEanDuplicadoException.class)
                .hasMessageContaining("9999999999999");

            verify(productoRepository, never()).save(any());
        }

        @Test
        @DisplayName("no verifica duplicado cuando el EAN no cambia")
        void actualizarProducto_mismoEan_noVerificaDuplicado() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));
            when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductoUpdateRequest request = new ProductoUpdateRequest(
                "1234567890123", "Nuevo Nombre", null, null, null
            );

            productoService.actualizarProducto(1L, request);

            verify(productoRepository, never()).existsByCodigoEan(anyString());
        }

        @Test
        @DisplayName("lanza ProductoNoEncontradoException cuando el producto no existe")
        void actualizarProducto_productoInexistente_lanzaExcepcion() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            ProductoUpdateRequest request = new ProductoUpdateRequest(
                null, "Nombre", null, null, null
            );

            assertThatThrownBy(() -> productoService.actualizarProducto(99L, request))
                .isInstanceOf(ProductoNoEncontradoException.class);
        }
    }

    // ─── eliminarProducto ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("eliminarProducto")
    class EliminarProducto {

        @Test
        @DisplayName("elimina el producto cuando existe")
        void eliminarProducto_existente_elimina() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoBase));

            productoService.eliminarProducto(1L);

            verify(productoRepository).delete(productoBase);
        }

        @Test
        @DisplayName("lanza ProductoNoEncontradoException cuando el producto no existe")
        void eliminarProducto_noExistente_lanzaExcepcion() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.eliminarProducto(99L))
                .isInstanceOf(ProductoNoEncontradoException.class)
                .hasMessageContaining("99");

            verify(productoRepository, never()).delete(any());
        }
    }
}
