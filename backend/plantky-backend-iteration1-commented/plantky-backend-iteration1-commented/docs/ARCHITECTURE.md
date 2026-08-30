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
