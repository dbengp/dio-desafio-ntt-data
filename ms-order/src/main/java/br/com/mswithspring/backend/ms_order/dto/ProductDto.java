package br.com.mswithspring.backend.ms_order.dto;

import java.math.BigDecimal;

public record ProductDto(
        String name,
        String description,
        BigDecimal price
) {
}
