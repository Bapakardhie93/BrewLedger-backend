# BrewLedger Backend

This is the backend API service for the BrewLedger application. Built with Spring Boot and PostgreSQL, it provides robust and secure endpoints for the application frontend.

## Prerequisites

- **Java**: 21 (OpenJDK or equivalent)
- **Maven**: 3.x (Or use the included `./mvnw` wrapper)
- **Database**: PostgreSQL (Locally or cloud, such as Supabase)

## Folder Structure

The project follows a standard Spring Boot layered architecture:
- `controller/` - REST API Endpoints
- `service/` - Business logic and use case implementations
- `repository/` - Data access layer (Spring Data JPA)
- `entity/` - JPA Database Models
- `dto/` - Data Transfer Objects (Request/Response models)
- `security/` - JWT Authentication & Authorization filters
- `config/` - Spring configuration classes
- `exception/` - Global error handling
- `enums/` - Constants and Enum types

## Environment Setup

The application uses `spring-dotenv` to manage secrets. This prevents committing sensitive database credentials or JWT keys to version control.

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
2. Edit `.env` and fill in your specific local or cloud configuration:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/brewledger
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   ADMIN_FULLNAME=Admin
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=admin
   JWT_SECRET=your_256_bit_secret_key_goes_here
   ```

> **Note:** `.env` is ignored by Git, ensuring secrets remain safe on your local machine.

## Running the Application

### Development Mode
By default, the application runs on the `default` profile, which uses the configurations in `application.properties`. This mode runs `ddl-auto=update` and shows SQL logs.

Run via Maven wrapper:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8081`.

### Production Mode
For production, you should use the `prod` profile, which relies on `application-prod.properties`. It applies optimizations like disabling SQL logging and validating the schema instead of automatically updating it.

To run with the production profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Or, if running a built JAR:
```bash
./mvnw clean package
java -jar -Dspring.profiles.active=prod target/brewledger.backend-0.0.1-SNAPSHOT.jar
```

## API Documentation
*(To be populated via Swagger/OpenAPI or postman collections)*

Currently, an initial setup test can be done by hitting the login endpoint:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "your_admin_username", "password": "your_admin_password"}'
```
