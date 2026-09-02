# Plantky Backend — Iteration 1

Spring Boot monolith for **Plantky / Check Before You Plant** (City of Monash), FIT5120 Project 2 — Sustainable Urban Ecosystems.

## 1. Iteration 1 Scope

Two public read-only APIs are implemented:

```http
GET /api/v1/plants/search?q={keyword}
GET /api/v1/plants/{plantId}/assessment
```

They cover:

- common/scientific name search
- autocomplete backend query
- plant identity
- establishment means / degree of establishment
- City of Monash VBA occurrence evidence
- 2022 Advisory List environmental weed risk
- backend-owned planting recommendation
- plain-language explanation
- source attribution
- missing/partial-data warnings

No login, Cookie, Session, JWT, Redis, microservices, or write APIs are included in Iteration 1.

## 2. Architecture

```text
PlantController
  ├─ PlantSearchService
  │    └─ SpeciesDataMapper
  │
  └─ AssessmentOrchestrator
       ├─ PlantIdentityService
       ├─ OccurrenceService
       ├─ RiskAssessmentService
       ├─ RecommendationService
       └─ SpeciesDataMapper
                ↓
             MySQL
```

Responsibilities are separated deliberately:

- **controller**: HTTP contract only
- **service/impl**: use-case orchestration and query flow
- **service/component**: individual business rules
- **mapper**: database access only
- **domain/entity**: persistence model
- **domain/vo**: frontend response model
- **common**: shared enums, exceptions, handlers and constants
- **config**: CORS and OpenAPI configuration

## 3. Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8.x

## 4. Database

Create a database first:

```sql
CREATE DATABASE plantky CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plantky;
```

Then execute:

```text
src/main/resources/db/species_data_revised.sql
```

The supplied dataset contains 777 species and uses `id` as the API `plantId`.

## 5. Local Configuration

Defaults:

```text
port: 8080
DB: jdbc:mysql://localhost:3306/plantky
username: root
password: empty
frontend origin: http://localhost:5173
```

Recommended environment variables:

```bash
DB_URL=jdbc:mysql://localhost:3306/plantky?useUnicode=true&characterEncoding=utf8&serverTimezone=Australia/Melbourne&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password
FRONTEND_ORIGIN=http://localhost:5173
```

## 6. Run

```bash
mvn clean test
mvn spring-boot:run
```

## 7. API Examples

### Search

```http
GET http://localhost:8080/api/v1/plants/search?q=wattle
```

### Assessment

`Acacia baileyana` is `plantId=3` in the supplied SQL.

```http
GET http://localhost:8080/api/v1/plants/3/assessment
```

Expected recommendation:

```json
{
  "level": "USE_CAUTION",
  "displayLabel": "Use Caution"
}
```

## 8. OpenAPI / Apifox

After the application starts:

```text
OpenAPI JSON: http://localhost:8080/v3/api-docs
Swagger UI:   http://localhost:8080/swagger-ui.html
```

Apifox can import `/v3/api-docs` directly. The generated contract follows the Iteration 1 API document and does not wrap successful responses with `code/msg/data`.

## 9. Recommendation Rules

| Advisory List rating | Verdict |
|---|---|
| Very High | Reconsider Planting |
| High | Reconsider Planting |
| Moderately High | Use Caution |
| Medium | Use Caution |
| Lower | Lower Concern |
| No exact assessment | Not Assessed |

VBA record count is contextual evidence only. It never changes recommendation severity.

## 10. Error Contract

Example 404:

```json
{
  "code": "PLANT_NOT_FOUND",
  "message": "Plant not found.",
  "path": "/api/v1/plants/999999/assessment"
}
```

Exception handling is centralized in `GlobalExceptionHandler`; stack traces and SQL details are never returned to the frontend.

## 11. Detailed-comment edition

This archive is the **detailed-comment edition** of the same Iteration 1 backend.
Business behaviour and API contracts are intentionally unchanged.

For study/review, start in this order:

```text
1. controller/PlantController.java
2. service/PlantSearchService.java
3. service/impl/PlantSearchServiceImpl.java
4. service/AssessmentOrchestrator.java
5. service/impl/AssessmentOrchestratorImpl.java
6. service/component/PlantIdentityService.java
7. service/component/OccurrenceService.java
8. service/component/RiskAssessmentService.java
9. service/component/RecommendationService.java
10. mapper/SpeciesDataMapper.java
11. domain/entity + domain/vo
12. common + config
13. src/test
```

Every main package now also contains a `package-info.java` explaining that package's responsibility.
Java classes use JavaDoc for class/method/field responsibilities and inline comments for important business decisions.
Configuration files and `pom.xml` are also annotated.
