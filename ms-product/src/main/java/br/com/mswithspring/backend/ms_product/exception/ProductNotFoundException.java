package br.com.mswithspring.backend.ms_product.exception;

public class ProductNotFoundException extends RuntimeException {
  public ProductNotFoundException(Long id) {
    super(String.format("Produto com ID %d não encontrado.", id));
  }
}
