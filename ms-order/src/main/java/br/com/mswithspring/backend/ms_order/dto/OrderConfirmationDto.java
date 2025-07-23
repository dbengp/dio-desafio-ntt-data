package br.com.mswithspring.backend.ms_order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderConfirmationDto(
        String orderId,
        Long customerId,
        BigDecimal totalAmount,
        LocalDateTime orderDate,
        List<OrderConfirmationItemDto> confirmedItems,
        String status,
        String message
) {
}
