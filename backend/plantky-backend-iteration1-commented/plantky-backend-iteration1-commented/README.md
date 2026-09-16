# PlantAssure Backend — Iteration 2

Spring Boot backend for FIT5120 Project 2 — PlantAssure.

This version is a **continuous-development upgrade of the Iteration 1 backend**, not a rewrite.
Existing I1 Search/Assessment behaviour is preserved where practical, while Epic 2 and Epic 3 APIs are added.

## Scope

Implemented backend scope:

```http
# I1 compatibility
GET /api/v1/plants/search?q={query}&limit={limit}
GET /api/v1/plants/{plantId}/assessment

# Epic 3 — Plant Catalog & Browse
GET /api/v1/plants

# Epic 2 — Find a Better Plant + Compare
GET /api/v1/plants/{plantId}/alternatives?limit={limit}
GET /api/v1/plants/compare?plantIds={id1},{id2},{id3}
```

Not implemented in this backend revision:

- Epic 1 AI image upload / identification
- AI confidence scores
- AI species confirmation endpoint

The supplied frontend backend-contract document explicitly marks Epic 1 AI Identification as out of scope.

## Technology

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- MyBatis-Plus 3.5.7
- MySQL 8+
- Lombok
- Springdoc OpenAPI

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

The data-team raw SQL does not need to be edited inside this project copy. For the final shared pipeline/schema, however, the data team should ideally add the stable `id` primary key so the team has one authoritative schema.

## Important I2 business rules

### Environmental concern

Only the 2022 Advisory List determines environmental concern and backend recommendation.

```text
VERY_HIGH / HIGH          -> RECONSIDER_PLANTING
MODERATELY_HIGH / MEDIUM  -> USE_CAUTION
LOWER                     -> LOWER_CONCERN
NOT_ASSESSED              -> NOT_ASSESSED
```

`Not Assessed / No exact match` is explicitly mapped to `NOT_ASSESSED`.
It is never treated as low concern.

### Local occurrence

Iteration 2 source changes:

```text
I1 VBA25                  -> I2 VBA_FLORA100
I1 iNaturalist via GBIF   -> I2 ALA Monash download
```

VBA/ALA remain supporting evidence only and never change environmental concern.

### Alternatives

The backend does **not** trust `alternatives_json` as the final answer.
It revalidates candidates using:

1. verified PlantAssure database record;
2. documented concern lower than current plant;
3. NOT_ASSESSED / UNAVAILABLE excluded;
4. supported traits only: growth form, life history, woodiness, height;
5. missing traits are not inferred;
6. at least one supported similarity reason must exist.

If no valid alternative exists, the endpoint returns HTTP 200 with `alternatives: []`.

### Legal status

The supplied I2 dataset does not contain a verified Victorian statutory classification.
Therefore this build returns:

```json
"legalStatus": "UNAVAILABLE"
```

and also exposes an explanatory `legalStatusDetail` in Assessment.
It does **not** silently convert unavailable data to `NOT_REGULATED`.

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

`plantIds` must contain 2–3 unique positive IDs.
The response preserves requested order and returns:

- environmental concern
- legal status
- origin/native status
- growth form
- life history
- woodiness
- height
- local occurrence

It never returns winner, safestPlant, riskScore or aggregate score.

## Build

```bash
mvn clean test
mvn clean package
```

The modified archive intentionally does not include the old I1 pre-built JAR because that binary would be stale after source changes.

## Validation note

In the ChatGPT execution environment, Maven itself was unavailable. Main source code was nevertheless compiled successfully with `javac --release 17` using the dependency JARs extracted from the previously built I1 Spring Boot fat JAR. Run `mvn clean test` locally before deployment to execute the full JUnit/Mockito suite and produce a new I2 JAR.
