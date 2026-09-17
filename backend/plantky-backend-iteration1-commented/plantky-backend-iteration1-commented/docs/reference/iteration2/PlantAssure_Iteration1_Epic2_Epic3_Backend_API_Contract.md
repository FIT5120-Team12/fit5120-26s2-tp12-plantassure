# PlantAssure Backend API Contract
## Iteration 1 + Epic 2 + Epic 3

**Base path:** `/api/v1`  
**Frontend:** Vue 3 + TypeScript + Axios  
**Backend:** Spring Boot 3.3.5 + Java 17 + Spring MVC + MyBatis-Plus + MySQL 8  
**Scope:** Iteration 1, Epic 2, Epic 3  
**Out of scope:** Epic 1 AI Identification

---

# 1. Purpose

This document defines the backend API contract required by the current PlantAssure frontend.

The goal is that:

1. Backend developers can implement the required APIs directly from this document.
2. Frontend developers can integrate against stable DTOs without redesigning the current UI/data model.
3. Business semantics remain consistent across Assessment, Catalog, Alternatives, and Compare.
4. Missing or unavailable evidence is represented explicitly and must not be silently converted into misleading risk or recommendation values.

The required APIs are:

| Scope | Endpoint | Purpose |
|---|---|---|
| Iteration 1 | `GET /api/v1/plants/search` | Home plant autocomplete/search |
| Iteration 1 | `GET /api/v1/plants/{plantId}/assessment` | Plant assessment |
| Epic 3 | `GET /api/v1/plants` | Browse/search/filter assessed plants |
| Epic 2 | `GET /api/v1/plants/{plantId}/alternatives` | Lower-concern alternatives |
| Epic 2 | `GET /api/v1/plants/compare` | Side-by-side comparison |

---

# 2. Domain Rules

These rules are part of the API contract, not optional UI behaviour.

## 2.1 Data-source responsibilities

| Source | Responsibility |
|---|---|
| VicFlora | Plant identity; Victorian establishment/origin |
| 2022 Advisory List | Environmental weed concern |
| VBA | Local occurrence evidence only |
| AusTraits | Growth form, life history, woodiness, height |

## 2.2 Mandatory semantic rules

- VBA **must not** determine environmental concern.
- `localOccurrence.status = NOT_FOUND` means no matching VBA records were found. It does **not** prove the plant is absent locally.
- `NOT_ASSESSED` does **not** mean safe, low risk, or lower concern.
- `UNAVAILABLE` is different from `NOT_ASSESSED`.
- Planting `recommendation` is backend-owned.
- Frontend must not infer recommendation from unrelated fields.
- Partial evidence failure, especially VBA/local occurrence failure, must not automatically fail the whole Assessment response.
- Legal/regulatory status must remain separate from environmental concern.
- Missing traits must not be inferred.
- AI identification confidence is outside this contract and must never be treated as environmental risk evidence.
- Alternatives must be verified PlantAssure records.
- Regulated plants must not be returned as alternatives.
- Compare must not calculate a winner, safest plant, aggregate score, or risk score.

---

# 3. General API Conventions

## 3.1 Content type

Successful JSON APIs return:

```http
Content-Type: application/json
```

## 3.2 Identifiers

`plantId`:

- integer
- positive
- stable across APIs
- represents the same PlantAssure plant record everywhere

Example:

```json
101
```

## 3.3 Enum serialization

Enums use uppercase snake case.

Examples:

```text
VERY_HIGH
NOT_ASSESSED
RECONSIDER_PLANTING
NOT_REGULATED
```

## 3.4 Missing values

Missing optional scalar values must be:

```json
null
```

Do not return presentation strings such as:

```text
"Unknown"
"N/A"
"-"
```

Frontend is responsible for rendering missing values as `Unavailable` where appropriate.

## 3.5 Empty collections

Use:

```json
[]
```

Never return `null` for an empty collection.

## 3.6 Image field

Use:

```text
imageUrl
```

`imageUrl` may be `null`.

Frontend already supports an image fallback and load-failure fallback.

## 3.7 Numeric pagination

Catalog pagination is **zero-based**.

Example:

```text
page=0
```

means the first page.

---

# 4. Shared Enums

## 4.1 EnvironmentalConcern

```text
VERY_HIGH
HIGH
MODERATELY_HIGH
MEDIUM
LOWER
NOT_ASSESSED
UNAVAILABLE
```

Interpretation:

- `VERY_HIGH`, `HIGH`, `MODERATELY_HIGH`, `MEDIUM`, `LOWER` are documented concern levels.
- `NOT_ASSESSED` means no applicable environmental assessment exists.
- `UNAVAILABLE` means required evidence/data could not currently be obtained.

Concern ranking used only where a lower documented concern comparison is required:

```text
VERY_HIGH
> HIGH
> MODERATELY_HIGH
> MEDIUM
> LOWER
```

`NOT_ASSESSED` and `UNAVAILABLE` do not participate in this ranking.

---

## 4.2 Recommendation

```text
RECONSIDER_PLANTING
USE_CAUTION
LOWER_CONCERN
NOT_ASSESSED
```

Required mapping:

| Environmental concern | Recommendation |
|---|---|
| `VERY_HIGH` | `RECONSIDER_PLANTING` |
| `HIGH` | `RECONSIDER_PLANTING` |
| `MODERATELY_HIGH` | `USE_CAUTION` |
| `MEDIUM` | `USE_CAUTION` |
| `LOWER` | `LOWER_CONCERN` |
| `NOT_ASSESSED` | `NOT_ASSESSED` |

`VBA/localOccurrence` must not affect this mapping.

For `environmentalConcern = UNAVAILABLE`, the product rules supplied so far do not define a recommendation enum value. Backend must not invent one. If this state can occur for the overall assessment, the team should explicitly extend the recommendation contract before implementation.

---

## 4.3 OriginStatus

Current frontend contract:

```text
NATIVE
INTRODUCED
UNCERTAIN
```

Important:

`UNCERTAIN` must only be returned if the real backend/source mapping genuinely supports that state. Backend must not fabricate it merely to satisfy the frontend.

If the final data model cannot produce `UNCERTAIN`, remove it from the backend enum and align the frontend filter options during integration.

---

## 4.4 LocalOccurrenceStatus

```text
FOUND
NOT_FOUND
UNAVAILABLE
```

Semantics:

| Value | Meaning |
|---|---|
| `FOUND` | Matching local occurrence records were found |
| `NOT_FOUND` | No matching records were found |
| `UNAVAILABLE` | Local occurrence evidence could not currently be obtained |

`NOT_FOUND` must not be interpreted as proof of absence.

---

## 4.5 LegalStatus

```text
NOT_REGULATED
REGULATED
UNAVAILABLE
```

Legal status is independent from environmental concern.

---

# 5. Shared Data Models

## 5.1 PlantIdentityDto

```json
{
  "plantId": 101,
  "commonName": "Blue Periwinkle",
  "scientificName": "Vinca major",
  "family": "Apocynaceae",
  "imageUrl": "https://example.org/images/101.jpg"
}
```

Recommended Java shape:

```java
public record PlantIdentityDto(
    Long plantId,
    String commonName,
    String scientificName,
    String family,
    String imageUrl
) {}
```

`family` and `imageUrl` may be `null` if unavailable.

---

## 5.2 LocalOccurrenceDto

```json
{
  "status": "FOUND",
  "recordCount": 8,
  "latestRecordYear": 2025
}
```

Rules:

### FOUND

```json
{
  "status": "FOUND",
  "recordCount": 8,
  "latestRecordYear": 2025
}
```

### NOT_FOUND

```json
{
  "status": "NOT_FOUND",
  "recordCount": 0,
  "latestRecordYear": null
}
```

### UNAVAILABLE

```json
{
  "status": "UNAVAILABLE",
  "recordCount": null,
  "latestRecordYear": null
}
```

Do not substitute `NOT_FOUND` when the VBA source failed.

---

## 5.3 Trait fields

Supported traits:

```text
growthForm
lifeHistory
woodiness
height
```

Missing trait values must be `null`.

Backend must not derive or guess missing traits.

Current API examples represent traits as display-ready strings because this matches the existing frontend structure.

Example:

```json
{
  "growthForm": "Groundcover",
  "lifeHistory": "Perennial",
  "woodiness": "Herbaceous",
  "height": "0.3–1.0 m"
}
```

If the team later introduces normalized trait enums/ranges, that should be handled as a versioned contract change rather than silently changing these fields.

---

# 6. Iteration 1 API — Plant Search

## 6.1 Endpoint

```http
GET /api/v1/plants/search
```

Purpose:

Home search/autocomplete for locating a plant before opening Assessment.

---

## 6.2 Query parameters

| Parameter | Type | Required | Rules |
|---|---|---:|---|
| `q` | string | yes | trimmed, non-empty |
| `limit` | integer | no | positive; recommended default `8` |

Example:

```http
GET /api/v1/plants/search?q=vinca&limit=8
```

---

## 6.3 Search behaviour

Search should match:

- common name
- scientific name

Matching must be case-insensitive.

The backend may define sensible ranking, but should prefer stable, deterministic ordering.

No results is a normal successful response.

---

## 6.4 Success response

### `200 OK`

```json
{
  "query": "vinca",
  "items": [
    {
      "plantId": 101,
      "commonName": "Blue Periwinkle",
      "scientificName": "Vinca major",
      "family": "Apocynaceae",
      "imageUrl": "https://example.org/images/101.jpg"
    }
  ]
}
```

No results:

```json
{
  "query": "vinca",
  "items": []
}
```

---

## 6.5 Validation

Examples that should produce `400 Bad Request`:

```text
q=
q=   
limit=0
limit=-1
limit=abc
```

---

# 7. Iteration 1 API — Plant Assessment

## 7.1 Endpoint

```http
GET /api/v1/plants/{plantId}/assessment
```

Purpose:

Returns the complete assessment data required by the current Assessment page.

---

## 7.2 Path parameter

| Parameter | Type | Rules |
|---|---|---|
| `plantId` | integer | positive |

Example:

```http
GET /api/v1/plants/101/assessment
```

---

## 7.3 Success response

### `200 OK`

```json
{
  "plant": {
    "plantId": 101,
    "commonName": "Blue Periwinkle",
    "scientificName": "Vinca major",
    "family": "Apocynaceae",
    "imageUrl": "https://example.org/images/101.jpg"
  },
  "originStatus": "INTRODUCED",
  "victorianEstablishment": {
    "status": "NATURALISED",
    "label": "Naturalised in Victoria"
  },
  "localOccurrence": {
    "status": "FOUND",
    "recordCount": 8,
    "latestRecordYear": 2025
  },
  "environmentalConcern": {
    "status": "HIGH",
    "source": "2022_ADVISORY_LIST"
  },
  "legalStatus": "NOT_REGULATED",
  "recommendation": "RECONSIDER_PLANTING"
}
```

---

## 7.4 Assessment response model

```text
AssessmentResponse
├── plant: PlantIdentityDto
├── originStatus: OriginStatus
├── victorianEstablishment
│   ├── status
│   └── label
├── localOccurrence: LocalOccurrenceDto
├── environmentalConcern
│   ├── status: EnvironmentalConcern
│   └── source
├── legalStatus: LegalStatus
└── recommendation: Recommendation
```

### Victorian establishment status

The supplied project context includes `NATURALISED` as an example but does not define the complete allowed enum.

Therefore:

- backend must use the actual VicFlora-backed establishment mapping;
- values must be stable uppercase enum values;
- frontend-facing `label` should remain available as a display label;
- the final allowed status set must be confirmed from the real source mapping before the backend contract is frozen.

Backend must not invent unsupported establishment categories.

---

## 7.5 Recommendation ownership

The backend must calculate and return:

```text
recommendation
```

Frontend must not reconstruct it from `environmentalConcern`.

Required logic:

```text
VERY_HIGH / HIGH
→ RECONSIDER_PLANTING

MODERATELY_HIGH / MEDIUM
→ USE_CAUTION

LOWER
→ LOWER_CONCERN

NOT_ASSESSED
→ NOT_ASSESSED
```

VBA/local occurrence does not participate in this decision.

---

## 7.6 Partial evidence failure

If VBA/local occurrence data cannot be loaded, Assessment should still return `200 OK` where the rest of the assessment is available.

Example:

```json
{
  "plant": {
    "plantId": 101,
    "commonName": "Blue Periwinkle",
    "scientificName": "Vinca major",
    "family": "Apocynaceae",
    "imageUrl": null
  },
  "originStatus": "INTRODUCED",
  "victorianEstablishment": {
    "status": "NATURALISED",
    "label": "Naturalised in Victoria"
  },
  "localOccurrence": {
    "status": "UNAVAILABLE",
    "recordCount": null,
    "latestRecordYear": null
  },
  "environmentalConcern": {
    "status": "HIGH",
    "source": "2022_ADVISORY_LIST"
  },
  "legalStatus": "NOT_REGULATED",
  "recommendation": "RECONSIDER_PLANTING"
}
```

Do not fail the entire endpoint only because VBA is unavailable.

---

## 7.7 Plant not found

If `plantId` does not identify a PlantAssure plant record:

```http
404 Not Found
```

---

# 8. Epic 3 API — Browse Assessed Plants

## 8.1 Endpoint

```http
GET /api/v1/plants
```

Purpose:

Server-side browse/search/filter/pagination for the `/plants` Catalog page.

This endpoint is broad exploration, not a recommendation API.

---

## 8.2 Query parameters

### MVP required

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `q` | string | no | common/scientific name search |
| `environmentalConcern` | enum / repeated parameter | no | concern filter |
| `originStatus` | enum / repeated parameter | no | origin filter |
| `page` | integer | no | zero-based page |
| `size` | integer | no | page size |
| `sort` | string | no | server-supported sort |

### Future AusTraits filters

| Parameter | Type | Status |
|---|---|---|
| `growthForm` | string/enum | future |
| `lifeHistory` | string/enum | future |
| `minHeight` | numeric | future |
| `maxHeight` | numeric | future |

Do not add unsupported filters such as sun/shade, soil, or drought tolerance.

---

## 8.3 Example request

```http
GET /api/v1/plants?q=vinca&environmentalConcern=HIGH&originStatus=INTRODUCED&page=0&size=12&sort=commonName,asc
```

For multi-value filters, recommended request format is repeated query parameters:

```http
GET /api/v1/plants?environmentalConcern=HIGH&environmentalConcern=MEDIUM&page=0&size=12
```

Backend should document and keep one consistent format.

---

## 8.4 Filter combination

Different filter groups combine using:

```text
AND
```

Example:

```text
q matches
AND environmentalConcern matches
AND originStatus matches
```

For multiple values inside the same filter group, recommended semantics are:

```text
OR
```

Example:

```text
environmentalConcern IN (HIGH, MEDIUM)
```

---

## 8.5 Assessed-only rule

Catalog must not return:

```text
NOT_ASSESSED
```

The frontend concern filter also does not expose `NOT_ASSESSED`.

Because Catalog is defined as “Browse Assessed Plants”, the recommended backend interpretation is to return only records with a documented concern level:

```text
VERY_HIGH
HIGH
MODERATELY_HIGH
MEDIUM
LOWER
```

Accordingly, `UNAVAILABLE` should also be excluded from the normal assessed catalog unless the product team explicitly decides otherwise.

This `UNAVAILABLE` exclusion is a contract recommendation derived from the assessed-only requirement and should be confirmed when backend implementation begins.

---

## 8.6 Missing traits

When future trait filters are enabled:

```text
missing trait
→ does not match that trait filter
```

Backend must not infer a missing trait.

---

## 8.7 Success response

### `200 OK`

```json
{
  "items": [
    {
      "plantId": 101,
      "commonName": "Blue Periwinkle",
      "scientificName": "Vinca major",
      "imageUrl": "https://example.org/images/101.jpg",
      "environmentalConcern": "HIGH",
      "originStatus": "INTRODUCED",
      "growthForm": "Groundcover",
      "lifeHistory": "Perennial",
      "height": "0.3–1.0 m"
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 283,
  "totalPages": 24,
  "sort": "commonName,asc"
}
```

No results:

```json
{
  "items": [],
  "page": 0,
  "size": 12,
  "totalElements": 0,
  "totalPages": 0,
  "sort": "commonName,asc"
}
```

---

## 8.8 Catalog item model

```text
plantId
commonName
scientificName
imageUrl
environmentalConcern
originStatus
growthForm?
lifeHistory?
height?
```

`growthForm`, `lifeHistory`, and `height` may be `null` until AusTraits data is integrated.

---

# 9. Epic 2 API — Better Plant Alternatives

## 9.1 Endpoint

```http
GET /api/v1/plants/{plantId}/alternatives
```

Purpose:

Return lower documented environmental concern alternatives for the current plant.

This endpoint powers:

```text
/plants/:plantId/alternatives
```

It must not behave like a second Catalog endpoint.

---

## 9.2 Query parameters

| Parameter | Type | Required | Rules |
|---|---|---:|---|
| `limit` | integer | no | positive |

Example:

```http
GET /api/v1/plants/101/alternatives?limit=6
```

---

## 9.3 Candidate eligibility

Every returned alternative must satisfy all applicable rules:

1. verified PlantAssure record;
2. documented environmental concern is lower than the current plant;
3. `NOT_ASSESSED` excluded;
4. `UNAVAILABLE` excluded from lower-concern ranking;
5. regulated plants excluded;
6. similarity uses only supported traits;
7. missing traits are not inferred;
8. `matchReasons[]` explains actual supported similarity;
9. VBA/local occurrence does not determine environmental concern;
10. no synthetic risk/safety score.

Supported similarity traits:

```text
growthForm
lifeHistory
woodiness
height
```

---

## 9.4 Lower-concern ordering

Concern order:

```text
VERY_HIGH
> HIGH
> MODERATELY_HIGH
> MEDIUM
> LOWER
```

Examples:

- current `HIGH` → candidates may be `MODERATELY_HIGH`, `MEDIUM`, or `LOWER`
- current `MEDIUM` → candidate may be `LOWER`
- current `LOWER` → no lower documented concern level exists

`NOT_ASSESSED` and `UNAVAILABLE` must never be treated as lower concern.

---

## 9.5 Similarity and concern must remain separate

The backend may rank eligible lower-concern candidates by supported trait similarity.

However, it must not combine environmental concern and trait similarity into an opaque score.

Do not return fields such as:

```text
riskScore
safetyScore
similarityRiskScore
overallScore
```

If an internal similarity calculation is used for ordering, it does not need to be exposed.

The frontend requires explicit `matchReasons[]`.

---

## 9.6 Success response

### `200 OK`

```json
{
  "currentPlant": {
    "plantId": 101,
    "commonName": "Blue Periwinkle",
    "scientificName": "Vinca major",
    "imageUrl": "https://example.org/images/101.jpg",
    "environmentalConcern": "HIGH",
    "growthForm": "Groundcover",
    "lifeHistory": "Perennial",
    "woodiness": "Herbaceous",
    "height": "0.3–1.0 m"
  },
  "alternatives": [
    {
      "plantId": 205,
      "commonName": "Native Violet",
      "scientificName": "Viola hederacea",
      "imageUrl": "https://example.org/images/205.jpg",
      "environmentalConcern": "LOWER",
      "originStatus": "NATIVE",
      "legalStatus": "NOT_REGULATED",
      "growthForm": "Groundcover",
      "lifeHistory": "Perennial",
      "woodiness": "Herbaceous",
      "height": "0.1–0.2 m",
      "matchReasons": [
        "Similar growth form",
        "Same life-history category",
        "Similar mature height"
      ]
    }
  ]
}
```

---

## 9.7 `matchReasons[]`

`matchReasons`:

- required for each alternative;
- must be an array;
- must contain only reasons supported by actual data;
- must not mention a trait that is missing;
- should use concise user-facing English text.

Examples:

```json
[
  "Similar growth form",
  "Same life-history category"
]
```

If only one supported similarity exists:

```json
[
  "Similar growth form"
]
```

Do not fabricate additional reasons to make the list longer.

---

## 9.8 No alternatives

No valid candidates is a successful result.

Return:

```http
200 OK
```

with:

```json
{
  "currentPlant": {
    "plantId": 101,
    "commonName": "Blue Periwinkle",
    "scientificName": "Vinca major",
    "imageUrl": null,
    "environmentalConcern": "LOWER",
    "growthForm": "Groundcover",
    "lifeHistory": "Perennial",
    "woodiness": "Herbaceous",
    "height": "0.3–1.0 m"
  },
  "alternatives": []
}
```

Do not return `404` merely because there are no alternatives.

---

# 10. Epic 2 API — Compare Plants

## 10.1 Endpoint

```http
GET /api/v1/plants/compare
```

Purpose:

Return normalized data for side-by-side plant comparison.

Example:

```http
GET /api/v1/plants/compare?plantIds=205,301,418
```

---

## 10.2 Query parameter

| Parameter | Type | Required | Rules |
|---|---|---:|---|
| `plantIds` | comma-separated IDs | yes | 2–3 unique positive integer IDs |

Valid:

```text
plantIds=205,301
plantIds=205,301,418
```

Invalid:

```text
plantIds=
plantIds=205
plantIds=205,205
plantIds=205,301,418,500
plantIds=abc,301
plantIds=-1,301
```

The current frontend route query is:

```text
/compare?plants=id1,id2,id3
```

The frontend should translate that route query into the backend request parameter:

```text
plantIds=id1,id2,id3
```

---

## 10.3 Required comparison fields

Every returned plant supports the fixed comparison rows:

```text
Environmental concern
Legal status
Origin status
Growth form
Life history
Woodiness
Height
Local occurrence
```

---

## 10.4 Success response

### `200 OK`

```json
{
  "plants": [
    {
      "plantId": 205,
      "commonName": "Native Violet",
      "scientificName": "Viola hederacea",
      "imageUrl": "https://example.org/images/205.jpg",
      "environmentalConcern": "LOWER",
      "legalStatus": "NOT_REGULATED",
      "originStatus": "NATIVE",
      "growthForm": "Groundcover",
      "lifeHistory": "Perennial",
      "woodiness": "Herbaceous",
      "height": "0.1–0.2 m",
      "localOccurrence": {
        "status": "FOUND",
        "recordCount": 12,
        "latestRecordYear": 2024
      }
    }
  ]
}
```

The response should preserve the requested `plantIds` ordering whenever possible so the frontend comparison columns remain deterministic.

---

## 10.5 Missing values

Missing traits:

```json
{
  "growthForm": null,
  "lifeHistory": null,
  "woodiness": null,
  "height": null
}
```

Frontend will display `Unavailable`.

A `NOT_ASSESSED` concern remains:

```json
"environmentalConcern": "NOT_ASSESSED"
```

Frontend will display `Not Assessed`.

Do not convert it to `LOWER`.

---

## 10.6 Local occurrence

Local occurrence remains supporting evidence only.

It must not affect:

- environmental concern;
- recommendation;
- a comparison winner;
- any risk score.

If VBA fails for one compared plant, return:

```json
{
  "status": "UNAVAILABLE",
  "recordCount": null,
  "latestRecordYear": null
}
```

where feasible rather than failing the entire comparison.

---

## 10.7 Forbidden response fields

The Compare API must not return:

```text
winner
bestPlant
safestPlant
score
riskScore
recommendationRanking
```

Compare is descriptive, not prescriptive.

---

# 11. Error Handling

A consistent error envelope is recommended for all endpoints.

## 11.1 Error response

```json
{
  "code": "INVALID_REQUEST",
  "message": "plantIds must contain 2 to 3 unique positive integer IDs",
  "details": []
}
```

Recommended shape:

```text
code: stable machine-readable string
message: developer-readable message
details: optional array, [] when empty
```

---

## 11.2 Recommended HTTP status usage

| Status | Use |
|---|---|
| `200 OK` | Successful response, including empty search/catalog/alternatives results |
| `400 Bad Request` | Invalid query/path parameter syntax or validation |
| `404 Not Found` | Requested plant record does not exist |
| `500 Internal Server Error` | Unexpected backend failure |
| `503 Service Unavailable` | Required core dependency unavailable and endpoint cannot produce a meaningful response |

Partial optional evidence failure should normally be represented inside a successful domain response using `UNAVAILABLE`, not automatically converted into `503`.

---

## 11.3 Suggested error codes

```text
INVALID_REQUEST
INVALID_PLANT_ID
PLANT_NOT_FOUND
INVALID_PAGE
INVALID_PAGE_SIZE
INVALID_FILTER
INVALID_SORT
INVALID_COMPARE_SELECTION
INTERNAL_ERROR
SERVICE_UNAVAILABLE
```

Frontend should not depend on exact English error message text.

---

# 12. Validation Rules

## 12.1 Plant ID

```text
positive integer
```

Applies to:

- Assessment
- Alternatives
- Compare

---

## 12.2 Compare selection

Must contain:

```text
minimum 2
maximum 3
unique IDs
positive integer IDs
```

Validation occurs before querying comparison data.

---

## 12.3 Pagination

Recommended defaults:

```text
page = 0
size = 12
```

Recommended maximum page size:

```text
100
```

The exact maximum may be changed by backend, but should be documented and stable.

Negative `page` or non-positive `size` should return `400`.

---

## 12.4 Sort

Backend should whitelist supported sort fields rather than accepting arbitrary SQL/property names.

Recommended initial sorts:

```text
commonName,asc
commonName,desc
scientificName,asc
scientificName,desc
```

If concern sorting is implemented, use the explicit domain ranking rather than alphabetical enum order.

---

# 13. Recommended Backend DTOs

The following names are recommendations; backend may choose equivalent Java names while preserving the JSON contract.

```text
PlantIdentityDto

PlantSearchResponse
PlantSearchItemDto

PlantAssessmentResponse
VictorianEstablishmentDto
EnvironmentalConcernDto
LocalOccurrenceDto

PlantCatalogResponse
PlantCatalogItemDto

PlantAlternativesResponse
CurrentPlantDto
AlternativePlantDto

PlantComparisonResponse
ComparisonPlantDto

ApiErrorResponse
```

Recommended package structure:

```text
dto/
  common/
  search/
  assessment/
  catalog/
  alternatives/
  compare/
```

Avoid exposing persistence entities directly from controllers.

---

# 14. Recommended Controller Structure

One reasonable Spring MVC structure:

```text
PlantSearchController
  GET /api/v1/plants/search

PlantAssessmentController
  GET /api/v1/plants/{plantId}/assessment

PlantCatalogController
  GET /api/v1/plants

PlantAlternativesController
  GET /api/v1/plants/{plantId}/alternatives

PlantComparisonController
  GET /api/v1/plants/compare
```

Alternatively, the endpoints may be grouped under one `PlantController`, but service responsibilities should still remain separated.

Recommended service boundaries:

```text
PlantSearchService
PlantAssessmentService
PlantCatalogService
PlantAlternativeService
PlantComparisonService
```

Domain/data integration services may separately encapsulate:

```text
VicFlora
2022 Advisory List
VBA
AusTraits
```

This helps preserve data-source responsibility boundaries.

---

# 15. Frontend Integration Matrix

| Frontend area | Backend API | Main fields |
|---|---|---|
| Home search | `GET /plants/search` | plant identity |
| Assessment | `GET /plants/{id}/assessment` | identity, establishment, occurrence, concern, legal status, recommendation |
| Catalog | `GET /plants` | identity, concern, origin, optional traits, pagination |
| Alternatives | `GET /plants/{id}/alternatives` | current plant, lower-concern alternatives, match reasons |
| Compare | `GET /plants/compare` | fixed comparison fields |

Full URLs use the `/api/v1` prefix.

---

# 16. Frontend Compatibility Requirements

To avoid unnecessary frontend redesign, backend should preserve these field names:

```text
plantId
commonName
scientificName
family
imageUrl

originStatus
environmentalConcern
legalStatus
recommendation

growthForm
lifeHistory
woodiness
height

localOccurrence
recordCount
latestRecordYear

matchReasons

items
page
size
totalElements
totalPages
sort
```

Backend should not return human-readable placeholders in place of `null`.

Backend should not rename values into presentation-specific text such as:

```text
"High Risk"
"Native Plant"
"No Data"
```

Use enums/data values and let the frontend presentation layer provide labels.

---

# 17. Backend Acceptance Checklist

## Shared domain

- [ ] Stable positive integer `plantId`
- [ ] Shared enum serialization uses uppercase snake case
- [ ] Missing optional values are `null`
- [ ] Empty arrays are `[]`
- [ ] VBA does not determine environmental concern
- [ ] `NOT_FOUND` is not treated as local absence proof
- [ ] `NOT_ASSESSED` is not treated as low concern
- [ ] `UNAVAILABLE` remains distinct from `NOT_ASSESSED`
- [ ] Missing traits are not inferred
- [ ] Legal status is separate from environmental concern

## Search

- [ ] `GET /api/v1/plants/search`
- [ ] Case-insensitive common-name search
- [ ] Case-insensitive scientific-name search
- [ ] Limit validation
- [ ] Empty result returns `items: []`

## Assessment

- [ ] `GET /api/v1/plants/{plantId}/assessment`
- [ ] Plant identity returned
- [ ] Victorian establishment returned
- [ ] Local occurrence returned
- [ ] Environmental concern returned
- [ ] Legal status returned
- [ ] Recommendation calculated by backend
- [ ] VBA failure can produce `localOccurrence.status = UNAVAILABLE`
- [ ] VBA failure does not automatically fail entire assessment
- [ ] Missing plant returns `404`

## Catalog

- [ ] `GET /api/v1/plants`
- [ ] Name search implemented
- [ ] Environmental concern filter implemented
- [ ] Origin status filter implemented
- [ ] Server-side pagination implemented
- [ ] Zero-based page
- [ ] Assessed-only behaviour
- [ ] `NOT_ASSESSED` excluded
- [ ] Empty result is valid `200`
- [ ] Future AusTraits filters can be added without replacing the base response structure

## Alternatives

- [ ] `GET /api/v1/plants/{plantId}/alternatives`
- [ ] Only verified PlantAssure records
- [ ] Only lower documented concern candidates
- [ ] `NOT_ASSESSED` excluded
- [ ] `UNAVAILABLE` excluded from concern ranking
- [ ] Regulated candidates excluded
- [ ] Similarity uses supported traits only
- [ ] Missing traits are not inferred
- [ ] `matchReasons[]` returned
- [ ] No synthetic risk/safety score
- [ ] No candidates returns `200` + `alternatives: []`

## Compare

- [ ] `GET /api/v1/plants/compare`
- [ ] Accepts exactly 2–3 unique positive IDs
- [ ] Returns environmental concern
- [ ] Returns legal status
- [ ] Returns origin status
- [ ] Returns growth form
- [ ] Returns life history
- [ ] Returns woodiness
- [ ] Returns height
- [ ] Returns local occurrence
- [ ] Missing values remain `null`
- [ ] No winner/best/safest field
- [ ] No risk/aggregate score

---

# 18. Frontend Acceptance Checklist

Once backend is available, frontend integration should be possible without changing the current product structure.

## Search

- [ ] Replace current data source with `/api/v1/plants/search`
- [ ] Navigate selected `plantId` to Assessment

## Assessment

- [ ] Fetch by route `plantId`
- [ ] Render backend-owned recommendation
- [ ] Keep current concern presentation utility
- [ ] `UNAVAILABLE` local evidence renders as unavailable rather than failing page
- [ ] `NOT_ASSESSED` remains neutral
- [ ] `Find a Better Plant` only for `RECONSIDER_PLANTING` and `USE_CAUTION`

## Catalog

- [ ] Send search/filter state to backend
- [ ] Use backend pagination metadata
- [ ] Do not re-introduce mock records
- [ ] Preserve existing `PlantCard`
- [ ] Do not show `NOT_ASSESSED`

## Alternatives

- [ ] Use backend candidate list as authoritative
- [ ] Do not calculate lower concern on frontend
- [ ] Render backend `matchReasons`
- [ ] Preserve current compare selection behaviour

## Compare

- [ ] Parse frontend route query
- [ ] Request backend with `plantIds`
- [ ] Render fixed comparison fields
- [ ] Do not calculate winner or score

---

# 19. Implementation Priority

Recommended backend implementation order:

```text
1. Shared enums and DTO conventions
2. Plant search API
3. Assessment API
4. Catalog API
5. Alternatives API
6. Compare API
7. AusTraits-backed Catalog filters
```

This order allows the frontend to begin integration incrementally.

---

# 20. Contract Decisions Requiring Final Confirmation

Most of the contract is already defined by current frontend/product rules. The following items should be explicitly confirmed during backend implementation because the supplied project context does not fully define them:

## 20.1 Victorian establishment enum

The complete set of valid `victorianEstablishment.status` values must come from the actual VicFlora mapping.

Known example:

```text
NATURALISED
```

Do not invent the remaining enum values.

## 20.2 Origin `UNCERTAIN`

Keep `UNCERTAIN` only if the real source mapping supports it.

## 20.3 Catalog treatment of `UNAVAILABLE`

The product explicitly excludes `NOT_ASSESSED` from Catalog.

Because the page is “Browse Assessed Plants”, this contract recommends excluding `UNAVAILABLE` as well, but this should be confirmed as a final backend rule.

## 20.4 Overall Assessment concern `UNAVAILABLE`

The shared concern enum includes `UNAVAILABLE`, but the current recommendation enum does not define a matching recommendation value.

If whole-assessment environmental concern can genuinely be `UNAVAILABLE`, define the required product behaviour before exposing that state.

---

# 21. Out of Scope

The following are not part of this API contract:

- Epic 1 image upload/capture
- AI species identification
- AI candidate confidence
- image-classification endpoints
- Assessment preview routes
- frontend mock data
- recommendation based on VBA occurrence
- Catalog sun/shade filter
- Catalog soil filter
- Catalog drought-tolerance filter
- compare winner selection
- aggregate environmental risk score
- “safe plant” classification

---

# 22. Final Endpoint Summary

```http
# Iteration 1
GET /api/v1/plants/search?q={query}&limit={limit}

GET /api/v1/plants/{plantId}/assessment

# Epic 3
GET /api/v1/plants
    ?q={query}
    &environmentalConcern={value}
    &originStatus={value}
    &page={page}
    &size={size}
    &sort={field,direction}

# Epic 2
GET /api/v1/plants/{plantId}/alternatives?limit={limit}

GET /api/v1/plants/compare?plantIds={id1},{id2},{id3}
```

This contract should be treated as the shared integration baseline for backend implementation and frontend API integration.
