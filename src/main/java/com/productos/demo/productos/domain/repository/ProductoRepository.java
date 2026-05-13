package com.productos.demo.productos.domain.repository;

import com.productos.demo.productos.domain.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    /**
     * Busca un producto por su código EAN
     */
    Optional<Producto> findByCodigoEan(String codigoEan);
    
    /**
     * Busca productos cuyo nombre contenga el término de búsqueda (case-insensitive)
     */
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    /**
     * Busca productos por nombre o código EAN (case-insensitive)
     */
    Page<Producto> findByNombreContainingIgnoreCaseOrCodigoEanContaining(
        String nombre, 
        String codigoEan, 
        Pageable pageable
    );
    
    /**
     * Verifica si existe un producto con el código EAN dado
     */
    boolean existsByCodigoEan(String codigoEan);
}
