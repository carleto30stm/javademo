package com.productos.demo.productos.domain.service;

import com.productos.demo.common.config.CacheConfig;
import com.productos.demo.common.exception.CodigoEanDuplicadoException;
import com.productos.demo.common.exception.ProductoNoEncontradoException;
import com.productos.demo.productos.api.dto.ProductoUpdateRequest;
import com.productos.demo.productos.domain.model.Producto;
import com.productos.demo.productos.domain.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    /**
     * Obtiene todos los productos con paginación y búsqueda
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PRODUCTOS_CACHE, key = "#page + '-' + #limit + '-' + (#search != null ? #search : 'all') + '-' + #sortBy + '-' + #sortDir")
    public Page<Producto> listarProductos(
        int page,
        int limit,
        String search,
        String sortBy,
        String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());
        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));

        if (search == null || search.trim().isEmpty()) {
            return productoRepository.findAll(pageable);
        }

        return productoRepository.findByNombreContainingIgnoreCaseOrCodigoEanContaining(
            search,
            search,
            pageable
        );
    }

    /**
     * Obtiene un producto por su ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PRODUCTO_BY_ID_CACHE, key = "#id")
    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    /**
     * Crea un nuevo producto
     * Valida que el código EAN sea único
     */
    @Transactional
    @CacheEvict(value = {CacheConfig.PRODUCTOS_CACHE, CacheConfig.PRODUCTO_BY_ID_CACHE}, allEntries = true)
    public Producto crearProducto(Producto producto) {
        // Validar unicidad del código EAN
        if (productoRepository.existsByCodigoEan(producto.getCodigoEan())) {
            throw new CodigoEanDuplicadoException(producto.getCodigoEan());
        }

        return productoRepository.save(producto);
    }

    /**
     * Actualiza un producto existente.
     * Solo modifica los campos no nulos del request (partial update).
     * Valida que el código EAN sea único si se está modificando.
     */
    @Transactional
    @CacheEvict(value = {CacheConfig.PRODUCTOS_CACHE, CacheConfig.PRODUCTO_BY_ID_CACHE}, allEntries = true)
    public Producto actualizarProducto(Long id, ProductoUpdateRequest request) {
        Producto producto = obtenerProductoPorId(id);

        if (request.codigoEan() != null
                && !request.codigoEan().equals(producto.getCodigoEan())
                && productoRepository.existsByCodigoEan(request.codigoEan())) {
            throw new CodigoEanDuplicadoException(request.codigoEan());
        }

        if (request.codigoEan()  != null) producto.setCodigoEan(request.codigoEan());
        if (request.nombre()     != null) producto.setNombre(request.nombre());
        if (request.descripcion()!= null) producto.setDescripcion(request.descripcion());
        if (request.precio()     != null) producto.setPrecio(request.precio());
        if (request.stock()      != null) producto.setStock(request.stock());

        return productoRepository.save(producto);
    }

    /**
     * Elimina un producto por su ID
     */
    @Transactional
    @CacheEvict(value = {CacheConfig.PRODUCTOS_CACHE, CacheConfig.PRODUCTO_BY_ID_CACHE}, allEntries = true)
    public void eliminarProducto(Long id) {
        Producto producto = obtenerProductoPorId(id);
        productoRepository.delete(producto);
    }
}
