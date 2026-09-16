# Epic 1 Apifox Test Guide

## Before testing with a real AI service

Set:

```text
PLANT_IDENTIFICATION_ENABLED=true
PLANT_IDENTIFICATION_BASE_URL=<AI service base URL>
PLANT_IDENTIFICATION_ENDPOINT=<AI identify endpoint>
```

If the AI service requires a bearer key:

```text
PLANT_IDENTIFICATION_API_KEY=<secret>
```

## Case 1 — valid JPEG

```http
POST /api/v1/plants/identify
Content-Type: multipart/form-data
```

Form-data:

```text
image = <real .jpg file>
```

Expected:

```text
200 OK
status = MATCHES_FOUND or NO_CONFIDENT_MATCH
matches = array
```

When matches exist, verify every item has:

```text
scientificName
identificationConfidence
plantAssureMatch
```

and that at most 3 candidates are returned under the default config.

## Case 2 — unsupported file

Upload `.gif`, `.txt` or another unsupported type.

Expected:

```text
400 INVALID_IMAGE
```

## Case 3 — fake JPG

Rename a text/executable file to `.jpg` and submit it as `image/jpeg`.

Expected:

```text
400 INVALID_IMAGE
```

The backend checks the file signature, not only the filename/MIME declaration.

## Case 4 — file > 10 MB

Expected:

```text
400 INVALID_IMAGE
```

## Case 5 — missing image

Send the multipart request without an `image` part.

Expected:

```text
400 INVALID_IMAGE
```

## Case 6 — AI provider disabled/unavailable

Set:

```text
PLANT_IDENTIFICATION_ENABLED=false
```

Expected:

```text
503 IDENTIFICATION_SERVICE_UNAVAILABLE
```

Then confirm these existing APIs still work normally:

```http
GET /api/v1/plants/search?q=wattle
GET /api/v1/plants/{plantId}/assessment
GET /api/v1/plants
```

## Case 7 — AI candidate has no PlantAssure match

Expected candidate shape:

```json
{
  "plantId": null,
  "commonName": null,
  "scientificName": "...",
  "identificationConfidence": 0.81,
  "plantAssureMatch": false
}
```

The frontend must not open an assessment for that candidate.
