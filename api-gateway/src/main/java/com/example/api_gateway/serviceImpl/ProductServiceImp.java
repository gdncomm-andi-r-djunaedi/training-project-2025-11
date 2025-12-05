package com.example.api_gateway.serviceImpl;

import com.example.api_gateway.client.ProductServiceClient;
import com.example.api_gateway.response.ProductListResponse;
import com.example.api_gateway.request.ProductRequest;
import com.example.api_gateway.response.ProductResponse;
import com.example.api_gateway.request.UpdateProductRequest;
import com.example.api_gateway.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImp implements ProductService {

    @Autowired
    ProductServiceClient productServiceClient;

    @Override
    public List<ProductResponse> addproducts(List<ProductRequest> productRequests){
        return productServiceClient.addProducts(productRequests).getBody();
    }

    @Override
    public ProductListResponse getProductsListing(int pageNumber, int size){
        return productServiceClient.getProductsListing(pageNumber,size).getBody();
    }

    @Override
    public ProductResponse getProductDetailByItemSku(String itemSku){
        return productServiceClient.getProductDetailByItemSku(itemSku).getBody();
    }

    @Override
    public ProductListResponse getProductBySearchTerm(String searchTerm, int pageNumber,int pageSize){
        return productServiceClient.getProductsBySearchTerm(searchTerm,pageNumber,pageSize).getBody();
    }

    @Override
    public ProductResponse updateProduct(String itemSku, UpdateProductRequest updateRequest){
        return productServiceClient.updateProduct(itemSku,updateRequest).getBody();
    }

    @Override
    public void deleteproductByItemSku(String itemSku){
        productServiceClient.deleteProductByItemSku(itemSku);
    }
}
