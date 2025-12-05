# Product Service API Documentation

## Overview
The Product Service manages the product catalog, allowing for creation, retrieval, and searching of products.

## Endpoints

### 1. Search Products
Searches for products based on a query string with pagination support.

*   **URL**: `/api/product/search`
*   **Method**: `GET`
*   **Query Parameters**:
    *   `query` (Required): The search term (e.g., "phone").
    *   `page` (Optional): Page number (0-indexed, default 0).
    *   `size` (Optional): Number of items per page (default 10).
*   **Response**: `200 OK` returns a `Page<Product>` object containing the list of products and pagination metadata.

### 2. Get Product by ID
Retrieves detailed information for a specific product.

*   **URL**: `/api/product/{id}`
*   **Method**: `GET`
*   **Path Parameters**:
    *   `id`: The unique identifier of the product.
*   **Response**: `200 OK` returns the `Product` entity.

### 3. Create Product
Adds a new product to the catalog.

*   **URL**: `/api/product`
*   **Method**: `POST`
*   **Request Body**:
    ```json
    {
      "name": "Smartphone",
      "description": "Latest model",
      "price": 999.99,
      "stock": 100
    }
    ```
*   **Response**: `200 OK` returns the created `Product` entity.
