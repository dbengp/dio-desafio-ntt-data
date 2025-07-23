package br.com.mswithspring.backend.ms_product.controller;

import br.com.mswithspring.backend.ms_product.model.dto.ProductDto;
import br.com.mswithspring.backend.ms_product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> findAll() {
        List<ProductDto> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable @Positive(message = "O ID do produto deve ser um número positivo.") Long id) {
        ProductDto product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductDto> save(@Valid @RequestBody ProductDto productDto) {
        ProductDto savedProduct = productService.save(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable @Positive(message = "O ID do produto deve ser um número positivo.") Long id, @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.update(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Positive(message = "O ID do produto deve ser um número positivo.") Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}