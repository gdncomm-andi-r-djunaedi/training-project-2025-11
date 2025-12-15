product-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── productservice/
│   │   │               ├── ProductServiceApplication.java
│   │   │               ├── config/
│   │   │               │   ├── MongoConfig.java
│   │   │               │   ├── RedisConfig.java
│   │   │               │   └── SwaggerConfig.java
│   │   │               ├── controller/
│   │   │               │   └── ProductController.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── CreateProductRequest.java
│   │   │               │   │   └── UpdateProductRequest.java
│   │   │               │   ├── response/
│   │   │               │   │   ├── ProductResponse.java
│   │   │               │   │   └── PageResponse.java
│   │   │               │   └── ProductImageDto.java
│   │   │               ├── entity/
│   │   │               │   ├── Product.java
│   │   │               │   └── ProductImage.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── ProductNotFoundException.java
│   │   │               │   └── DuplicateSkuException.java
│   │   │               ├── mapper/
│   │   │               │   └── ProductMapper.java
│   │   │               ├── repository/
│   │   │               │   └── ProductRepository.java
│   │   │               └── service/
│   │   │                   ├── ProductService.java
│   │   │                   └── impl/
│   │   │                       └── ProductServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
└── pom.xml