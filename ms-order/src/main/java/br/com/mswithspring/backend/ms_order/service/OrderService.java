package br.com.mswithspring.backend.ms_order.service;

import br.com.mswithspring.backend.ms_order.client.ProductClient;
import br.com.mswithspring.backend.ms_order.exception.OrderCreationException;
import br.com.mswithspring.backend.ms_order.dto.OrderConfirmationDto;
import br.com.mswithspring.backend.ms_order.dto.OrderConfirmationItemDto;
import br.com.mswithspring.backend.ms_order.dto.OrderDto;
import br.com.mswithspring.backend.ms_order.dto.OrderItemDto;
import br.com.mswithspring.backend.ms_order.dto.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class OrderService {

    private final ProductClient productClient;

    @Autowired
    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public OrderConfirmationDto simulateOrder(OrderDto orderDto) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderConfirmationItemDto> confirmedItems = new ArrayList<>();

        for (OrderItemDto item : orderDto.items()) {
            ProductDto product;
            try {
                product = productClient.getProductById(item.productId());
            } catch (Exception e) {
                throw new OrderCreationException(String.format("Produto com ID %d não encontrado ou indisponível. Detalhes: %s", item.productId(), e.getMessage()));
            }

            if (product == null || product.price() == null) {
                throw new OrderCreationException(String.format("Detalhes do produto com ID %d inválidos (preço ausente).", item.productId()));
            }

            BigDecimal itemSubtotal = product.price().multiply(BigDecimal.valueOf(item.quantity()));
            totalAmount = totalAmount.add(itemSubtotal);

            confirmedItems.add(new OrderConfirmationItemDto(
                    item.productId(),
                    product.name(),
                    product.price(),
                    item.quantity(),
                    itemSubtotal
            ));
        }

        String simulatedOrderId = UUID.randomUUID().toString();

        return new OrderConfirmationDto(
                simulatedOrderId,
                orderDto.customerId(),
                totalAmount,
                LocalDateTime.now(),
                confirmedItems,
                "SIMULATED_SUCCESS",
                "Pedido criado com sucesso."
        );
    }
}
