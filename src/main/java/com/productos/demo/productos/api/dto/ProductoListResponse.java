package com.productos.demo.productos.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record ProductoListResponse(
    List<ProductoResponse> data,
    PaginationInfo pagination,
    Instant timestamp,
    @JsonProperty("traceId")
    String traceId
) {
}
