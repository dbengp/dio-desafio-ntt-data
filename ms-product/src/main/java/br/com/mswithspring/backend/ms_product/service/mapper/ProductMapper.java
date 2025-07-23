package br.com.mswithspring.backend.ms_product.service.mapper;

import br.com.mswithspring.backend.ms_product.model.dto.ProductDto;
import br.com.mswithspring.backend.ms_product.model.entity.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class ProductMapper {

    public Optional<ProductDto> toDto(Product product) {
        if (product == null) {
            return Optional.empty();
        }
        return Optional.of(new ProductDto(product.getName(), product.getDescription(), product.getPrice()));
    }

    public Optional<Product> toEntity(ProductDto productDto) {
        if (productDto == null) {
            return Optional.empty();
        }
        Product product = new Product();
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setPrice(productDto.price());
        return Optional.of(product);
    }

    public Product updateEntityFromDto(Product product, ProductDto productDto) {
        if (productDto != null) {
            product.setName(productDto.name());
            product.setDescription(productDto.description());
            product.setPrice(productDto.price());
        }
        return product;
    }
}
