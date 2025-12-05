package org.jovian.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.jovian.product.entity.Product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, String>{

    boolean existsById(String id);
    List<Product> findByName(String name);
    List<Product> viewAllProducts();
    List<Product> viewAllProductsByCategory(String category);
    List<Product> viewAllProductsByProducerCode(String producerCode);

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.productCategory) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.producerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
