package com.blibli.product.controller;

import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.SearchRequestDTO;
import com.blibli.product.dto.SearchResponseDTO;
import com.blibli.product.response.CustomPageResponse;
import com.blibli.product.response.GdnResponse;
import com.blibli.product.service.ProductSolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductSearchController {
    @Autowired
    ProductSolrService productESService;
    @PostMapping("/product/search/byProductName")
    public ResponseEntity<GdnResponse<CustomPageResponse<SearchResponseDTO>>> searchByProductName(@RequestBody SearchRequestDTO searchRequestDTO) throws SolrServerException, IOException {
        return new ResponseEntity<>(new GdnResponse(true,null,productESService.search(searchRequestDTO, PageRequest.of(searchRequestDTO.getPageNo(), searchRequestDTO.getSize(), Sort.Direction.valueOf(searchRequestDTO.getSort()),searchRequestDTO.getSearchKeyword()))), HttpStatus.OK);
    }

    @PostMapping("/product/search/reindexSolr")
    public ResponseEntity<GdnResponse<Boolean>> reindex(@RequestBody CreateProductRequestDTO createProductRequestDTO){
        return new ResponseEntity<>(new GdnResponse<>(true,null,productESService.reindex(createProductRequestDTO)),HttpStatus.OK);
    }


}
