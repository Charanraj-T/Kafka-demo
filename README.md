# Product Ingestion & Catalog API Service

A Spring Boot microservice for event-driven product catalog ingestion and real-time retrieval. It consumes product events from Apache Kafka, enforces schema validation, performs idempotent upserts into MongoDB, and exposes REST endpoints.

Unresolvable or invalid payloads are routed directly to a Dead Letter Queue (`products.dlq`), while transient errors trigger 3 in-place retries with a 1-second backoff.

---

## Architecture

```
[ Kafka: products.in ]
          │
          ▼
   ProductConsumer (@Valid @Payload ProductMessageDto)
          │
          ├── [Valid] ──────────────────────────► ProductService ──► MongoDB [products]
          │                                                              (sku index)
          └── [Error] ──────────────────────────► DefaultErrorHandler
                                                        │
                      ┌─────────────────────────────────┴─────────────────────────────────┐
                      ▼ [Validation Error]                                                ▼ [Transient Error]
                 Immediate DLQ                                                       3x Retries (1s backoff)
                      │                                                                   │ (exhausted)
                      └────────────────────────► [ Kafka: products.dlq ] ◄────────────────┘

[ REST Client ] ──► ProductController
                         ├── GET /products/{sku}  (200 OK / 404 ProblemDetail)
                         └── GET /products        (200 OK)
```

---

## Tech Stack

- **Java**: 21
- **Spring Boot**: 4.1.1
- **Messaging**: Apache Kafka
- **Database**: MongoDB 7.0
- **Testing**: JUnit 5, Mockito, JaCoCo

---

## REST Endpoints

| Method | Endpoint | Description | Success Response |
| :--- | :--- | :--- | :--- |
| `GET` | `/products/{sku}` | Fetch product by SKU | `200 OK` (or `404 ProblemDetail`) |
| `GET` | `/products` | List all persisted products | `200 OK` (JSON Array) |
| `GET` | `/actuator/health` | Service & MongoDB health probe | `200 OK` (`"status": "UP"`) |
| `POST` | `/mock/publish` | Publish custom product message | `200 OK` (`"status": "PUBLISHED"`) |
| `POST` | `/mock/scenario` | Run batch scenario from JSON file | `200 OK` (`"status": "SUCCESS"`) |

### Sample Response: `GET /products/{sku}`
```json
{
  "id": "6acc7566b6693a90f75dfd02",
  "sku": "101980-RED",
  "productId": "101980",
  "name": "Classic Budding Rose Deluxe",
  "brand": "1800FLOWERS",
  "category": "Flowers",
  "price": 69.99,
  "inventory": 25,
  "status": "ACTIVE",
  "createdAt": "2026-10-12T05:51:34.202Z",
  "updatedAt": "2026-10-12T05:51:34.209Z"
}
```

---

## Kafka Contracts

### Inbound: `products.in`
- **Key**: `sku` (`String`)
- **Payload (`ProductMessageDto`)**:
  - `productId`: `@NotBlank String`
  - `sku`: `@NotBlank String`
  - `name`: `@NotBlank String`
  - `brand`: Optional `String`
  - `category`: Optional `String`
  - `price`: `@NotNull @DecimalMin("0.0") Double`
  - `inventory`: `@NotNull @Min(0) Integer`
  - `status`: `@NotNull ProductStatus` (`ACTIVE`, `INACTIVE`)

### Dead Letter: `products.dlq`
- Receives records that fail validation immediately or exhaust 3 retries, retaining the original payload with error metadata headers (`kafka_dlt-exception-message`, `kafka_dlt-original-topic`, etc.).

---

## How to Run

### 1. Start Infrastructure (Docker)

```bash
# 1. MongoDB
docker run -d --name mongo -p 27017:27017 mongo:7

# 2. Apache Kafka (KRaft mode)
docker run -d --name kafka -p 9092:9092 apache/kafka:latest

# 3. Redpanda Console UI (http://localhost:8082)
docker run -d --name redpanda-console -p 8082:8080 \
  -e KAFKA_BROKERS=host.docker.internal:9092 \
  docker.redpanda.com/redpandadata/console:latest
```

### 2. Start Application
```bash
mvn spring-boot:run
```
The application starts on **port 8080**.

### 3. Run Tests
```bash
mvn test
```

---

## Verification

```bash
# 1. Check health
curl http://localhost:8080/actuator/health

# 2. Trigger mock scenario batch (1 create, 1 update, 1 invalid -> DLQ)
curl -X POST http://localhost:8080/mock/scenario

# 3. Retrieve valid upserted product
curl http://localhost:8080/products/101980-RED

# 4. Confirm invalid product was rejected and routed to DLQ (returns 404)
curl http://localhost:8080/products/101981-INVALID
```
