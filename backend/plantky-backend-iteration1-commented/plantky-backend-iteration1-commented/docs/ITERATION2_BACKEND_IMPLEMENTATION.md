# Iteration 2 Backend Implementation Notes

## 1. Source-of-truth order used for this backend revision

The supplied materials contained one important conflict: the older CSV AC says that at least three alternatives are shown, while the latest Epic says a clear no-results state must be supported and unsupported suggestions must not be generated.

This implementation therefore uses the following resolution already agreed during backend planning:

1. latest Epic semantics;
2. current frontend/backend API contract;
3. Iteration 2 CSV/LeanKit wording where it does not conflict;
4. Iteration 1 compatibility requirements.

## 2. Continuous-development strategy

Iteration 1 endpoints remain available:

```http
GET /api/v1/plants/search
GET /api/v1/plants/{plantId}/assessment
```

New endpoints are separated by responsibility:

```text
PlantCatalogController
PlantAlternativesController
PlantComparisonController
```

Services are similarly separated:

```text
PlantCatalogService
PlantAlternativeService
PlantComparisonService
```

This avoids turning the original PlantController or AssessmentOrchestrator into a large multi-feature class.

## 3. Assessment compatibility

The I1 response blocks are retained:

```text
plant
localOccurrence
environmentalRisk
recommendation
sources
warnings
```

I2 adds:

```text
originStatus
victorianEstablishment
environmentalConcern
legalStatus
legalStatusDetail
traits
griisSupplementaryEvidence
supportingEvidence
```

The current I1 frontend can ignore unknown new JSON fields, while the I2 frontend can gradually integrate the new blocks.

### Known contract mismatch deliberately not forced

The newer frontend contract describes `recommendation` as a scalar enum, while the real I1 frontend currently expects a structured object:

```json
{
  "level": "USE_CAUTION",
  "displayLabel": "Use Caution",
  "explanation": "..."
}
```

This implementation keeps the existing structure to avoid breaking the deployed I1 page. The team should change this only as a coordinated frontend/backend versioned migration.

## 4. Stable plantId migration

The raw I2 dataset contains all 777 I1 scientific names plus 103 new scientific names.

The backend seed therefore preserves the exact I1 IDs for all existing records and allocates:

```text
778–880
```

to the new records.

This satisfies the requirement that the same PlantAssure record use the same `plantId` across Search, Assessment, Catalog, Alternatives and Compare.

## 5. Alternatives algorithm

Candidate eligibility:

```text
current documented concern
        ↓
query only lower documented concern records
        ↓
exclude NOT_ASSESSED / UNAVAILABLE
        ↓
exclude REGULATED when verified legal data becomes available
        ↓
calculate supported trait similarity
        ↓
require >= 1 real match reason
        ↓
order by trait similarity only
```

Internal similarity weights are used only to sort equally eligible candidates:

```text
growth form   +4
life history  +3
woodiness     +2
height overlap +1
```

These weights are not returned to the frontend and are not environmental risk/safety scores.

## 6. Legal-status limitation

The current dataset does not provide legal classification.

Therefore:

```text
LegalStatusService -> UNAVAILABLE
```

The service boundary already exists so a verified legal dataset can be connected later without rewriting Catalog/Alternatives/Compare controllers.

A strict rule such as “regulated candidates must never be returned” cannot be fully verified until that dataset exists. The current code will exclude a candidate automatically if LegalStatusService later returns REGULATED.

## 7. Epic 1 — AI-assisted identification

Epic 1 is now implemented using the separate Iteration 2 AI development document and Epic acceptance criteria.

Backend endpoint:

```http
POST /api/v1/plants/identify
```

The implementation uses:

```text
PlantIdentificationController
PlantIdentificationServiceImpl
ImageValidationService
PlantIdentificationClient
HttpPlantIdentificationClient
SpeciesMatchingService
```

The backend returns up to Top 3 possible species matches by default. It never auto-confirms the highest-confidence result and never invokes Assessment automatically.

The real AI provider URL/final JSON schema was not supplied, so provider-specific mapping remains isolated inside `HttpPlantIdentificationClient`. See `EPIC1_AI_IDENTIFICATION.md`.
