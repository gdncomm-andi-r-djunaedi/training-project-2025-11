package com.blibli.product.erviceImplTest;


import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.CreateProductResponseDTO;
import com.blibli.product.dto.SearchRequestDTO;
import com.blibli.product.dto.SearchResponseDTO;
import com.blibli.product.entity.Product;
import com.blibli.product.entity.ProductSolr;
import com.blibli.product.repository.ProductRepository;
import com.blibli.product.response.CustomPageResponse;
import com.blibli.product.service.ProductSolrService;
import com.blibli.product.service.impl.ProductServiceImpl;
import com.blibli.product.service.impl.ProductSolrServiceImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @InjectMocks
    ProductServiceImpl productServiceImpl;
    @InjectMocks
    ProductSolrServiceImpl productSolrServiceImpl;
    @Mock
    ProductRepository productRepository;
    @Mock
    ProductSolrService productSolrService;
    @Mock
    SolrClient solrClient;
    @Test
    public void test_createProduct(){
        List< CreateProductRequestDTO> createProductRequestDTOList = new ArrayList<>();
        CreateProductRequestDTO createProductRequestDTO = new CreateProductRequestDTO();
        createProductRequestDTO.setProductName("test");
        createProductRequestDTO.setProductBrand("brand");
        createProductRequestDTO.setProductCategory("category");
        createProductRequestDTO.setProductDesc("desc");
        createProductRequestDTO.setProductPrice(1000.0);
        createProductRequestDTOList.add(createProductRequestDTO);

        Product product = new Product();
        BeanUtils.copyProperties(createProductRequestDTO,product);

        Mockito.when(productRepository.findByProductSku(createProductRequestDTO.getProductSku())).thenReturn(null);

        Mockito.when(productRepository.save(product)).thenReturn(product);
        CreateProductResponseDTO response =  productServiceImpl.createProduct(createProductRequestDTOList).get(0);
        Assertions.assertNotNull(response);

    }

    @Test
    public void test_findProductById(){
        String productId = "asdf";
        Product product = new Product();
        product.setProductName("test");
        product.setProductBrand("brand");
        product.setProductCategory("category");
        product.setProductDesc("desc");
        product.setProductPrice(1000.0);
        product.setId("asdf");
        Mockito.when(productRepository.findByProductSku(productId)).thenReturn(product);
        CreateProductResponseDTO createProductResponseDTO = productServiceImpl.findProductById(productId);
        Assertions.assertNotNull(createProductResponseDTO);

    }

    @Test
    public void test_updateProductData(){
        CreateProductRequestDTO createProductRequestDTO = new CreateProductRequestDTO();
        createProductRequestDTO.setProductName("test");
        createProductRequestDTO.setProductBrand("brand");
        createProductRequestDTO.setProductCategory("category");
        createProductRequestDTO.setProductDesc("desc");
        createProductRequestDTO.setProductPrice(1000.0);

        Product product = new Product();
        BeanUtils.copyProperties(createProductRequestDTO,product);

        Mockito.when(productRepository.findByProductSku(createProductRequestDTO.getProductSku())).thenReturn(product);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        CreateProductResponseDTO responseDTO = productServiceImpl.updateProductData(createProductRequestDTO);
        Assertions.assertNotNull(responseDTO);
    }

    @Test
    public void test_findProductByIdList(){
        List<String> products=  new ArrayList<>();
        products.add("asdf");

        List<Product> productList = new ArrayList<>();
        Product product = new Product();
        product.setProductName("test");
        product.setProductBrand("brand");
        product.setProductCategory("category");
        product.setProductDesc("desc");
        product.setProductPrice(1000.0);
        productList.add(product);

        Mockito.when(productRepository.findByProductSkuIn(products)).thenReturn(productList);
        List<CreateProductResponseDTO> responseDTOList = productServiceImpl.findProductByIdList(products);
        Assertions.assertNotNull(responseDTOList);
    }

    @Test
    public void test_search() throws SolrServerException, IOException {
        ProductSolr productSolr = new ProductSolr();
        productSolr.setProductName("test");
        productSolr.setProductBrand("brand");
        productSolr.setProductCategory("category");
        productSolr.setProductDesc("desc");
        productSolr.setProductPrice(1000.0);
        SolrQuery solrQuery=new SolrQuery();
        String keyword = "asdf";
        solrQuery.setQuery("autocomplete:" + keyword + "* OR productName:*" + keyword + "* OR productSku:*"+keyword+"* OR productDesc:*"+keyword+"* OR productBrand:*"+keyword+"* OR productCategory:*"+keyword+"*");
//        solrQuery.set("q.op", "OR");
//        solrQuery.set("defType", "lucene");
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField("productName","asdf");
        solrDocument.setField("productBrand","brand");
        solrDocument.setField("productCategory","category");
        solrDocument.setField("productDesc","desc");
        SolrDocumentList solrDocuments = new SolrDocumentList();
        solrDocuments.add(solrDocument);
        // Mock QueryResponse
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocuments);

        // Fix: Match any SolrQuery
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class)))
                .thenReturn(queryResponse);
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setSearchKeyword("asdf");
        searchRequestDTO.setSize(1);
        searchRequestDTO.setPageNo(0);

        CustomPageResponse<SearchResponseDTO> response = productSolrServiceImpl.search(searchRequestDTO,PageRequest.of(0,1));
        Assertions.assertNotNull(response);
    }
}
