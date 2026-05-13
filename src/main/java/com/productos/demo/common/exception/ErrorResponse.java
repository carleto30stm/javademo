package com.productos.demo.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Instant timestamp,
    @JsonProperty("traceId")
    String traceId,
    Integer status,
    String error,
    String codigo,
    String message,
    String path,
    List<ErrorDetail> details
) {
}
