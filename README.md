Microservices example (minimal)
Services:
- api-gateway (port 8080)
- member-service (port 8081) - Postgres
- product-service (port 8082) - MongoDB
- cart-service (port 8083) - Postgres

Each service is a standalone Maven Spring Boot project.
To run:
- Ensure PostgreSQL and MongoDB are running locally and reachable with the settings in application.properties.
- From each service folder: mvn spring-boot:run

This zip contains minimal example code to get started. You will likely need to adapt DB credentials and add JWT integration and more robust gateway routing for production use.
