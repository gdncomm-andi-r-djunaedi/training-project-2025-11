package com.blibli.product.service.impl;

import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.SearchRequestDTO;
import com.blibli.product.dto.SearchResponseDTO;
import com.blibli.product.entity.Product;
import com.blibli.product.response.CustomPageResponse;
import com.blibli.product.service.ProductSolrService;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProductSolrServiceImpl implements ProductSolrService {
    @Autowired
    SolrClient solrClient;

    @Override
    public CustomPageResponse<SearchResponseDTO> search(SearchRequestDTO searchRequestDTO, PageRequest pageable) throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        String keyword = searchRequestDTO.getSearchKeyword();
        solrQuery.setQuery("autocomplete:" + keyword + "* OR productName:*" + keyword + "* OR productSku:*"+keyword+"* OR productDesc:*"+keyword+"* OR productBrand:*"+keyword+"* OR productCategory:*"+keyword+"*");
        solrQuery.setStart(searchRequestDTO.getPageNo()*searchRequestDTO.getSize());
        solrQuery.setRows(searchRequestDTO.getSize());

        log.info("Solr query to search the data "+solrQuery);
        QueryResponse queryResponse = solrClient.query(solrQuery);
        SolrDocumentList productSolrList = queryResponse.getResults();

        List<SearchResponseDTO> searchResponseDTOList = new ArrayList<>();
        for(SolrDocument itr:productSolrList){
            SearchResponseDTO searchResponseDTO = new SearchResponseDTO();
            searchResponseDTO.setProductName((String) itr.getFieldValueMap().get("productName"));
            searchResponseDTO.setProductSku((String) itr.getFieldValueMap().get("productSku"));
            searchResponseDTO.setProductDesc((String) itr.getFieldValueMap().get("productDesc"));
            searchResponseDTO.setProductBrand((String) itr.getFieldValueMap().get("productBrand"));
            searchResponseDTO.setProductCategory((String) itr.getFieldValueMap().get("productCategory"));
            searchResponseDTO.setProductPrice((Double) itr.getFieldValueMap().get("productPrice"));
            searchResponseDTOList.add(searchResponseDTO);
        }
        Page<SearchResponseDTO> page=  new PageImpl<>(searchResponseDTOList,pageable,productSolrList.getNumFound());
        CustomPageResponse<SearchResponseDTO> response = new CustomPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
        return response;
    }

    public void saveProduct(Product product){
        SolrInputDocument solrInputFields = new SolrInputDocument();
        solrInputFields.setField("id",product.getProductSku());
        solrInputFields.setField("productName",product.getProductName());
        solrInputFields.setField("productSku",product.getProductSku());
        solrInputFields.setField("productDesc",product.getProductDesc());
        solrInputFields.setField("productBrand",product.getProductBrand());
        solrInputFields.setField("productCategory",product.getProductCategory());
        solrInputFields.setField("productPrice",product.getProductPrice());
        try {
            log.info("Initiating to save the data to solr ");
            solrClient.add(solrInputFields);
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            log.error("Error while saving the field in Solr"+e);
        }
    }

    @Override
    public Boolean reindex(CreateProductRequestDTO createProductRequestDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(createProductRequestDTO,product);
        product.setId(createProductRequestDTO.getProductSku());
        saveProduct(product);
        return true;
    }
}
