# Mettech International Backend

A Java 17 and Spring Boot REST API for an e-commerce application. It includes account management, JWT authentication, a product catalog, customer addresses, order endpoints, email workflows, and real-time address notifications over WebSockets.

Built as a personal project, it uses a layered architecture with Spring MVC, Spring Security, Spring Data JPA, and PostgreSQL. Some cart features are unfinished; see [Known limitations](#known-limitations).

## Features

- **Accounts:** registration, email verification, login, and password reset via email.
- **Security:** JWT bearer authentication, BCrypt password hashing, request validation, and address ownership checks.
- **Catalog:** product information, pricing, and inventory data.
- **Orders and carts:** order retrieval and endpoints for adding, updating, and removing cart items (partially implemented).
- **Customer addresses:** retrieve, create, and update addresses.
- **Real-time updates:** address-change notifications via STOMP over SockJS.

## Tech stack

| Area | Technologies |
| --- | --- |
| Language and build | Java 17, Maven |
| Backend | Spring Boot 3.1.1, Spring MVC, Bean Validation |
| Security | Spring Security, Auth0 Java JWT, BCrypt |
| Persistence | PostgreSQL, Spring Data JPA, Hibernate |
| Messaging | Spring Mail, Spring WebSocket, STOMP, SockJS |
| Testing | JUnit 5, Spring Boot Test, MockMvc, H2, GreenMail |

## Architecture

The application separates HTTP handling, business logic, and persistence into controllers, services, and Spring Data repositories. Spring Security protects authenticated routes, while address changes are published through an in-memory WebSocket message broker.

```text
mettech-intl/
├── pom.xml
├── .env.example
├── src/main/java/mettech_intl/
│   ├── Application.java
│   ├── api/controller/       # REST endpoints
│   ├── api/models/           # Request and response models
│   ├── api/security/         # HTTP/JWT and WebSocket security
│   ├── service/              # Business logic and email workflows
│   ├── model/                # JPA entities
│   │   └── dao/              # Spring Data repositories
│   └── exception/
├── src/main/resources/      # Application configuration
├── src/test/                # Automated tests and fixtures
└── tools/webSocket-tools/   # Manual WebSocket test client
```

## Getting started

### Prerequisites

- JDK 17 and an installed Maven version compatible with Spring Boot 3.1.1 (for example, Maven 3.9.x).
- PostgreSQL and a development database/user.
- An SMTP server or local email-capture service for email workflows.

The checked-in Maven wrapper scripts do not include the required `.mvn/wrapper` configuration, so use the installed `mvn` command.

### Configure the application

From the application module (the directory containing `pom.xml`):

```bash
cd mettech-intl
cp .env.example .env
```

On Windows PowerShell, use `Copy-Item .env.example .env` instead. Edit `.env` with your local settings:

| Variables | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | PostgreSQL connection |
| `JWT_ALGORITHM_KEY` | Secure, randomly generated JWT signing secret |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Email server configuration |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | SMTP authentication and TLS options |
| `EMAIL_FROM`, `APP_FRONTEND_URL` | Email sender and frontend links |

Spring Boot imports the module-local `.env` as a Java properties file. Start the application from the directory containing `pom.xml`; do not commit `.env`. The `.env.example` file is intended to contain placeholders only.

The example configuration assumes PostgreSQL on port `5432` and a local SMTP capture server on port `1025`; these services are **not** bundled. By default, the API runs on port `8082`. The configured development CORS origin is `http://localhost:4200` and is not automatically updated by changing `APP_FRONTEND_URL`.

### Run

```bash
mvn spring-boot:run
```

The API is served at `http://localhost:8082`.

To build a JAR, run:

```bash
mvn package
```

Then run the generated application JAR from `target/` using its actual filename. Packaging normally runs the tests first.

## API overview

| Function | Endpoints |
| --- | --- |
| Account registration and login | `POST /auth/register`, `POST /auth/login` |
| Verify email | `POST /auth/verify?token=...` |
| Reset password | `POST /auth/forgot?email=...`, `POST /auth/reset` |
| Current user | `GET /auth/me` |
| Products | `GET /products` |
| User orders | `GET /orders/current` |
| Cart (partial) | `POST /orders/add`, `PATCH /orders/update`, `DELETE /orders/remove/{productId}` |
| Customer addresses | `GET`, `PUT /user/{userId}/address`; `PATCH /user/{userId}/address/{addressId}` |

Authenticated routes require an `Authorization: Bearer <JWT>` header. Use JSON request bodies where applicable. The cart-add endpoint accepts a numeric product ID; quantity updates use `productId` and `quantity`.

For manual WebSocket checks, open `tools/webSocket-tools/index.html`, connect to `http://localhost:8082/websocket`, and subscribe to `/topic/user/{userId}/address` with the appropriate bearer token in STOMP headers.

## Testing

Run the test suite from the application module:

```bash
mvn test
```

The repository includes tests for application startup, authentication, JWT processing, password hashing, HTTP security, and product listing. Test dependencies include H2 and GreenMail; reports are written to `target/surefire-reports/`.

**Test status:** the current suite has not been confirmed to pass. Known route and fixture inconsistencies require attention; see below.

## Known limitations

- **Cart:** newly created carts do not populate an address required by the order entity; the service behind `DELETE /orders/clear` has no implementation.
- **Orders:** `GET /orders/current` returns the user's orders without filtering by current order status.
- **Products:** the controller uses `/products`, but the public security matcher and a product test refer to `/product`; product listing therefore currently requires authentication.
- **Tests:** some authenticated order tests are commented out; an order test targets an older route, and SQL fixtures omit a required order status.

This is a portfolio/learning project and is **not production-ready**.

## Security considerations

Before publishing or deploying, rotate any credentials previously committed to Git and review the repository history: ignoring `.env` does not remove historical secrets. Remove tracked build artifacts (`target/`) and IDE-generated files from version control after review.

The current implementation also needs security fixes before any public deployment: verification-token expiry is not enforced, some ownership comparisons use boxed IDs with `==`, and the WebSocket endpoint permits any origin. These issues should be corrected and tested before deployment.
