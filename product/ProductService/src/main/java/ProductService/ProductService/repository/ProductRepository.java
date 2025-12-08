package ProductService.ProductService.repository;

import ProductService.ProductService.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.regex.Pattern;

public interface ProductRepository extends MongoRepository<Product,String> {
    Page<Product> findByNameRegexIgnoreCase(Pattern regex, Pageable pageable);

}
