package com.blibli.product.service.impl;

import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.CreateProductResponseDTO;
import com.blibli.product.entity.Product;
import com.blibli.product.repository.ProductRepository;
import com.blibli.product.service.ProductService;
import com.blibli.product.service.ProductSolrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductSolrService productSolrService;
    @Override
    public List<CreateProductResponseDTO> createProduct(List<CreateProductRequestDTO> createProductRequestDTOlist) {
        List<CreateProductResponseDTO> createProductResponseDTOList = new ArrayList<>();
        for(CreateProductRequestDTO createProductRequestDTO : createProductRequestDTOlist) {
            if(createProductRequestDTO.getProductSku()==null)
                throw new RuntimeException("Invalid product sku to create an product");
            if(createProductRequestDTO.getProductName()== null)
                throw new RuntimeException("Invalid product name");
            if(createProductRequestDTO.getProductPrice()==null|| createProductRequestDTO.getProductPrice()<=0)
                throw new RuntimeException("Product price is invalid");
            if(createProductRequestDTO.getProductCategory()==null )
                throw new RuntimeException("Invalid product category data");
            if (productRepository.findByProductSku(createProductRequestDTO.getProductSku()) != null) {
                throw new RuntimeException("Sku id is already present");
            }
            Product product = convertFromDTO(createProductRequestDTO);
            log.info("Saving the create product request to DB");
            Product response = productRepository.save(product);
            createProductResponseDTOList.add(convertFromEntity(response));
            log.info("Saving the create product request to SOLR")
            productSolrService.saveProduct(product);
        }
        return createProductResponseDTOList;
    }

    @Override
    @Cacheable(value = "products", key = "#productId", condition = "#productId!=null")
    public CreateProductResponseDTO findProductById(String productId) {
        log.info("Initiating the repo call to find the data ");
        Product response = productRepository.findByProductSku(productId);
        if(response == null)
            throw new RuntimeException("Invalid product ID");
        CreateProductResponseDTO createProductResponseDTO = convertFromEntity(response);
        return createProductResponseDTO;
    }

    @Override
    @CacheEvict(value = "products", key = "#createProductRequestDTO.productSku", condition = "#createProductRequestDTO.productSku != null")
    public CreateProductResponseDTO updateProductData(CreateProductRequestDTO createProductRequestDTO) {
        if(createProductRequestDTO.getProductSku()==null)
            throw new RuntimeException("Invalid product sku to create an product");
        if(createProductRequestDTO.getProductName()== null)
            throw new RuntimeException("Invalid product name");
        if(createProductRequestDTO.getProductPrice()==null|| createProductRequestDTO.getProductPrice()<=0)
            throw new RuntimeException("Product price is invalid");
        if(createProductRequestDTO.getProductCategory()==null )
            throw new RuntimeException("Invalid product category data");

        log.info("Initiating the repo call to find the data ");
        Product prodResponse = productRepository.findByProductSku(createProductRequestDTO.getProductSku());
        if(prodResponse==null){
            throw new RuntimeException("Product sku is not present");
        }
        BeanUtils.copyProperties(createProductRequestDTO,prodResponse);
        Product response =  productRepository.save(prodResponse);
        CreateProductResponseDTO responseDTO = convertFromEntity(response);
        productSolrService.saveProduct(response);
        return responseDTO;
    }

    @Override
    public List<CreateProductResponseDTO> findProductByIdList(List<String> productIds) {
        if(productIds.size()>45)
            throw new RuntimeException("Product id should be less than 45");
        List<Product> productList = productRepository.findByProductSkuIn(productIds);
        List<CreateProductResponseDTO> responseDTOList = new ArrayList<>();
        for(Product itr: productList){
            CreateProductResponseDTO createProductResponseDTO = new CreateProductResponseDTO();
            BeanUtils.copyProperties(itr,createProductResponseDTO);
            responseDTOList.add(createProductResponseDTO);
        }
        return responseDTOList;
    }

    private CreateProductResponseDTO convertFromEntity(Product response) {
        CreateProductResponseDTO createProductResponseDTO=new CreateProductResponseDTO();
        BeanUtils.copyProperties(response,createProductResponseDTO);
        return createProductResponseDTO;
    }

    private Product convertFromDTO(CreateProductRequestDTO createProductRequestDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(createProductRequestDTO,product);
        return product;
    }

}
