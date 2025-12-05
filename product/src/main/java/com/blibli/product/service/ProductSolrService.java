package com.blibli.product.service;

import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.SearchRequestDTO;
import com.blibli.product.dto.SearchResponseDTO;
import com.blibli.product.entity.Product;
import com.blibli.product.response.CustomPageResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

public interface ProductSolrService {
    CustomPageResponse<SearchResponseDTO> search(SearchRequestDTO searchRequestDTO, PageRequest pageRequest) throws SolrServerException, IOException;
    public void saveProduct(Product product) ;

    Boolean reindex(CreateProductRequestDTO createProductRequestDTO);
}
