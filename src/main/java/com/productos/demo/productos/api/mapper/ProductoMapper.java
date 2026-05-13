package com.productos.demo.productos.api.mapper;

import com.productos.demo.productos.api.dto.ProductoCreateRequest;
import com.productos.demo.productos.api.dto.ProductoResponse;
import com.productos.demo.productos.domain.model.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toDomain(ProductoCreateRequest request) {
        return Producto.builder()
            .codigoEan(request.codigoEan())
            .nombre(request.nombre())
            .descripcion(request.descripcion())
            .precio(request.precio())
            .stock(request.stock())
            .build();
    }

    public ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
            producto.getProductoId(),
            producto.getCodigoEan(),
            producto.getNombre(),
            producto.getDescripcion(),
            producto.getPrecio(),
            producto.getStock(),
            producto.getCreatedAt(),
            producto.getUpdatedAt()
        );
    }
}
