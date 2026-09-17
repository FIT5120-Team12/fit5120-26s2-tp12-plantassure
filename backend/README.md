PlantAssure Backend — Iteration 1 & Iteration 2

Spring Boot monolith for PlantAssure / Check Before You Plant (City of Monash), FIT5120 Project 2 — Sustainable Urban Ecosystems.

This README separates the backend functionality delivered in Iteration 1 and Iteration 2.

---

1. Iteration 1

1.1 Scope

Iteration 1 implemented the core plant search and assessment flow.

Public read-only APIs:

    GET /api/v1/plants/search?q={keyword}
    GET /api/v1/plants/{plantId}/assessment

Iteration 1 covers:

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

No login, Cookie, Session, JWT, Redis, microservices, or write APIs are included.

---

1.2 Iteration 1 Architecture

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

Responsibilities:

- controller: HTTP contract only
- service/impl: use-case orchestration and query flow
- service/component: individual business rules
- mapper: database access only
- domain/entity: persistence model
- domain/vo: frontend response model
- common: shared enums, exceptions, handlers and constants
- config: CORS and OpenAPI configuration

---

1.3 Iteration 1 Recommendation Rules

Advisory List rating	Verdict            
Very High           	Reconsider Planting
High                	Reconsider Planting
Moderately High     	Use Caution        
Medium              	Use Caution        
Lower               	Lower Concern      
No exact assessment 	Not Assessed

VBA occurrence is contextual evidence only and does not change recommendation severity.

---

2. Iteration 2

2.1 Scope

Iteration 2 keeps the Iteration 1 search and assessment APIs and extends the backend with:

- plant image identification support
- updated plant occurrence evidence
- additional plant traits
- supplementary GRIIS evidence
- plant catalog browsing
- lower-concern plant alternatives
- side-by-side plant comparison

Iteration 2 public APIs:

    POST /api/v1/plants/identify
    
    GET /api/v1/plants/search?q={keyword}
    GET /api/v1/plants/{plantId}/assessment
    
    GET /api/v1/plants
    GET /api/v1/plants/{plantId}/alternatives
    GET /api/v1/plants/compare?plantIds={id1},{id2},{id3}

The Iteration 1 search and assessment endpoints remain available for compatibility.

---

2.2 Plant Identification

The backend supports plant identification through:

    POST /api/v1/plants/identify
    Content-Type: multipart/form-data

The request contains a plant image using the field:

    image

The backend:

1. validates the uploaded image;
2. sends the image for plant identification;
3. receives possible plant candidates;
4. maps candidate scientific names to existing PlantAssure records where possible;
5. returns up to three possible matches to the frontend.

A response candidate contains:

    {
      "plantId": 3,
      "commonName": "Cootamundra Wattle",
      "scientificName": "Acacia baileyana",
      "identificationConfidence": 0.94,
      "plantAssureMatch": true
    }

If a candidate cannot be mapped to a PlantAssure record:

    {
      "plantId": null,
      "commonName": null,
      "scientificName": "Example species",
      "identificationConfidence": 0.82,
      "plantAssureMatch": false
    }

The backend may return fewer than three candidates when fewer suitable matches are available.

Plant identification does not automatically open an assessment.

After the user selects and confirms a candidate with a valid plantId, the existing assessment endpoint is used:

    GET /api/v1/plants/{plantId}/assessment

Assessment data continues to come from PlantAssure records and verified project data sources.

---

2.3 Iteration 2 Data Changes

The Iteration 2 data model extends the Iteration 1 dataset.

Traits

Supported plant traits include:

    growthForm
    woodiness
    lifeHistory
    heightMin
    heightMax

Missing traits remain unavailable and are not inferred.

Local occurrence

Iteration 2 updates the occurrence sources:

    Iteration 1:
    VBA25
    iNaturalist / GBIF
    
    Iteration 2:
    VBA_FLORA100
    ALA

Local occurrence remains supporting evidence only and does not determine environmental concern or recommendation severity.

GRIIS supplementary evidence

GRIIS data is included as supplementary evidence where available.

It does not replace the 2022 Advisory List as the main environmental-concern source.

Legal status

The current Iteration 2 dataset does not provide a verified Victorian legal-classification value for every plant.

Unavailable legal information is therefore represented as unavailable rather than inferred.

---

2.4 Catalog

Endpoint:

    GET /api/v1/plants

Example:

    GET /api/v1/plants?page=0&size=12

Filtered example:

    GET /api/v1/plants?environmentalConcern=HIGH&originStatus=INTRODUCED&page=0&size=12

Supported catalog functions include:

- common/scientific name search
- environmental concern filter
- origin/native-status filter
- growth-form filter
- life-history filter
- woodiness filter
- height-range filter
- server-side pagination
- sorting

Catalog pagination is zero-based.

The normal assessed catalog excludes NOT_ASSESSED.

---

2.5 Lower-Concern Alternatives

Endpoint:

    GET /api/v1/plants/{plantId}/alternatives?limit=3

Alternative selection follows these backend rules:

1. the candidate must be an existing PlantAssure record;
2. the candidate must have a lower documented environmental concern than the current plant;
3. NOT_ASSESSED is not treated as lower concern;
4. UNAVAILABLE is not treated as lower concern;
5. matching uses supported traits only:
    - growth form
    - life history
    - woodiness
    - height
6. missing trait values are not inferred;
7. the backend does not fabricate candidates to fill a fixed number.

Therefore a successful request may validly return:

    {
      "alternatives": []
    }

when no suitable verified alternative is available.

---

2.6 Compare Plants

Endpoint:

    GET /api/v1/plants/compare?plantIds=1,3

or:

    GET /api/v1/plants/compare?plantIds=1,3,5

The endpoint accepts:

- minimum 2 plant IDs
- maximum 3 plant IDs
- positive IDs only
- unique IDs only

Comparison fields include:

- environmental concern
- legal status
- origin status
- growth form
- life history
- woodiness
- height
- local occurrence

Compare is descriptive only.

It does not return:

    winner
    bestPlant
    safestPlant
    riskScore
    overallScore

---

2.7 Iteration 2 Architecture

    PlantController
      ├─ PlantSearchService
      └─ AssessmentOrchestrator
    
    PlantIdentificationController
      └─ PlantIdentificationService
    
    PlantCatalogController
      └─ PlantCatalogService
    
    PlantAlternativesController
      └─ PlantAlternativeService
    
    PlantComparisonController
      └─ PlantComparisonService
    
    AssessmentOrchestrator
      ├─ PlantIdentityService
      ├─ PlantClassificationService
      ├─ OccurrenceService
      ├─ RiskAssessmentService
      ├─ RecommendationService
      ├─ TraitService
      ├─ GriisEvidenceService
      ├─ LegalStatusService
      ├─ SupportingEvidenceService
      └─ SpeciesDataMapper
               ↓
            MySQL

Iteration 2 extends the existing Iteration 1 layered design rather than replacing it.

---

3. Database

3.1 Create Database

    CREATE DATABASE plantky CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    USE plantky;

Then import the Iteration 2 backend SQL supplied with the project.

The backend table uses:

    id

as the numeric primary key exposed through the APIs as:

    plantId

Important rules:

- id is the backend record identifier.
- scientific_name stores the accepted scientific name.
- match_key is indexed but is not required to be unique.
- missing data remains NULL.
- persistence entities are not exposed directly through controllers.

Iteration 2 data includes information from:

- VicFlora
- VBA_FLORA100
- Atlas of Living Australia (ALA)
- 2022 Advisory List
- AusTraits
- GRIIS

---

4. Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8.x

---

5. Local Configuration

Typical local defaults:

    port: 8080
    DB: jdbc:mysql://localhost:3306/plantky
    username: root
    frontend origin: http://localhost:5173

Recommended database environment variables:

    DB_URL=jdbc:mysql://localhost:3306/plantky?useUnicode=true&characterEncoding=utf8&serverTimezone=Australia/Melbourne&useSSL=false&allowPublicKeyRetrieval=true
    DB_USERNAME=root
    DB_PASSWORD=your_password
    FRONTEND_ORIGIN=http://localhost:5173

---

6. Build and Run

Run all tests:

    mvn clean test

Start the backend:

    mvn spring-boot:run

Create the executable JAR:

    mvn clean package

The generated JAR is located under:

    target/

Example:

    java -jar target/plantky-backend-1.0.0.jar

Use the actual generated filename if the project version changes.

---

7. API Examples

Search

    GET http://localhost:8080/api/v1/plants/search?q=wattle

Assessment

    GET http://localhost:8080/api/v1/plants/3/assessment

Catalog

    GET http://localhost:8080/api/v1/plants?page=0&size=12

Alternatives

    GET http://localhost:8080/api/v1/plants/3/alternatives?limit=3

Compare

    GET http://localhost:8080/api/v1/plants/compare?plantIds=3,17

Plant Identification

    POST http://localhost:8080/api/v1/plants/identify
    Content-Type: multipart/form-data

---

8. OpenAPI / Apifox

After the application starts:

    OpenAPI JSON: http://localhost:8080/v3/api-docs
    Swagger UI:   http://localhost:8080/swagger-ui.html

Apifox can import /v3/api-docs directly.

Successful responses are returned as domain responses and are not wrapped in a generic:

    code/msg/data

structure.

---

9. Environmental Concern and Recommendation Rules

Documented concern ranking:

    VERY_HIGH
    > HIGH
    > MODERATELY_HIGH
    > MEDIUM
    > LOWER

Recommendation mapping:

Environmental concern	Recommendation     
Very High            	Reconsider Planting
High                 	Reconsider Planting
Moderately High      	Use Caution        
Medium               	Use Caution        
Lower                	Lower Concern      
Not Assessed         	Not Assessed

Important rules:

- local occurrence does not change environmental concern;
- local occurrence does not change recommendation severity;
- NOT_FOUND occurrence does not prove local absence;
- NOT_ASSESSED does not mean low concern;
- UNAVAILABLE remains different from NOT_ASSESSED;
- missing traits are not inferred;
- legal status remains separate from environmental concern.

---

10. Error Contract

Example 404:

    {
      "code": "PLANT_NOT_FOUND",
      "message": "Plant not found.",
      "path": "/api/v1/plants/999999/assessment"
    }

Invalid comparison example:

    {
      "code": "INVALID_COMPARE_SELECTION",
      "message": "plantIds must contain 2 to 3 unique positive integer IDs",
      "details": []
    }

Exception handling is centralized in GlobalExceptionHandler.

Stack traces, SQL details, credentials and internal implementation details are not returned to the frontend.

---

11. Suggested Review Order

Iteration 1 Core

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

Iteration 2 Extensions

    1. controller/PlantIdentificationController.java
    2. controller/PlantCatalogController.java
    3. controller/PlantAlternativesController.java
    4. controller/PlantComparisonController.java
    
    5. service/PlantIdentificationService.java
    6. service/PlantCatalogService.java
    7. service/PlantAlternativeService.java
    8. service/PlantComparisonService.java
    
    9. service/impl/
    10. service/component/
    11. domain/entity/
    12. domain/query/
    13. domain/vo/
    14. common/
    15. config/
    16. src/test/

Java classes use JavaDoc for class, method and field responsibilities, with inline comments for important business decisions.

The Iteration 2 backend continues the Iteration 1 layered structure while extending it with the new project functionality.
