package br.com.mswithspring.backend.ms_order.client;

import br.com.mswithspring.backend.ms_order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "MS-PRODUCT")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}
