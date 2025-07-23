package br.com.mswithspring.backend.ms_order.dto;

import java.math.BigDecimal;

public record OrderConfirmationItemDto(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
