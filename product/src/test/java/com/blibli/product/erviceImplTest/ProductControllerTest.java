package com.blibli.product.erviceImplTest;

import com.blibli.product.controller.ProductController;
import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.CreateProductResponseDTO;
import com.blibli.product.response.GdnResponse;
import com.blibli.product.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @InjectMocks
    ProductController productController;
    @Mock
    ProductService productService;

    @Test
    public void test_createProduct(){
        CreateProductRequestDTO createProductRequestDTO = new CreateProductRequestDTO();
        createProductRequestDTO.setProductName("productName");
        createProductRequestDTO.setProductSku("test-0001");
        createProductRequestDTO.setProductPrice(1000.0);
        createProductRequestDTO.setProductBrand("brand");
        createProductRequestDTO.setProductCategory("category");
        createProductRequestDTO.setProductDesc("desc");

        List<CreateProductRequestDTO> createProductRequestDTOList = new ArrayList<>();
        createProductRequestDTOList.add(createProductRequestDTO);

        List<CreateProductResponseDTO> createProductResponseDTOList = new ArrayList<>();
        CreateProductResponseDTO createProductResponseDTO = new CreateProductResponseDTO();
        BeanUtils.copyProperties(createProductRequestDTO,createProductResponseDTO);
        createProductResponseDTOList.add(createProductResponseDTO);

        Mockito.when(productService.createProduct(createProductRequestDTOList)).thenReturn(createProductResponseDTOList);

        ResponseEntity<GdnResponse<List<CreateProductResponseDTO>>>  response= productController.createProduct(createProductRequestDTOList);
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().get(0).getProductName().contains("productName"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductSku().contains("test-0001"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductPrice()==1000.0);
        Assertions.assertTrue(response.getBody().getData().get(0).getProductBrand().contains("brand"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductCategory().contains("category"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductDesc().contains("desc"));

    }

    @Test
    public void test_getProductProductById(){
        String productId = "test-0001";

        CreateProductResponseDTO createProductResponseDTO = new CreateProductResponseDTO();
        createProductResponseDTO.setProductName("productName");
        createProductResponseDTO.setProductSku("test-0001");
        createProductResponseDTO.setProductPrice(1000.0);
        createProductResponseDTO.setProductBrand("brand");
        createProductResponseDTO.setProductCategory("category");
        createProductResponseDTO.setProductDesc("desc");

        Mockito.when(productService.findProductById(productId)).thenReturn(createProductResponseDTO);

        ResponseEntity<GdnResponse<CreateProductResponseDTO>> response = productController.getProductProductById(productId);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().getProductName().contains("productName"));
        Assertions.assertTrue(response.getBody().getData().getProductSku().contains("test-0001"));
        Assertions.assertTrue(response.getBody().getData().getProductPrice()==1000.0);
        Assertions.assertTrue(response.getBody().getData().getProductBrand().contains("brand"));
        Assertions.assertTrue(response.getBody().getData().getProductCategory().contains("category"));
        Assertions.assertTrue(response.getBody().getData().getProductDesc().contains("desc"));
    }

    @Test
    public void test_updateProductData(){
        CreateProductRequestDTO createProductRequestDTO = new CreateProductRequestDTO();
        createProductRequestDTO.setProductName("productName");
        createProductRequestDTO.setProductSku("test-0001");
        createProductRequestDTO.setProductPrice(1000.0);
        createProductRequestDTO.setProductBrand("brand");
        createProductRequestDTO.setProductCategory("category");
        createProductRequestDTO.setProductDesc("desc");

        CreateProductResponseDTO createProductResponseDTO = new CreateProductResponseDTO();
        BeanUtils.copyProperties(createProductRequestDTO,createProductResponseDTO);

        Mockito.when(productService.updateProductData(createProductRequestDTO)).thenReturn(createProductResponseDTO);

        ResponseEntity<GdnResponse<CreateProductResponseDTO>> response = productController.updateProductData(createProductRequestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().getProductName().contains("productName"));
        Assertions.assertTrue(response.getBody().getData().getProductSku().contains("test-0001"));
        Assertions.assertTrue(response.getBody().getData().getProductPrice()==1000.0);
        Assertions.assertTrue(response.getBody().getData().getProductBrand().contains("brand"));
        Assertions.assertTrue(response.getBody().getData().getProductCategory().contains("category"));
        Assertions.assertTrue(response.getBody().getData().getProductDesc().contains("desc"));
    }

    @Test
    public void test_findByListProductId(){
        List<String> productId = new ArrayList<>();
        productId.add("test-0001");

        List<CreateProductResponseDTO> createProductResponseDTOList = new ArrayList<>();
        CreateProductResponseDTO createProductResponseDTO = new CreateProductResponseDTO();
        createProductResponseDTO.setProductName("productName");
        createProductResponseDTO.setProductSku("test-0001");
        createProductResponseDTO.setProductPrice(1000.0);
        createProductResponseDTO.setProductBrand("brand");
        createProductResponseDTO.setProductCategory("category");
        createProductResponseDTO.setProductDesc("desc");
        createProductResponseDTOList.add(createProductResponseDTO);

        Mockito.when(productService.findProductByIdList(productId)).thenReturn(createProductResponseDTOList);

        ResponseEntity<GdnResponse<List<CreateProductResponseDTO>>> response = productController.findByListProductId(productId);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody().getData().get(0).getProductName().contains("productName"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductSku().contains("test-0001"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductPrice()==1000.0);
        Assertions.assertTrue(response.getBody().getData().get(0).getProductBrand().contains("brand"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductCategory().contains("category"));
        Assertions.assertTrue(response.getBody().getData().get(0).getProductDesc().contains("desc"));

    }
}
