package com.training.marketplace.product.service;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.controller.modal.request.GetProductListRequest;
import com.training.marketplace.product.controller.modal.request.GetProductListResponse;
import com.training.marketplace.product.entity.Product;
import com.training.marketplace.product.entity.ProductEntity;
import com.training.marketplace.product.entity.ProductListItem;
import com.training.marketplace.product.repository.ProductRepository;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@GrpcService
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void getProductDetail(GetProductDetailRequest request, StreamObserver<GetProductDetailResponse> responseObserver) {

        Optional<ProductEntity> result = this.productRepository.findByProductId(request.getProductId());

        if (result.isEmpty()) {
            Metadata.Key<GetProductDetailRequest> requestBody = ProtoUtils.keyForProto(GetProductDetailRequest.getDefaultInstance());
            Metadata metadata = new Metadata();
            metadata.put(requestBody, request);
            responseObserver.onError(Status.NOT_FOUND.withDescription("Product not found").asRuntimeException(metadata));
        }

        GetProductDetailResponse response =
                GetProductDetailResponse.newBuilder()
                        .setProduct(Product.newBuilder()
                                .setProductId(result.get().getProductId())
                                .setProductName(result.get().getProductName())
                                .setProductPrice(result.get().getProductPrice().doubleValue())
                                .setProductDetail(result.get().getProductDetail())
                                .setProductNotes(result.get().getProductNotes())
                                .setProductImage(result.get().getProductImage())
                                .build())
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getProductList(GetProductListRequest request, StreamObserver<GetProductListResponse> responseObserver) {
        String parsedQuery = request.getQuery();

        Pageable pageable = PageRequest.of(request.getPage(), request.getItemPerPage());

        Page<ProductEntity> result;

        if (parsedQuery.isEmpty() || parsedQuery == null){
            result = productRepository.findAll(pageable);
        } else {
            result = productRepository.findByProductNameLike(request.getQuery(),pageable);
        }

        List<ProductListItem> responseValue = new ArrayList<>();

        for (ProductEntity entity:result.getContent()){
            responseValue.add(
                    ProductListItem.newBuilder()
                            .setProductId(entity.getProductId())
                            .setProductName(entity.getProductName())
                            .setProductPrice(entity.getProductPrice().doubleValue())
                            .setProductImage(entity.getProductImage())
                            .build()
            );
        }

        GetProductListResponse response = GetProductListResponse.newBuilder().addAllProductList(responseValue).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
