package com.productos.demo.productos.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaginationInfo(
    Integer page,
    Integer limit,
    Integer total,
    @JsonProperty("totalPages")
    Integer totalPages
) {
}
