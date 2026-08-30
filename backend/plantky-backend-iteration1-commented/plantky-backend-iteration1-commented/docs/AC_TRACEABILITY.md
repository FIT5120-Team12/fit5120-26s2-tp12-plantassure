# Iteration 1 Acceptance Criteria → Backend Code

| Requirement | Backend implementation |
|---|---|
| Search by common/scientific name | `PlantSearchServiceImpl.search()` |
| Valid search returns results | `SpeciesDataMapper.selectList()` + `PlantSearchResponse` |
| Multiple species preserved | each database row maps to one search result |
| No match returns `results: []` | empty mapper result is returned with HTTP 200 |
| Autocomplete backend | same `/search` endpoint; Vue enforces 3-character trigger/debounce |
| Plant identity | `PlantIdentityService` |
| Establishment | `PlantIdentityService` |
| Local occurrence | `OccurrenceService` |
| Environmental weed risk | `RiskAssessmentService` |
| Exactly one verdict | `RecommendationService.determineLevel()` |
| VBA does not alter verdict | recommendation depends only on risk status/rating |
| Plain-language evidence | `RecommendationService.buildExplanation()` |
| Source attribution | `AssessmentOrchestratorImpl.buildSources()` |
| Invalid blank search | `InvalidSearchQueryException` → HTTP 400 |
| Invalid plant ID / missing plant | validation / `PlantNotFoundException` |
| Unexpected error does not leak internals | `GlobalExceptionHandler` |
