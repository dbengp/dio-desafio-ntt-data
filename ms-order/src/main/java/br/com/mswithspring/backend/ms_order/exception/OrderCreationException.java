package br.com.mswithspring.backend.ms_order.exception;

public class OrderCreationException extends RuntimeException {
    public OrderCreationException(String message) {
        super(message);
    }
}
