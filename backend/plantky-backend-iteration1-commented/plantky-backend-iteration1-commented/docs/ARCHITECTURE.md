# Backend Architecture Notes

## Layering

### Controller
`PlantController` exposes only the two Iteration 1 API endpoints. It does not perform database queries or recommendation logic.

### Application Services
`PlantSearchServiceImpl` owns the search use case.

`AssessmentOrchestratorImpl` owns the assessment use case and performs one database read before delegating to domain components.

### Business Components
- `PlantIdentityService`: persistence model → identity response
- `OccurrenceService`: VBA evidence → FOUND / NOT_FOUND / UNAVAILABLE
- `RiskAssessmentService`: database risk value → ASSESSED / NOT_ASSESSED / UNAVAILABLE
- `RecommendationService`: deterministic verdict and plain-language explanation

### Persistence
`SpeciesDataMapper` is the only persistence gateway for Iteration 1 and extends MyBatis-Plus `BaseMapper`.

## Important Business Boundaries

1. Environmental risk is copied from the 2022 Advisory List mapping; the backend does not invent a new risk score.
2. `risk_rating = NULL` means **Not Assessed**, never Low Risk or Safe.
3. VBA count is occurrence evidence only and must not change recommendation severity.
4. `vba_record_count = 0` means no matching VBA records; it does not prove the plant is absent from Monash.
5. The frontend displays the backend recommendation and must not recalculate it.

## Why Redis Is Not Used

The current dataset has roughly 777 read-only rows. MySQL lookup latency is already sufficient for the Iteration 1 target. Redis would add deployment and invalidation complexity without a demonstrated performance need.

## Why Transactions Are Not Added

Each Iteration 1 endpoint performs one read query and no multi-step write operation. A transaction is therefore not required for correctness.

---

# Iteration 2 Epic 1 Extension — AI-Assisted Plant Identification

Epic 1 extends the existing architecture without changing the verified Assessment pipeline.

```text
Vue Frontend
    │
    │ multipart/form-data (image)
    ▼
PlantIdentificationController
    │
    ▼
PlantIdentificationServiceImpl
    ├──────────────► ImageValidationService
    │                 MIME + size + file signature
    │
    ├──────────────► PlantIdentificationClient
    │                    │
    │                    ▼
    │              External AI Provider
    │                    │
    │          scientificName + confidence
    │                    │
    └──────────────► SpeciesMatchingService
                         │
                         ▼
                    species_data
                         │
                         ▼
                   Top 3 matches
                         │
                         ▼
                    Vue Frontend
                         │
                    User confirms
                         │
                         ▼
            GET /plants/{plantId}/assessment
                         │
                         ▼
              Existing AssessmentOrchestrator
```

## Boundary rule

```text
AI identification confidence
        !=
environmental concern / risk / recommendation
```

`PlantIdentificationServiceImpl` deliberately does not depend on `AssessmentOrchestrator`.
The existing Assessment is entered only after the user confirms a candidate with a real PlantAssure `plantId`.

## External-dependency isolation

All provider-specific HTTP/schema handling lives behind:

```text
PlantIdentificationClient
```

This permits the AI team to replace its Python model/API without rewriting PlantAssure controllers or domain response objects.
