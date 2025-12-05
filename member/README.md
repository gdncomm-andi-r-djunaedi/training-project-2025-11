# Member Service API Documentation

## Overview
The Member Service handles user registration, authentication, and session management.

## Endpoints

### 1. Register Member
Registers a new user in the system.

*   **URL**: `/api/member/register`
*   **Method**: `POST`
*   **Request Body**:
    ```json
    {
      "username": "newuser",
      "email": "user@example.com",
      "password": "securepassword"
    }
    ```
*   **Response**: `200 OK` with a success message string.

### 2. Login
Authenticates a user and issues a JWT token.

*   **URL**: `/api/member/login`
*   **Method**: `POST`
*   **Request Body**:
    ```json
    {
      "username": "newuser",
      "password": "securepassword"
    }
    ```
*   **Response**: `200 OK`
    *   **Body**: JSON containing the JWT token and user details.
    *   **Cookies**: Sets an HttpOnly cookie named `jwt` containing the token.

### 3. Logout
Logs out the user by invalidating their session/token.

*   **URL**: `/api/member/logout`
*   **Method**: `POST`
*   **Headers**:
    *   `Authorization` (Required): Bearer token (e.g., `Bearer <token>`).
*   **Response**: `200 OK` with "Logged out successfully".
