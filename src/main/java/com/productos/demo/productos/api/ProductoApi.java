package com.productos.demo.productos.api;

import com.productos.demo.productos.api.dto.ProductoCreateRequest;
import com.productos.demo.productos.api.dto.ProductoListResponse;
import com.productos.demo.productos.api.dto.ProductoResponse;
import com.productos.demo.productos.api.dto.ProductoUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * INTERFAZ DEL CONTRATO API
 * 
 * Define el contrato REST para las operaciones CRUD de Productos.
 * Contiene TODAS las anotaciones de Spring (@RestController, @RequestMapping, etc.)
 * pero NO contiene lógica de negocio.
 * 
 * La implementación de esta interfaz es RESPONSABILIDAD del controlador.
 */
@RestController
@RequestMapping("/api/v1/productos")
public interface ProductoApi {

    /**
     * GET /api/v1/productos
     * Obtiene un listado paginado de productos
     */
    @GetMapping
    ResponseEntity<ProductoListResponse> listarProductos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "productoId") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDir
    );

    /**
     * POST /api/v1/productos
     * Crea un nuevo producto
     */
    @PostMapping
    ResponseEntity<ProductoResponse> crearProducto(
        @Valid @RequestBody ProductoCreateRequest request
    );

    /**
     * GET /api/v1/productos/{id}
     * Obtiene un producto por su ID
     */
    @GetMapping("/{id}")
    ResponseEntity<ProductoResponse> obtenerProductoPorId(
        @PathVariable Long id
    );

    /**
     * PUT /api/v1/productos/{id}
     * Actualiza un producto existente
     */
    @PutMapping("/{id}")
    ResponseEntity<ProductoResponse> actualizarProducto(
        @PathVariable Long id,
        @Valid @RequestBody ProductoUpdateRequest request
    );

    /**
     * DELETE /api/v1/productos/{id}
     * Elimina un producto
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminarProductoPorId(
        @PathVariable Long id
    );
}
