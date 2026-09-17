# PlantAssure Backend — Iteration 2 (Epic 1 + Epic 2 + Epic 3)

Spring Boot backend for FIT5120 Project 2 — PlantAssure.

This version is a **continuous-development upgrade of the Iteration 1 backend**, not a rewrite.
Existing I1 Search/Assessment behaviour remains available while Iteration 2 adds AI-assisted identification, Better Plant + Compare, and Catalog/Browse.

## Scope

Implemented backend scope:

```http
# I1 compatibility
GET  /api/v1/plants/search?q={query}&limit={limit}
GET  /api/v1/plants/{plantId}/assessment

# Epic 1 — AI-Assisted Plant Identification
POST /api/v1/plants/identify

# Epic 2 — Find a Better Plant + Compare
GET  /api/v1/plants/{plantId}/alternatives?limit={limit}
GET  /api/v1/plants/compare?plantIds={id1},{id2},{id3}

# Epic 3 — Plant Catalog & Browse
GET  /api/v1/plants
```

## Technology

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- MyBatis-Plus 3.5.7
- MySQL 8+
- Spring `RestClient` for AI provider integration
- Lombok
- Springdoc OpenAPI

## Epic 1 — AI identification flow

```text
Vue uploads image
        ↓
POST /api/v1/plants/identify
        ↓
ImageValidationService
        ↓
PlantIdentificationClient
        ↓
External AI provider
        ↓
scientificName + identificationConfidence candidates
        ↓
SpeciesMatchingService
        ↓
PlantAssure species_data
        ↓
Top 3 possible matches returned to frontend
        ↓
User selects/confirm one candidate
        ↓
GET /api/v1/plants/{plantId}/assessment
```

Important boundaries:

- AI identification is **not** an environmental assessment.
- The backend does not auto-confirm the highest-confidence candidate.
- The response returns up to Top 3 possible matches by default.
- `identificationConfidence` is separate from environmental concern/risk.
- A candidate that does not map to PlantAssure remains visible with `plantId: null` and `plantAssureMatch: false`.
- Only a confirmed candidate with a real `plantId` may open the existing verified Assessment API.

### Image rules

Accepted:

```text
image/jpeg
image/png
image/webp
```

Maximum size:

```text
10 MB
```

The backend validates both MIME type and basic file signature. It does not trust the original filename.

### AI provider configuration

The actual AI service URL/schema was not supplied in the project files, so provider integration is isolated behind `PlantIdentificationClient`.

Environment variables:

```text
PLANT_IDENTIFICATION_ENABLED=true
PLANT_IDENTIFICATION_BASE_URL=http://localhost:8000
PLANT_IDENTIFICATION_ENDPOINT=/identify
PLANT_IDENTIFICATION_API_KEY=
PLANT_IDENTIFICATION_TOP_CANDIDATES=3
PLANT_IDENTIFICATION_MIN_CONFIDENCE=0.0
PLANT_IDENTIFICATION_CONNECT_TIMEOUT=3s
PLANT_IDENTIFICATION_READ_TIMEOUT=15s
```

Until the real AI service is deployed, keep `PLANT_IDENTIFICATION_ENABLED=false`. In that state only `/identify` returns `503 IDENTIFICATION_SERVICE_UNAVAILABLE`; Search/Assessment/Catalog/Alternatives/Compare remain usable.

The current HTTP adapter accepts a small compatibility set for provider response fields. Once the AI team freezes its real response JSON, narrow `HttpPlantIdentificationClient.parseCandidates(...)` to that final contract and add an integration test.

See:

```text
docs/EPIC1_AI_IDENTIFICATION.md
docs/EPIC1_AC_TRACEABILITY.md
docs/EPIC1_APIFOX_TESTS.md
```

## Database

Use:

```text
src/main/resources/db/species_data_i2_backend.sql
```

This script is derived from the supplied `species_data_i2.sql`, with backend-owned schema changes:

1. adds numeric `id` primary key;
2. preserves all 777 I1 plant IDs by exact scientific-name matching;
3. assigns the 103 new I2 records IDs 778–880;
4. keeps `match_key` non-unique because subspecies/variety rows can share it;
5. uses the table name `species_data` so the existing I1 Entity/Mapper remains compatible.

Epic 1 does not require a new database table for the MVP. The current matching sequence is:

```text
exact accepted scientific_name
→ unique species-level match_key
→ no PlantAssure match
```

A verified synonym table can be added later when the team has an authoritative synonym dataset. The backend does not use uncontrolled fuzzy matching to invent species matches.

## Important I2 environmental rules

Only the 2022 Advisory List determines environmental concern and backend recommendation.

```text
VERY_HIGH / HIGH          -> RECONSIDER_PLANTING
MODERATELY_HIGH / MEDIUM  -> USE_CAUTION
LOWER                     -> LOWER_CONCERN
NOT_ASSESSED              -> NOT_ASSESSED
```

`Not Assessed / No exact match` is never treated as low concern.

VBA100/ALA remain supporting occurrence evidence only and never change environmental concern.

## Alternatives

The backend does **not** trust `alternatives_json` as the final answer. It revalidates candidates using:

1. verified PlantAssure database record;
2. documented concern lower than current plant;
3. NOT_ASSESSED / UNAVAILABLE excluded;
4. supported traits only: growth form, life history, woodiness, height;
5. missing traits are not inferred;
6. at least one supported similarity reason must exist.

If no valid alternative exists, the endpoint returns HTTP 200 with `alternatives: []`.

## Legal status limitation

The supplied I2 dataset does not contain a verified Victorian statutory classification. Therefore this build returns:

```json
"legalStatus": "UNAVAILABLE"
```

It does not silently convert unavailable data to `NOT_REGULATED`.

## Catalog filters

Supported now:

- q (common/scientific name)
- environmentalConcern
- originStatus
- growthForm
- lifeHistory
- woodiness
- minHeight / maxHeight
- page / size
- commonName/scientificName sort

Catalog is assessed-only and excludes NOT_ASSESSED / UNAVAILABLE concern records.

## Compare

`plantIds` must contain 2–3 unique positive IDs. The response preserves requested order and never returns winner, safestPlant, riskScore or aggregate score.

## Build

```bash
mvn clean test
mvn clean package
```

The archive intentionally does not include an old pre-built JAR because that binary would be stale after source changes.

## Validation note

In the ChatGPT execution environment, Maven itself was unavailable. Main source code was compiled successfully using Java 17 target compatibility (`javac --release 17`) and dependency JARs extracted from the previously built Spring Boot fat JAR.

Run `mvn clean test` locally before deployment to execute the full JUnit/Mockito suite and produce the final Iteration 2 JAR.
