package com.productos.demo.common.exception;

public class ProductoNoEncontradoException extends RuntimeException {
    
    private static final String CODIGO_ERROR = "PRODUCTO_NO_ENCONTRADO";
    
    public ProductoNoEncontradoException(Long id) {
        super("El producto con ID " + id + " no existe");
    }
    
    public String getCodigoError() {
        return CODIGO_ERROR;
    }
}
