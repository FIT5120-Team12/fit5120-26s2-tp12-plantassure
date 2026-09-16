# Epic 1 Backend Acceptance Criteria Traceability

The Epic contains both frontend-only UI behaviour and backend responsibilities. This table identifies exactly what the backend implements.

| AC | Requirement | Backend implementation | Frontend responsibility |
|---|---|---|---|
| 1.1.1 | supported image accepted + preview | `POST /plants/identify` accepts supported multipart image | display local preview |
| 1.1.2 | replacing image replaces preview | no server state is retained | replace selected file/preview |
| 1.1.3 | removing image blocks submission | missing `image` part returns `INVALID_IMAGE` | clear preview + disable submit |
| 1.1.4 | unsupported/invalid file rejected | `ImageValidationService` + `INVALID_IMAGE` | show backend/frontend validation message |
| 1.1.5 | submit triggers identification only | `PlantIdentificationServiceImpl` never calls Assessment | stay on candidate screen |
| 1.2.1 | candidates show common/scientific names | AI scientific name + DB mapping in `SpeciesMatchingService` | render returned names |
| 1.2.2 | possible, not certain | returns a list; rank #1 is never auto-confirmed | label candidates as possible matches |
| 1.2.3 | score labeled correctly | field is `identificationConfidence` | render as identification confidence |
| 1.2.4 | no match | `NO_CONFIDENT_MATCH` + `matches: []` | show no-match + search by name option |
| 1.2.5 | service unavailable | `503 IDENTIFICATION_SERVICE_UNAVAILABLE` | show retry/search options |
| 1.3.1 | selecting candidate | stable candidate fields are returned | mark selected candidate |
| 1.3.2 | selecting different candidate | stateless candidate response supports switching | replace selected state |
| 1.3.3 | confirm matched species | `plantAssureMatch=true` + stable `plantId`; existing Assessment remains verified | call `/plants/{plantId}/assessment` after confirm |
| 1.3.4 | no DB assessment | `plantAssureMatch=false`, `plantId=null`; no AI assessment generated | show no-assessment message |

## Important semantic guarantee

```text
identificationConfidence
≠ environmentalConcern
≠ environmentalRisk
≠ recommendation
```

The AI module does not write to or modify the verified assessment fields.
