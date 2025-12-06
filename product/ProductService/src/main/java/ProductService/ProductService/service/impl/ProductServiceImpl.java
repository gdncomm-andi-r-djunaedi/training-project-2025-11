package ProductService.ProductService.service.impl;

import ProductService.ProductService.dto.ProductRequestDto;
import ProductService.ProductService.dto.ProductResponseDto;
import ProductService.ProductService.entity.Product;
import ProductService.ProductService.exception.ProductNotFoundException;
import ProductService.ProductService.mapper.ProductMapper;
import ProductService.ProductService.repository.ProductRepository;
import ProductService.ProductService.service.ProductService;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductMapper mapper;

    @Autowired
    ProductRepository productRepository;

    @Override
    public ProductResponseDto addProduct(ProductRequestDto productRequestDto){
        Product product = mapper.toEntity(productRequestDto);
        product.setCreatedAt(LocalDate.now());
        Product savedProduct = productRepository.save(product);
        return mapper.toDto(savedProduct);
    }

    @Override
    public ProductResponseDto getProductById(String id){
       Product product = productRepository.findById(id)
               .orElseThrow(()->new ProductNotFoundException("Product not found with id:- "+id));
       return mapper.toDto(product);
    }

    @Override
    public Page<ProductResponseDto> getProducts(int page, int size){
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(mapper::toDto);
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page,size);

        //wildcardSearch
        String regexPattern = ".*" + Pattern.quote(keyword) + ".*";
        Pattern regex = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);

        Page<Product> products = productRepository.findByNameRegexIgnoreCase(regex,pageable);
        return products.map(mapper::toDto);
    }

    @Override
    public ProductResponseDto updateProduct(String id, ProductRequestDto dto){
        Product product = productRepository.findById(id)
                .orElseThrow(()->new ProductNotFoundException("Product not found with id:- "+id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        productRepository.save(product);
        return mapper.toDto(product);
    }

    @Override
    public void deleteProduct(String id){
      if(!productRepository.existsById(id)){
         throw new ProductNotFoundException("Product not found with id:- "+id);
      }
      productRepository.deleteById(id);
    }

    @Override
    public String generateBulkProducts(int count) {
        Faker faker = new Faker();
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setId(UUID.randomUUID().toString());
            product.setName(faker.commerce().productName());
            product.setDescription(faker.lorem().sentence());
            product.setPrice(Double.parseDouble(faker.commerce().price()));
            product.setCreatedAt(LocalDate.now());
            products.add(product);

            // Save in batches of 1000
            if (products.size() == 1000) {
                productRepository.saveAll(products);
                products.clear();
            }
        }

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }

        return count + " products inserted successfully!";
    }

}
