package com.kailash.search;
import com.kailash.search.dto.ProductPayload;
import com.kailash.search.service.SearchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPayload getProductById(@PathVariable String id) {
        return searchService.getById(id);
    }

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,String>> ping() {
        return ResponseEntity.ok(Map.of("ok","true"));
    }

    @GetMapping
    public List<ProductPayload> search(@RequestParam("q") String q) {
        return searchService.searchByName(q);
    }
}
