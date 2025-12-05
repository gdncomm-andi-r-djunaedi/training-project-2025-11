# Product Search Implementation Guide

This document details the implementation of the advanced product search functionality in the Product Microservice. The search feature supports **wildcards**, **case-insensitive matching**, and **pagination**, allowing users to find products by name, description, or category.

## 1. Overview

The search functionality is designed to be flexible and robust:
-   **Wildcard Support**: Users can use `*` (match any sequence) and `?` (match single character).
-   **Multi-Field Search**: The search term is applied simultaneously to `name`, `description`, and `category`.
-   **Pagination**: Results are paginated to handle large datasets efficiently.
-   **Case-Insensitivity**: "Apple" and "apple" yield the same results.

## 2. Architecture Flow

The request travels through the standard Spring Boot layers:

1.  **Controller Layer** (`ProductController`): Receives the HTTP request.
2.  **Service Layer** (`ProductService`): Validates input and orchestrates the logic.
3.  **Repository Layer** (`ProductRepository`): Defines the interface.
4.  **Custom Implementation** (`ProductCustomRepositoryImpl`): Contains the core logic using `MongoTemplate`.
5.  **Database**: MongoDB executes the query.

## 3. Implementation Details

### 3.1 Controller Layer
**File**: `ProductController.java`

The controller exposes a `POST` endpoint `/product/search`. It accepts `page`, `size`, and `searchTerm` as parameters.

```java
@PostMapping("/search")
public ResponseEntity<ProductPageResponse> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String searchTerm) throws Exception {
    ProductPageResponse response = productService.searchProducts(searchTerm, page, size);
    return new ResponseEntity<>(response, HttpStatus.OK);
}
```

### 3.2 Service Layer
**File**: `ProductServiceImpl.java`

The service layer validates the pagination parameters and the search term. It then calls the repository.

```java
public ProductPageResponse searchProducts(String searchTerm, int page, int size) throws Exception {
    // Validation logic...
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> productPage = productRepository.searchProducts(searchTerm, pageable);
    // Convert to DTO and return...
}
```

### 3.3 Custom Repository Logic (The Core)
**File**: `ProductCustomRepositoryImpl.java`

This is where the wildcard logic resides. Since standard Spring Data repositories don't natively support complex wildcard-to-regex transformation with OR conditions across multiple fields, we use `MongoTemplate`.

#### Key Logic: Wildcard to Regex Conversion
To make the search safe and functional, we convert user-friendly wildcards into MongoDB-compatible Regular Expressions.

-   `*` becomes `.*` (match anything).
-   `?` becomes `.` (match one character).
-   Special regex characters (like `+`, `(`, `)`) are escaped to prevent errors.

```java
private String createRegexPattern(String searchTerm) {
    StringBuilder sb = new StringBuilder();
    for (char c : searchTerm.toCharArray()) {
        if (c == '*') {
            sb.append(".*");
        } else if (c == '?') {
            sb.append(".");
        } else if ("\\^$.|+()[]{}".indexOf(c) != -1) {
            sb.append('\\').append(c); // Escape special chars
        } else {
            sb.append(c);
        }
    }
    return sb.toString();
}
```

#### Key Logic: Building the Query
We use Spring's `Criteria` API to build an `OR` query.

```java
Query query = new Query();
String regexPattern = createRegexPattern(searchTerm);
Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);

Criteria criteria = new Criteria().orOperator(
        Criteria.where("name").regex(pattern),
        Criteria.where("description").regex(pattern),
        Criteria.where("category").regex(pattern));

query.addCriteria(criteria);
```

## 4. Usage Examples

### Scenario 1: Basic Search
*   **Input**: `apple`
*   **Matches**: "Apple iPhone", "Green Apple", "Pineapple" (if description contains it).

### Scenario 2: Wildcard Search
*   **Input**: `ph*ne`
*   **Matches**: "Phone", "Phantom", "Phonetic".
*   **Regex Generated**: `ph.*ne`

### Scenario 3: Single Character Wildcard
*   **Input**: `b?ll`
*   **Matches**: "ball", "bell", "bill".
*   **Regex Generated**: `b.ll`

### Scenario 4: Special Characters
*   **Input**: `C++`
*   **Matches**: "C++ Programming Book".
*   **Regex Generated**: `C\+\+` (Safe from regex errors).
