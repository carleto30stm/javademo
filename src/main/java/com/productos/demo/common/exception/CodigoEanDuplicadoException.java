package com.productos.demo.common.exception;

public class CodigoEanDuplicadoException extends RuntimeException {
    
    private static final String CODIGO_ERROR = "CODIGO_EAN_DUPLICADO";
    private final String codigoEan;
    
    public CodigoEanDuplicadoException(String codigoEan) {
        super("El código EAN " + codigoEan + " ya existe en el sistema");
        this.codigoEan = codigoEan;
    }
    
    public String getCodigoError() {
        return CODIGO_ERROR;
    }
    
    public String getCodigoEan() {
        return codigoEan;
    }
}
