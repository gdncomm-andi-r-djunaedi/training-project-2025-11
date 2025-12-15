package com.gdn.marketplace.product.grpc;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    @Autowired
    private ProductService productService;

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            Product product = productService.getProduct(request.getId());
            ProductResponse response = mapToProductResponse(product);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            // Stock is not in Product entity anymore based on previous steps, but it is in
            // proto.
            // Ignoring stock for now as it was removed from Product entity.

            Product savedProduct = productService.createProduct(product);
            ProductResponse response = mapToProductResponse(savedProduct);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void searchProducts(SearchProductsRequest request, StreamObserver<SearchProductsResponse> responseObserver) {
        try {
            List<com.gdn.marketplace.product.document.ProductDocument> documents = productService
                    .searchProducts(request.getQuery());

            List<ProductResponse> productResponses = documents.stream()
                    .map(this::mapDocumentToProductResponse)
                    .collect(Collectors.toList());

            SearchProductsResponse response = SearchProductsResponse.newBuilder()
                    .addAllProducts(productResponses)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription() != null ? product.getDescription() : "")
                .setPrice(product.getPrice() != null ? product.getPrice() : 0.0)
                .build();
    }

    private ProductResponse mapDocumentToProductResponse(com.gdn.marketplace.product.document.ProductDocument doc) {
        return ProductResponse.newBuilder()
                .setId(doc.getId())
                .setName(doc.getName())
                .setDescription(doc.getDescription() != null ? doc.getDescription() : "")
                .setPrice(doc.getPrice() != null ? doc.getPrice() : 0.0)
                .build();
    }
}
