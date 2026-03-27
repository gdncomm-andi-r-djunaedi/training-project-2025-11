package com.ecom.product.Service.Impl;

import com.ecom.product.Dto.ProductDto;
import com.ecom.product.Entitiy.Product;
import com.ecom.product.Repository.ProductRepo;
import com.ecom.product.Service.ProductService;
import com.ecom.product.exception.NoDataFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepo productRepo;

    @Override
    public ProductDto findBySku(String sku) {
        List<Product> productsList = productRepo.findAll();

        for(Product p : productsList){
            if(p.getProductSku().equals(sku)){
                ProductDto productDto = new ProductDto();
                BeanUtils.copyProperties(p, productDto);
                return productDto;
            }
        }
        throw new NoDataFoundException("No Product found with "+sku+"");
    }

    @Override
    public Page<ProductDto> findAllByName(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (search == null || search.isBlank()) {
            throw new NoDataFoundException("No Product found with this search term");
        }

        String trimmedSearch = search.trim();
        Page<Product> products = productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(trimmedSearch, trimmedSearch, pageable);

        if (products.isEmpty()) {
            String cleanedSearch = removeRegexCharacters(trimmedSearch);
            if (!cleanedSearch.equals(trimmedSearch) && !cleanedSearch.isBlank()) {
                products = productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(cleanedSearch, cleanedSearch, pageable);
            }
            
            if (products.isEmpty()) {
                throw new NoDataFoundException("No Product found with term : " + search + "");
            }
        }

        return products.map(product -> convertToDto(product));
    }

    private String removeRegexCharacters(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return searchTerm;
        }
        // Remove common regex characters: . * + ? ^ $ [ ]
        return searchTerm.replaceAll("[@.*+?^$\\[\\]]", "");
    }

    public ProductDto convertToDto(Product product){
        ProductDto productDto = new ProductDto();
        BeanUtils.copyProperties(product, productDto);
        return productDto;
    }


}
