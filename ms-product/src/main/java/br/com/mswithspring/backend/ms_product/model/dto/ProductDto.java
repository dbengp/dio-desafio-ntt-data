package br.com.mswithspring.backend.ms_product.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductDto(
        @NotBlank(message = "O nome do produto não pode ser vazio.")
        @Size(min = 3, max = 100, message = "O nome do produto deve ter entre 3 e 100 caracteres.")
        String name,

        @Size(max = 500, message = "A descrição do produto não pode exceder 500 caracteres.")
        String description,

        @NotNull(message = "O preço do produto não pode ser nulo.")
        @DecimalMin(value = "0.0", inclusive = true, message = "O preço do produto deve ser um valor positivo ou zero.")
        BigDecimal price
) {
}
