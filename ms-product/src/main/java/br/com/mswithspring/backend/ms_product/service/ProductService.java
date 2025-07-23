package br.com.mswithspring.backend.ms_product.service;

import br.com.mswithspring.backend.ms_product.exception.ProductNotFoundException;
import br.com.mswithspring.backend.ms_product.model.dto.ProductDto;
import br.com.mswithspring.backend.ms_product.model.entity.Product;
import br.com.mswithspring.backend.ms_product.repository.ProductRepository;
import br.com.mswithspring.backend.ms_product.service.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toDto(product)
                .orElseThrow(() -> new IllegalStateException("Erro ao mapear produto para DTO."));
    }

    @Transactional
    public ProductDto save(ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("DTO do produto não pode ser nulo para salvar.");
        }
        if (productDto.name() == null || productDto.name().isBlank()) {
            throw new IllegalArgumentException("Produto deve ter um nome.");
        }
        Product product = productMapper.toEntity(productDto)
                .orElseThrow(() -> new IllegalStateException("Erro ao converter DTO para entidade Product."));
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct)
                .orElseThrow(() -> new IllegalStateException("Erro ao mapear produto salvo para DTO."));
    }

    @Transactional
    public ProductDto update(Long id, ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("DTO do produto não pode ser nulo para atualização.");
        }
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        Product updatedProduct = productMapper.updateEntityFromDto(existingProduct, productDto);
        Product savedProduct = productRepository.save(updatedProduct);
        return productMapper.toDto(savedProduct)
                .orElseThrow(() -> new IllegalStateException("Erro ao mapear produto atualizado para DTO."));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}