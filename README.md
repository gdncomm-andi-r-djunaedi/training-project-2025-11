**Project Summary**

A microservices-based e-commerce system built with Spring Boot, where an API Gateway serves as the centralized entry point for authentication, routing, request filtering, and security enforcement.
The gateway uses JWT authentication, routes requests to downstream services, and enforces authorization for protected operations like cart management.

**System Architecture Overview**

The system follows a loosely coupled microservices architecture containing:

API Gateway (Port 8999) – Central entry point

Member Service (Port 8081) – User registration & authentication

Product Service (Port 8004) – Product catalog management

Cart Service (Port 8085) – User shopping cart

Search Service – Elasticsearch-based product search

Kafka Message Broker – Event streaming for product updates

**Databases**

PostgreSQL (Member, Product)

MongoDB (Cart)

Elasticsearch (Search Indexing)

Find the Data Model, api specs and architecture diagram here - https://gdncomm.atlassian.net/wiki/spaces/PROJ/pages/edit-v2/1798373458?draftShareId=56d00935-3a1b-4402-99c7-d1483b39ce59

The API Gateway contains several core components. SecurityConfig defines public and protected routes and configures the filter chain. JwtFilter validates JWT tokens from the Authorization header, extracts the userId, and sets the Spring Security authentication context. The CartUserIdGatewayFilter runs after JwtFilter for cart-related endpoints, extracts the userId from the authenticated context, and injects an X-User-Id header into requests sent to the Cart Service using HttpServletRequestWrapper. The AuthController handles the login endpoint, makes Feign calls to the Member Service for credential validation, and generates JWT tokens upon successful authentication. The JWTService generates tokens with the userId as the subject, validates signatures and expiration, and sets tokens to expire after one hour.

The Member Service runs on port 8081 and manages user registration and authentication validation. It stores user data in PostgreSQL and exposes endpoints for registration and login validation. When a client registers via the gateway, the request is routed to the Member Service, which stores the user in PostgreSQL and returns a success response. For login, the gateway’s AuthController uses a Feign client to send credentials to the Member Service, which validates them against the database and returns the authentication result along with the userId if successful.

The Product Service runs on port 8004 and manages the product catalog. It stores product data in PostgreSQL and provides endpoints for retrieving and updating products. All product endpoints are public and do not require authentication. When clients request product information, the gateway routes these requests to the Product Service, which queries PostgreSQL and returns product data. When products are updated, the Product Service publishes events to Kafka to enable asynchronous communication with other services.

The Cart Service runs on port 8085 and handles shopping cart operations. It stores cart data in MongoDB and requires authentication for all its endpoints. When a cart request is made, the gateway’s JwtFilter validates the JWT token, extracts the userId, and sets the authentication context. Then the CartUserIdGatewayFilter extracts that userId and injects it as an X-User-Id header before forwarding the request to the Cart Service. The Cart Service uses this header to identify the user and perform cart operations on MongoDB. It also uses Feign to call the Product Service for product details when needed for cart-related calculations.

The Search Service handles product search using Elasticsearch. It consumes product update events from Kafka, which are published by the Product Service whenever a product is created or updated. This event-driven approach allows the Search Service to update the Elasticsearch index asynchronously, ensuring search results remain up to date without slowing down product operations.

Kafka acts as the message broker for asynchronous event streaming. When a product is updated, the Product Service publishes an event to Kafka. The Search Service consumes these events and updates its Elasticsearch index. This pattern supports scalability and loose coupling between services.

The authentication flow works as follows: clients register through the gateway, which forwards the request to the Member Service for database storage. For login, clients send credentials to the gateway’s login endpoint. The gateway uses a Feign client to validate these credentials with the Member Service. If valid, the gateway generates a JWT containing the userId and returns it to the client. The client stores this token and includes it in the Authorization header as Bearer <token> for future protected requests.

For protected endpoints (like cart operations), the gateway validates the JWT, extracts the userId, and injects it as an X-User-Id header before routing the request. This design prevents clients from manipulating the userId and ensures only authenticated users can access their own cart data. The system uses stateless authentication with JWT tokens, eliminating the need for server-side sessions and enabling horizontal scaling.

Overall, the system follows microservices best practices. Each service maintains its own database (PostgreSQL for Member and Product Services, MongoDB for the Cart Service, and Elasticsearch for the Search Service). Services communicate through structured APIs, and the architecture enables independent deployment and scaling. The API Gateway centralizes cross-cutting concerns such as authentication, routing, and security, while downstream services focus purely on business logic. The combination of synchronous communication (Feign) and asynchronous communication (Kafka) provides flexibility for real-time operations and background processing, supporting a scalable and maintainable architecture.
