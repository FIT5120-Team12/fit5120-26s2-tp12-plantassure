# Plantky Iteration 1 Backend Implementation Notes

## 1. Two Core Request Flows

### 1.1 Search / Autocomplete

```text
GET /api/v1/plants/search?q=wattle
        ↓
PlantController.searchPlants
        ↓
PlantSearchServiceImpl.search
        ↓
SpeciesDataMapper.selectList
        ↓
MySQL species_data
        ↓
PlantSearchResponse
```

The backend searches both `scientific_name` and `vernacular_name` and returns all matching species. If there are no results, it returns HTTP 200 + `results: []`.

### 1.2 Complete Plant Assessment

```text
GET /api/v1/plants/{plantId}/assessment
        ↓
PlantController.getAssessment
        ↓
AssessmentOrchestratorImpl.assess
        ↓
SpeciesDataMapper.selectById      ← the database is queried only once
        ↓
├─ PlantIdentityService
├─ OccurrenceService
├─ RiskAssessmentService
└─ RecommendationService
        ↓
PlantAssessmentResponse
```

`AssessmentOrchestratorImpl` is responsible for orchestrating the process, rather than placing detailed business rules inside the Controller.

## 2. Responsibilities of Each Module

| Module | Responsibility |
|---|---|
| controller | HTTP paths, request parameters, and response handling |
| service | Use-case interfaces |
| service/impl | Search flow and assessment workflow orchestration |
| service/component | Individual business rules |
| mapper | MySQL data access |
| domain/entity | Database entities |
| domain/vo | Data structures returned to the frontend |
| common/enums | Stable status values, risk values, and recommendation levels |
| common/exception | Business exceptions |
| common/handler | Global exception conversion |
| config | CORS and OpenAPI configuration |

## 3. Why Recommendation Is Separated into Its Own Service

Recommendation is the most important business rule in Iteration 1, so it must not be implemented in the Controller or Vue frontend.

Rules:

```text
Very High / High   → RECONSIDER_PLANTING
Moderately High    → USE_CAUTION
Medium             → USE_CAUTION
Lower              → LOWER_CONCERN
No exact assessment → NOT_ASSESSED
```

The VBA `recordCount` does not participate in the recommendation-level calculation; it is used only in the explanatory text.

## 4. Error Handling

All exceptions are handled centrally through `GlobalExceptionHandler`:

```text
InvalidSearchQueryException → 400 INVALID_SEARCH_QUERY
PlantNotFoundException      → 404 PLANT_NOT_FOUND
Parameter type/validation errors → 400 INVALID_REQUEST
Other exceptions            → 500 INTERNAL_SERVER_ERROR
```

The frontend will not receive SQL details, Java exception class names, or stack traces.

## 5. MyBatis-Plus Mapping

MySQL:

```text
scientific_name
risk_rating
vba_record_count
```

Java:

```text
scientificName
riskRating
vbaRecordCount
```

`application.yml` explicitly enables `map-underscore-to-camel-case: true`.

## 6. Technologies Not Currently Included

- Redis: the dataset contains only 777 read-only records, and there is no evidence that caching is needed.
- Cookie / Session / JWT: Iteration 1 has no login functionality.
- Microservices: the current implementation remains a single Spring Boot monolith.
- Write transactions: both endpoints perform a single read-only query and do not require multi-table write consistency.

When later iterations add login, favourites, user profiles, real-time data synchronisation, or similar features, the corresponding infrastructure can be introduced based on actual requirements.
