package com.gdn.project.waroenk.catalog.controller.grpc;

import com.gdn.project.waroenk.catalog.SeedAllProductRequest;
import com.gdn.project.waroenk.catalog.SeedMerchantProductRequest;
import com.gdn.project.waroenk.catalog.SeedResponse;
import com.gdn.project.waroenk.catalog.SeedServiceGrpc;
import com.gdn.project.waroenk.catalog.service.ProductSeedDataGenerator;
import com.gdn.project.waroenk.catalog.service.SearchService;
import com.gdn.project.waroenk.common.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class SeedController extends SeedServiceGrpc.SeedServiceImplBase {

  private final SearchService searchService;
  private final ProductSeedDataGenerator dataGenerator;

  @Override
  public void seedProductForSpecificMerchant(SeedMerchantProductRequest request,
      StreamObserver<SeedResponse> responseObserver) {
    try {
      log.info("Starting async seed of {} products for merchant: {}",
          request.getProductCount(),
          request.getMerchantCode());
      dataGenerator.generateProductsForMerchant(request.getMerchantCode(), request.getProductCount());
      SeedResponse response = SeedResponse.newBuilder()
          .setStatus("STARTED")
          .setMessage(String.format("Seed process for merchant {} started. Check logs for progress.",
              request.getMerchantCode()))
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error seed products for merchants : {}", request.getMerchantCode(), e);
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Merchant search failed: " + e.getMessage())
          .asRuntimeException());
    }
  }

  @Override
  public void seedProductsForAllMerchants(SeedAllProductRequest request,
      StreamObserver<SeedResponse> responseObserver) {
    try {
      log.info("Starting async seed of {} products per merchant for merchants {} to {}",
          request.getProductPerMerchant(),
          request.getBatchStart(),
          request.getBatchEnd());
      dataGenerator.generateProductsForAllMerchantsAsync(request.getProductPerMerchant(),
          request.getBatchStart(),
          request.getBatchEnd());
      SeedResponse response = SeedResponse.newBuilder()
          .setStatus("STARTED")
          .setMessage("Seed process for all merchant started. Check logs for progress.")
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error seed all products", e);
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Merchant search failed: " + e.getMessage())
          .asRuntimeException());
    }
  }

  @Override
  public void indexMerchantsInTypeSense(Empty request, StreamObserver<SeedResponse> responseObserver) {
    try {
      log.info("Starting async TypeSense indexing for merchants");

      dataGenerator.indexAllMerchantsInTypeSenseAsync();
      SeedResponse response = SeedResponse.newBuilder()
          .setStatus("STARTED")
          .setMessage("TypeSense merchant indexing started. Check logs for progress.")
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error indexing all merchant", e);
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Merchant search failed: " + e.getMessage())
          .asRuntimeException());
    }
  }

  @Override
  public void indexProductsInTypeSense(Empty request, StreamObserver<SeedResponse> responseObserver) {
    try {
      log.info("Starting async TypeSense indexing for products");

      dataGenerator.indexAllProductsInTypeSense();
      SeedResponse response = SeedResponse.newBuilder()
          .setStatus("STARTED")
          .setMessage("TypeSense products indexing started. Check logs for progress.")
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error indexing all products", e);
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Merchant search failed: " + e.getMessage())
          .asRuntimeException());
    }
  }

  @Override
  public void indexAllInTypeSense(Empty request, StreamObserver<SeedResponse> responseObserver) {
    try {
      log.info("Starting async TypeSense indexing for all data");

      // Index merchants first, then products
      dataGenerator.indexAllMerchantsInTypeSenseAsync();
      dataGenerator.indexAllProductsInTypeSenseAsync();

      SeedResponse response = SeedResponse.newBuilder()
          .setStatus("STARTED")
          .setMessage("TypeSense indexing started for merchants and products. Check logs for progress.")
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error indexing all data", e);
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Merchant search failed: " + e.getMessage())
          .asRuntimeException());
    }
  }
}
