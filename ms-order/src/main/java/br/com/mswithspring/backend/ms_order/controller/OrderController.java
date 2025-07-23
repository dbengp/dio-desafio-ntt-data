package br.com.mswithspring.backend.ms_order.controller;

import br.com.mswithspring.backend.ms_order.dto.OrderConfirmationDto;
import br.com.mswithspring.backend.ms_order.dto.OrderDto;
import br.com.mswithspring.backend.ms_order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<OrderConfirmationDto> simulateOrder(@Valid @RequestBody OrderDto orderDto) {
        OrderConfirmationDto confirmation = orderService.simulateOrder(orderDto);
        return ResponseEntity.status(HttpStatus.OK).body(confirmation);
    }
}
