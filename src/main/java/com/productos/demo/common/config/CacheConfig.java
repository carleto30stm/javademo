package com.productos.demo.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTOS_CACHE = "productos";
    public static final String PRODUCTO_BY_ID_CACHE = "productoById";
}
