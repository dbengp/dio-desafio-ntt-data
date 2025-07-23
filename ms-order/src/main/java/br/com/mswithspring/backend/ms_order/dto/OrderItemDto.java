package br.com.mswithspring.backend.ms_order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDto(
        @NotNull(message = "O ID do produto não pode ser nulo.")
        @Positive(message = "O ID do produto deve ser um número positivo.")
        Long productId,

        @NotNull(message = "A quantidade não pode ser nula.")
        @Positive(message = "A quantidade deve ser um número positivo.")
        Integer quantity
) {
}
