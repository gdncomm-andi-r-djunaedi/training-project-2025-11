Services:
- api-gateway (port 8080)
- member-service (port 8081) - Postgres
- product-service (port 8082) - MongoDB
- cart-service (port 8083) - Postgres

Run
- Ensure PostgreSQL and MongoDB are running locally and reachable with the settings in application.properties.
- From each service folder: mvn spring-boot:run
