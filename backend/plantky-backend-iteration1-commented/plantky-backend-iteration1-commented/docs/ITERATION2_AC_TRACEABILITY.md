# Iteration 2 Backend AC Traceability

## Epic 2 — Find a Better Plant + Compare

| Requirement | Backend implementation |
|---|---|
| lower documented concern only | `PlantAlternativeServiceImpl` + `EnvironmentalConcern` ranking |
| verified records | candidates queried only from `species_data` |
| supported traits only | growthForm, lifeHistory, woodiness, height |
| missing traits not inferred | null values simply do not create match reasons |
| no suitable alternatives | HTTP 200 + `alternatives: []` + status |
| compare 2–3 plants | `PlantComparisonServiceImpl` validation |
| environmental/legal separate | separate enum fields |
| local occurrence is evidence | `LocalOccurrenceVO`, never used in concern ranking |
| missing compare values | null / UNAVAILABLE, never guessed |
| selecting plant opens own assessment | stable `plantId` returned for every result |
| no winner/score | response model contains no such fields |

## Epic 3 — Catalog & Browse

| Requirement | Backend implementation |
|---|---|
| assessed plants only | base query restricts to five documented concern DB values |
| name/risk cards | `PlantCatalogItemVO` |
| selecting card opens assessment | stable `plantId` returned |
| risk filter | `environmentalConcern` repeated query parameter |
| native/origin filter | `originStatus` repeated query parameter |
| life history | `lifeHistory` filter |
| plant type | `growthForm` filter |
| size | `minHeight` / `maxHeight` range-overlap filter |
| no results | normal HTTP 200 with `items: []` |

## I1 compatibility / I2 data updates

| Change | Implementation |
|---|---|
| VBA25 -> VBA100 | `SpeciesDataEntity` + `OccurrenceService` |
| iNaturalist -> ALA | `SupportingEvidenceVO` |
| AusTraits | `TraitService` + Entity fields |
| GRIIS | `GriisEvidenceService` |
| legal data absent | `LegalStatus.UNAVAILABLE` |
| stable ID | `species_data_i2_backend.sql` |
| I1 search response | `results` retained; `items` alias added |
| I1 local occurrence year | `mostRecentRecordYear` retained; `latestRecordYear` alias added |

## Epic 1 — AI-Assisted Plant Identification

See the dedicated detailed matrix:

```text
docs/EPIC1_AC_TRACEABILITY.md
```

Backend coverage includes multipart image validation, AI-provider abstraction, Top-N possible matches, `identificationConfidence`, no-match state, provider-unavailable error, controlled PlantAssure scientific-name matching, and safe handoff to the existing verified Assessment API after user confirmation.
