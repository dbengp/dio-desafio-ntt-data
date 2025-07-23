package br.com.mswithspring.backend.ms_product.repository;

import br.com.mswithspring.backend.ms_product.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
