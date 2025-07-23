package br.com.mswithspring.backend.ms_product.model.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@SuppressWarnings("serial")
@Entity
@Table(name = "tb_products")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "price", precision = 10, scale = 2) // Exemplo: 99999999.99
    private BigDecimal price;

    public Product() {
    }

    public Product(Long id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() { return price; }

    public void setPrice(BigDecimal price) { this.price = price; }

    @java.lang.Override
    public java.lang.String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}