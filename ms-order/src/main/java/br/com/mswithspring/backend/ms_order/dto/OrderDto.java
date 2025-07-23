package br.com.mswithspring.backend.ms_order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderDto(
        @NotNull(message = "O ID do cliente não pode ser nulo.")
        @Positive(message = "O ID do cliente deve ser um número positivo.")
        Long customerId,

        @NotNull(message = "A lista de itens do pedido não pode ser nula.")
        @Size(min = 1, message = "O pedido deve conter pelo menos um item.")
        @Valid
        List<OrderItemDto> items
) {
}
