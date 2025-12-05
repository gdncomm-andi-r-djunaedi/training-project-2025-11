package project_product.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project_product.product.entity.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);

    @Query(value = "SELECT * FROM product WHERE name ILIKE %:keyword%", nativeQuery = true)
    Page<Product> findAllByKeyword(@Param("keyword") Pageable pageable, String keyword);
}
