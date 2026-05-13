package com.productos.demo.productos.api;

import com.productos.demo.common.util.TraceIdGenerator;
import com.productos.demo.productos.api.dto.*;
import com.productos.demo.productos.api.mapper.ProductoMapper;
import com.productos.demo.productos.domain.model.Producto;
import com.productos.demo.productos.domain.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ProductoController implements ProductoApi {

    private final ProductoService productoService;
    private final ProductoMapper productoMapper;
    private final TraceIdGenerator traceIdGenerator;
    private final Clock clock;

    @Override
    public ResponseEntity<ProductoListResponse> listarProductos(
        int page,
        int limit,
        String search,
        String sortBy,
        String sortDir
    ) {
        Page<Producto> productosPage = productoService.listarProductos(page, limit, search, sortBy, sortDir);

        var productosResponse = productosPage.getContent()
            .stream()
            .map(productoMapper::toResponse)
            .toList();

        PaginationInfo pagination = new PaginationInfo(
            page,
            limit,
            (int) productosPage.getTotalElements(),
            productosPage.getTotalPages()
        );

        ProductoListResponse response = new ProductoListResponse(
            productosResponse,
            pagination,
            Instant.now(clock),
            traceIdGenerator.generate()
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductoResponse> crearProducto(ProductoCreateRequest request) {
        Producto productoGuardado = productoService.crearProducto(productoMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(productoMapper.toResponse(productoGuardado));
    }

    @Override
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(Long id) {
        return ResponseEntity.ok(productoMapper.toResponse(productoService.obtenerProductoPorId(id)));
    }

    @Override
    public ResponseEntity<ProductoResponse> actualizarProducto(Long id, ProductoUpdateRequest request) {
        Producto productoGuardado = productoService.actualizarProducto(id, request);
        return ResponseEntity.ok(productoMapper.toResponse(productoGuardado));
    }

    @Override
    public ResponseEntity<Void> eliminarProductoPorId(Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}

