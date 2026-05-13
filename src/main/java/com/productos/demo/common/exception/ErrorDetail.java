package com.productos.demo.common.exception;

public record ErrorDetail(
    String target,
    String code,
    String message
) {
}
