# Cart Service API Documentation

## Overview
The Cart Service manages the shopping cart operations for the marketplace.

## Endpoints

### 1. Add Item to Cart
Adds a new item to the user's cart or updates the quantity if the item already exists.

*   **URL**: `/api/cart/add`
*   **Method**: `POST`
*   **Headers**:
    *   `X-User-Name` (Required): The username of the current user.
*   **Request Body**:
    ```json
    {
      "memberId": 123,
      "productId": "PROD-001",
      "quantity": 2
    }
    ```
*   **Response**: `200 OK` returns the updated `Cart` entity.

### 2. Get Cart
Retrieves the current user's shopping cart details.

*   **URL**: `/api/cart`
*   **Method**: `GET`
*   **Headers**:
    *   `X-User-Name` (Required): The username of the current user.
*   **Response**: `200 OK`
    ```json
    {
      "id": 1,
      "cart": {
        "id": 1,
        "username": "TESTUSER",
        "items": [
          {
            "id": 10,
            "product_id": "PROD-001",
            "quantity": 2
          }
        ]
      }
    }
    ```

### 3. Remove Item from Cart
Removes a specific product from the user's cart.

*   **URL**: `/api/cart/remove/{productId}`
*   **Method**: `DELETE`
*   **Headers**:
    *   `X-User-Name` (Required): The username of the current user.
*   **Path Parameters**:
    *   `productId`: The unique identifier of the product to remove.
*   **Response**: `200 OK` returns the updated `Cart` entity.
