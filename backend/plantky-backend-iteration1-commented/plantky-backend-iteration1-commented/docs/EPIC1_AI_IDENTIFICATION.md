# Epic 1 Backend — AI-Assisted Plant Identification

## 1. Purpose

Epic 1 lets a user submit a plant photo without knowing the plant name.

The backend must return possible species candidates with identification confidence, while keeping AI identification strictly separate from PlantAssure's verified environmental assessment.

## 2. Endpoint

```http
POST /api/v1/plants/identify
Content-Type: multipart/form-data
```

Multipart field:

```text
image
```

Supported image types:

```text
image/jpeg
image/png
image/webp
```

Maximum image size:

```text
10 MB
```

## 3. Successful response

Example:

```json
{
  "status": "MATCHES_FOUND",
  "matches": [
    {
      "plantId": 3,
      "commonName": "Cootamundra Wattle",
      "scientificName": "Acacia baileyana",
      "identificationConfidence": 0.92,
      "plantAssureMatch": true
    },
    {
      "plantId": null,
      "commonName": null,
      "scientificName": "Example species",
      "identificationConfidence": 0.76,
      "plantAssureMatch": false
    }
  ]
}
```

The default backend limit is Top 3 candidates. The backend sorts by identification confidence but **does not auto-confirm rank #1**.

## 4. No confident match

```json
{
  "status": "NO_CONFIDENT_MATCH",
  "matches": []
}
```

This is a normal HTTP 200 business outcome. The frontend should offer retry and/or normal name search.

The requirements do not define a numeric confidence threshold. Therefore `minimum-confidence` is configuration-driven and defaults to 0.0 until the AI team provides a validated threshold.

## 5. Candidate → PlantAssure matching

Current MVP matching:

```text
AI scientificName
      ↓
exact species_data.scientific_name
      ↓ if not found
normalised genus + species match_key
      ↓ only if unique
PlantAssure record
```

If matching remains ambiguous or no record exists:

```text
plantId = null
commonName = null
plantAssureMatch = false
```

No fuzzy/Levenshtein match is used because it could attach the wrong verified assessment to an AI guess.

A future verified `plant_synonym` table can be inserted between exact matching and species-level match_key matching.

## 6. Confirm flow

Selecting/replacing the selected candidate is frontend state.

When the user confirms a candidate where:

```text
plantAssureMatch = true
plantId != null
```

frontend calls the existing verified endpoint:

```http
GET /api/v1/plants/{plantId}/assessment
```

There is deliberately no second AI assessment endpoint and no `POST /confirm` endpoint in the MVP.

If `plantAssureMatch=false`, frontend must show a no-assessment message and must not present AI-generated content as verified PlantAssure assessment data.

## 7. Service separation

```text
PlantIdentificationController
        ↓
PlantIdentificationServiceImpl
        ├── ImageValidationService
        ├── PlantIdentificationClient
        │       ↓
        │  external AI provider
        └── SpeciesMatchingService
                ↓
           species_data
```

### PlantIdentificationController

Only handles HTTP/multipart boundary and returns DTO.

### ImageValidationService

Checks:

- non-empty file;
- <= 10 MB;
- allowed MIME type;
- JPG/PNG/WebP file signature.

### PlantIdentificationClient

Provider abstraction. The real URL/API key are configuration values.

### HttpPlantIdentificationClient

Current HTTP adapter. Because the AI team's final JSON was not supplied, provider-specific JSON parsing is isolated here.

### SpeciesMatchingService

Maps AI scientific name to an existing PlantAssure record without uncontrolled fuzzy inference.

### PlantIdentificationServiceImpl

Coordinates validation → AI → Top N → DB matching → frontend response.

It never calls `AssessmentOrchestrator` automatically.

## 8. AI provider configuration

```yaml
plantky:
  identification:
    enabled: false
    base-url: http://localhost:8000
    endpoint: /identify
    api-key:
    max-image-size-mb: 10
    top-candidates: 3
    minimum-confidence: 0.0
    connect-timeout: 3s
    read-timeout: 15s
```

Production should inject provider secrets through environment variables.

## 9. Expected provider contract

Until the AI team publishes its final contract, the preferred minimal response is:

```json
{
  "candidates": [
    {
      "scientificName": "Acacia baileyana",
      "confidence": 0.92
    }
  ]
}
```

Only identification facts belong here. AI should not return or decide:

```text
environmental concern
recommendation
legal status
local occurrence
plantId
```

Those remain PlantAssure/backend responsibilities.

## 10. Error contract

Invalid image:

```http
400 Bad Request
```

```json
{
  "code": "INVALID_IMAGE",
  "message": "Please upload a valid JPG, PNG or WebP image.",
  "path": "/api/v1/plants/identify"
}
```

Provider unavailable/unconfigured:

```http
503 Service Unavailable
```

```json
{
  "code": "IDENTIFICATION_SERVICE_UNAVAILABLE",
  "message": "Plant identification is temporarily unavailable. Please try again or search for the plant by name.",
  "path": "/api/v1/plants/identify"
}
```

AI failures do not disable the ordinary PlantAssure search/assessment APIs.
