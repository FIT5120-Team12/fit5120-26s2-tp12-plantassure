# Iteration 1 Frontend and API Technical Specification (Revised US/AC Synchronized Version)

## Project: Plantky / Check Before You Plant — City of Monash

**Audience:** Frontend Development / Backend Development / Testing  
**Scope:** Iteration 1  
**Status:** Updated according to the latest revised User Stories and Acceptance Criteria (including the teacher's feedback on the positive-search acceptance criteria for US 1.1)  
**Core Screens:** 2  
**Core APIs:** 2

---

# 1. Document Purpose

This document translates the latest Iteration 1 User Stories (US) and Acceptance Criteria (AC) into implementable and testable frontend and backend technical requirements.

The core Iteration 1 user flow is:

```text
Home / Plant Search
   ↓
Search / Autocomplete
   ↓
User selects one clearly identified species
   ↓
Plant Assessment Result
   ├─ Plant Identity
   ├─ Establishment Status
   ├─ City of Monash Local Occurrence
   ├─ Environmental Weed Risk
   ├─ Rule-Based Planting Recommendation
   └─ Plain-Language Explanation + Sources
```

---

# 2. Iteration 1 Functional Scope

## 2.1 Included in This Iteration

- Plant Search on the Home page
- Common name / scientific name search
- Autocomplete
- Selection among multiple matching species
- No-match handling
- Plant identity
- Victorian establishment status
- Degree of establishment
- City of Monash VBA occurrence evidence
- Environmental weed risk
- Assessed / Not Assessed handling
- Rule-based planting recommendation
- Plain-language explanation
- Data source attribution
- Missing data / partial data failure handling

## 2.2 Not Included in This Iteration

- User postcode / address
- Dynamic geolocation
- PostGIS radius search
- Suburb-level spatial query
- Interactive Mapbox map
- Official legal weed classification
- Conservation-area proximity
- Plant comparison
- AI chat
- Saved history
- Alternative recommendation
- Machine-learning prediction

---

# 3. Page Structure

Iteration 1 requires only two core pages.

## 3.1 Screen A — Home / Plant Search

### Purpose

Satisfies:

- US 1.1 — Search for a Plant
- US 1.2 — Autocomplete While Typing

### Page Requirements

After the Home screen loads, the plant search field must be immediately visible and ready for input.

### UI Components

| UI Element | Technical Requirement |
|---|---|
| Search field | Supports common name / scientific name |
| Search submit action | User can actively submit a keyword |
| Autocomplete dropdown | Displays suggestions after at least 3 characters are entered |
| Search results list | Displays matching species |
| Multiple-match state | Displays all matching species and allows the user to select one |
| No-match state | Consistently displays `No matches found` |
| Loading state | Visible while the search request is in progress |
| Error state | Displays a recoverable message when the API fails |

### Suggested Route

```text
/
```

or:

```text
/home
```

A separate `/plants/search` page is no longer required.

---

## 3.2 Screen B — Plant Assessment Result

### Purpose

Satisfies:

- US 1.3 — Plant Identity
- US 1.4 — Degree of Establishment
- US 2.1 — Local Occurrence
- US 2.2 — Environmental Weed Risk
- US 3.1 — Combined Planting Recommendation
- US 3.2 — Understand the Recommendation

### Suggested Route

```text
/plants/:plantId/assessment
```

### The Page Must Include

| Section | Content |
|---|---|
| Verdict Summary | Lower Concern / Use Caution / Reconsider Planting / Not Assessed |
| Plain-Language Explanation | Natural-language explanation of the verdict |
| Identity | Scientific name / common name |
| Establishment | Native / Introduced / degree of establishment |
| Local Occurrence | VBA matching record count / latest year / no-record state |
| Environmental Risk | Assessed / Not Assessed / rating / explanation |
| Data Sources | VicFlora / VBA / 2022 Advisory List |
| Warning Area | Partial data failure / unavailable source |

Supporting evidence must remain visible below the verdict and does not require a separate third page.

---

# 4. API Overview

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/plants/search?q={keyword}` | Search + autocomplete |
| GET | `/api/v1/plants/{plantId}/assessment` | Returns the complete plant assessment |

---

# 5. API 1 — Plant Search

## 5.1 Request

```http
GET /api/v1/plants/search?q=wattle
```

## 5.2 Response

```json
{
  "query": "wattle",
  "results": [
    {
      "plantId": "vicflora-12345",
      "scientificName": "Acacia example",
      "commonName": "Example Wattle"
    },
    {
      "plantId": "vicflora-67890",
      "scientificName": "Acacia example2",
      "commonName": "Another Wattle"
    }
  ]
}
```

## 5.3 No Match

```json
{
  "query": "zzzzz",
  "results": []
}
```

The frontend must display:

```text
No matches found
```

It must not display a blank area.

## 5.4 Valid Search Behaviour

For a valid common name or scientific name:

- If a matching plant exists in the database, the Search API must return one or more matching results.
- Each result must contain at least `plantId`, `scientificName`, and `commonName` where available.
- The positive path for a valid search is an independent acceptance requirement of US 1.1 and must not be verified only indirectly through the fact that a result is clickable when results happen to exist.

Example:

```http
GET /api/v1/plants/search?q=Vinca%20major
```

```json
{
  "query": "Vinca major",
  "results": [
    {
      "plantId": "vicflora-12345",
      "scientificName": "Vinca major",
      "commonName": "Blue Periwinkle"
    }
  ]
}
```

---

# 6. US 1.1 — Search for a Plant Technical Mapping

## User Story

> As a residential gardener in the City of Monash, I want to search for a plant by its common or scientific name, so that I can quickly find information about it before deciding whether to plant it.

## AC 1.1.1 — Plant search is available

Given the app has loaded,  
When the tester views the home screen,  
Then a plant search field is visible and ready for input.

### Technical Requirements

- The Search field must appear on the Home screen.
- After the page loads, users must be able to type without additional navigation.
- The Search input must not be disabled by default.

## AC 1.1.2 — Valid search returns matching results

Given the plant search field is available,  
When the tester enters a valid common name or scientific name and submits the search,  
Then the system displays one or more matching plant results containing the relevant scientific name and common name where available.

### Technical Requirements

- The Search API must support both valid common-name and valid scientific-name queries.
- When matching species exist in the database, the response must return one or more matching results.
- Each search result must return at least:
  - `plantId`
  - `scientificName`
  - `commonName` (where available)
- The frontend must display matching results to the user and must not incorrectly show `No matches found` for a valid query.

Successful response example:

```json
{
  "query": "Blue Periwinkle",
  "results": [
    {
      "plantId": "vicflora-12345",
      "scientificName": "Vinca major",
      "commonName": "Blue Periwinkle"
    }
  ]
}
```

## AC 1.1.3 — Plant can be selected

Given search results are displayed,  
When the tester selects a plant from the results list,  
Then the app navigates to that plant's assessment page.

### Technical Requirements

The backend search response must return `plantId`. After the frontend selects a result:

```text
selected plantId
   ↓
navigate
   ↓
/plants/{plantId}/assessment
```

## AC 1.1.4 — No results found

Given the search field is open,  
When the tester types a term that matches no species and submits,  
Then the system displays `No matches found` rather than a blank screen.

### Technical Requirements

- An empty search result uses HTTP 200.
- The response uses `results: []`.
- The UI wording is fixed as `No matches found`.

## AC 1.1.5 — Ambiguous matches are resolved

Given a term matching multiple distinct species has been entered,  
When the tester submits the search,  
Then the system displays a selectable list rather than auto-selecting a species.

### Technical Requirements

- The backend must return multiple distinct taxa.
- The frontend must not automatically select the first item.
- Multiple taxa must not be silently merged.

---

# 7. US 1.2 — Autocomplete While Typing Technical Mapping

## User Story

> As a residential gardener in the City of Monash, I want to see autocomplete suggestions as I type a plant name, so that I can find the correct plant faster even if I'm unsure of the exact spelling.

## AC 1.2.1 — Suggestions appear while typing

Given the search field is open,  
When the tester types 3 or more characters,  
Then a dropdown of matching species names appears within 1 second.

### Technical Requirements

- Minimum autocomplete trigger length: `3 characters`
- Recommended frontend debounce: `200–300 ms`
- Under normal operating conditions, the suggestion dropdown should appear within 1 second

## AC 1.2.2 — Suggestion can be selected

Given the autocomplete dropdown is visible,  
When the tester selects a suggestion,  
Then the app navigates to that plant's assessment page.

### Technical Requirements

Each autocomplete item must contain `plantId`. After a suggestion is selected, navigate directly to:

```text
/plants/{plantId}/assessment
```

A second search request is not required.

## AC 1.2.3 — No matches while typing

Given the search field is open,  
When the tester types 3 or more characters that match no species name,  
Then the dropdown displays `No matches found` rather than remaining empty.

### Technical Requirements

When autocomplete returns no results, the dropdown must remain visible and display `No matches found`.

---

# 8. Plant Identity / Establishment Data Structure

The Assessment API is recommended to return:

```json
{
  "plant": {
    "plantId": "vicflora-12345",
    "scientificName": "Vinca major",
    "commonName": "Blue Periwinkle",
    "family": "Apocynaceae",
    "establishmentMeans": "Introduced",
    "degreeOfEstablishment": "Naturalised"
  }
}
```

Frontend rules:

- `scientificName`: must be displayed
- `commonName = null`: display `Not available`
- `establishmentMeans = null`: display `Not available`
- `degreeOfEstablishment = null`: display `Not available`
- Do not display raw VicFlora column names

---

# 9. US 2.1 — Local Occurrence Technical Requirements

The role of VBA is fixed as:

> City of Monash local occurrence evidence

It is not an environmental risk score.

Recommended response:

```json
{
  "localOccurrence": {
    "status": "FOUND",
    "recordCount": 8,
    "mostRecentRecordYear": 2025,
    "source": "Victorian Biodiversity Atlas"
  }
}
```

Supported values:

```text
FOUND
NOT_FOUND
UNAVAILABLE
```

When there are no matching VBA records:

```text
No matching VBA records were found in the City of Monash.
```

Do not display:

```text
This plant does not occur in Monash.
```

Because:

```text
absence of records ≠ evidence of absence
```

---

# 10. US 2.2 — Check Environmental Weed Risk Technical Mapping

## User Story

> As a gardener, I want to see a plant's environmental weed risk rating, so that I can understand its assessed environmental weed risk in Victoria.

## Recommended Response

```json
{
  "environmentalRisk": {
    "assessmentStatus": "ASSESSED",
    "rating": "High",
    "explanation": "This species has a High environmental weed risk rating in the 2022 Advisory List.",
    "source": "2022 Advisory List of Environmental Weeds in Victoria"
  }
}
```

Supported values:

```text
ASSESSED
NOT_ASSESSED
UNAVAILABLE
```

## AC 2.2.1 — Risk rating is displayed when assessed

If the selected plant has an exact match in the Advisory List, the frontend displays the environmental weed risk rating recorded in the dataset. The backend must not generate a new risk rating on its own.

## AC 2.2.2 — Not Assessed is stated clearly

When there is no exact match:

```json
{
  "assessmentStatus": "NOT_ASSESSED",
  "rating": null
}
```

The frontend displays:

```text
Not Assessed
```

and explains:

```text
No exact matching assessment was found in the 2022 Advisory List.
```

It must not be interpreted as `Low Risk` or `Safe`.

## AC 2.2.3 — Rating includes a plain-language explanation

The Risk section must include both:

- rating
- one-sentence plain-language explanation

It must not display only an unexplained technical value / label.

## AC 2.2.4 — Data source is attributed

Use the consistent source name:

```text
2022 Advisory List of Environmental Weeds in Victoria
```

---

# 11. API 2 — Complete Assessment Response

```json
{
  "plant": {
    "plantId": "vicflora-12345",
    "scientificName": "Vinca major",
    "commonName": "Blue Periwinkle",
    "family": "Apocynaceae",
    "establishmentMeans": "Introduced",
    "degreeOfEstablishment": "Naturalised"
  },
  "localOccurrence": {
    "status": "FOUND",
    "recordCount": 8,
    "mostRecentRecordYear": 2025,
    "source": "Victorian Biodiversity Atlas"
  },
  "environmentalRisk": {
    "assessmentStatus": "ASSESSED",
    "rating": "High",
    "explanation": "This species has a High environmental weed risk rating in the 2022 Advisory List.",
    "source": "2022 Advisory List of Environmental Weeds in Victoria"
  },
  "recommendation": {
    "level": "RECONSIDER_PLANTING",
    "displayLabel": "Reconsider Planting",
    "explanation": "This plant is introduced and naturalised in Victoria, has documented occurrence records in the City of Monash, and has a High environmental weed risk rating."
  },
  "sources": [
    {
      "name": "VicFlora",
      "role": "Plant identity and establishment status"
    },
    {
      "name": "Victorian Biodiversity Atlas",
      "role": "City of Monash local occurrence evidence"
    },
    {
      "name": "2022 Advisory List of Environmental Weeds in Victoria",
      "role": "Environmental weed risk"
    }
  ],
  "warnings": []
}
```

---

# 12. US 3.1 — Receive a Combined Planting Recommendation

## User Story

> As a residential gardener in the City of Monash, I want a single, clear recommendation combining establishment status, local occurrence, and environmental risk, so that I get a direct answer rather than raw data to interpret myself.

## AC 3.1.1 — A single verdict is returned

After the assessment process is complete, exactly one of the following must be returned:

```text
Lower Concern
Use Caution
Reconsider Planting
Not Assessed
```

The frontend uses `recommendation.displayLabel` and must not recalculate the verdict itself.

## AC 3.1.2 — Verdict logic reflects validated environmental risk evidence

| Environmental Risk | Verdict |
|---|---|
| Very High | Reconsider Planting |
| High | Reconsider Planting |
| Moderately High | Use Caution |
| Medium | Use Caution |
| Lower / validated lower-concern category | Lower Concern |
| No exact Advisory List assessment | Not Assessed |

Core restrictions:

- VicFlora establishment status and VBA local occurrence are supporting/contextual evidence only.
- VBA record count must not independently increase or decrease the environmental risk category.
- Rules such as `many local records → Reconsider Planting`, `few local records → Use Caution`, or `no local records → Lower Concern` are not allowed.

## AC 3.1.3 — Partial data failures are handled transparently

If an evidence source is unavailable:

- The backend returns `UNAVAILABLE` in the corresponding section
- `warnings[]` identifies the check that could not be completed
- The frontend must display the warning
- Missing data must not be silently filled in
- A misleading verdict must not be displayed

---

# 13. US 3.2 — Understand the Recommendation

## User Story

> As a residential gardener in the City of Monash, I want a plain-language explanation of why I received the recommendation, so that I can understand and trust the result.

## AC 3.2.1 — Explanation references specific evidence

The explanation should reference the evidence that is actually available for the current plant, for example:

```text
This plant is introduced and naturalised in Victoria, has documented occurrence records in the City of Monash, and has a High environmental weed risk rating.
```

It should clearly distinguish between:

- establishment evidence
- local occurrence evidence
- environmental risk evidence

The three types of evidence must not be described as if they were the same risk metric.

## AC 3.2.2 — Explanation avoids raw technical fields

Allowed:

```text
This plant is introduced and naturalised in Victoria.
```

Not allowed:

```text
establishment_means=INTRODUCED
record_count=8
risk_code=H
```

Do not expose:

- raw database field names
- internal enums
- source dataset column names
- unexplained technical scores

## AC 3.2.3 — Supporting data remains visible

The following supporting sections must remain visible below the verdict:

```text
Identity
Establishment
Occurrence
Risk
```

These four supporting sections must not be replaced or hidden by the summary verdict.

---

# 14. Recommendation Logic — Backend Ownership

Recommendation logic must be executed by the Spring Boot backend.

The frontend is not responsible for:

- calculating risk
- combining the verdict
- adjusting risk according to VBA count
- guessing a verdict when data is missing

Recommended backend structure:

```text
PlantIdentityService
OccurrenceService
RiskAssessmentService
RecommendationService
AssessmentOrchestrator
```

---

# 15. Data Matching Rules

1. Primary matching key: accepted scientific name
2. Prefer exact scientific-name match
3. Use synonyms only when a verified accepted-name mapping exists
4. Do not use uncontrolled fuzzy matching to force joins
5. For subspecies / varieties without an exact risk match, default to `Not Assessed`
6. Do not automatically inherit the parent species risk unless there is a verified parent-taxon rule
7. Missing data must not be filled in arbitrarily

---

# 16. Missing Data / Error Handling

| Scenario | Backend | Frontend |
|---|---|---|
| Search no match | `results: []` | `No matches found` |
| Common name missing | `null` | `Not available` |
| Establishment data missing | `null` | `Not available` |
| No VBA match | `NOT_FOUND` | Approved no-record message |
| No Advisory exact match | `NOT_ASSESSED` | `Not Assessed` |
| Source failed | `UNAVAILABLE` + warning | Indicates the check that was not completed |
| Invalid plantId | HTTP 404 | Plant not found |
| Backend failure | HTTP 500 | Retry / generic error |

---

# 17. HTTP Status

| HTTP | Purpose |
|---|---|
| 200 | Search / assessment success, including empty search results |
| 400 | Invalid request |
| 404 | plantId not found |
| 500 | Unexpected backend error |
| 503 | Critical data/service unavailable (if used) |

---

# 18. Frontend State

Recommended Pinia stores:

```text
searchStore
- query
- suggestions
- searchResults
- isSearching
- searchError

assessmentStore
- selectedPlantId
- assessment
- isLoading
- error
```

---

# 19. Latest US / AC Traceability

| US | Acceptance Criteria | Technical Coverage |
|---|---|---|
| US 1.1 Search for a Plant | AC 1.1.1–1.1.5 | Home Search + Search API + Router |
| US 1.2 Autocomplete While Typing | AC 1.2.1–1.2.3 | Autocomplete + 3-char trigger + ≤1s target |
| US 1.3 Plant Identity | Identity display / missing handling | `plant` response object |
| US 1.4 Degree of Establishment | Establishment display / missing handling | `degreeOfEstablishment` |
| US 2.1 Local Occurrence | VBA match / count / latest year / no-record handling | `localOccurrence` |
| US 2.2 Environmental Weed Risk | AC 2.2.1–2.2.4 | `environmentalRisk` |
| US 3.1 Combined Recommendation | AC 3.1.1–3.1.3 | Backend `RecommendationService` |
| US 3.2 Understand Recommendation | AC 3.2.1–3.2.3 | Explanation + supporting sections |

---

# 20. Definition of Done

## Search

- [ ] Search field is visible on the Home screen
- [ ] Supports common name
- [ ] Supports scientific name
- [ ] A valid common-name query returns one or more matching plant results when matching data exists
- [ ] A valid scientific-name query returns one or more matching plant results when matching data exists
- [ ] Matching result displays `scientificName` and displays `commonName` where available
- [ ] Search results are selectable
- [ ] Multiple distinct matches are not auto-selected
- [ ] No results displays `No matches found`

## Autocomplete

- [ ] Triggered after entering 3 or more characters
- [ ] Suggestions are displayed within 1 second under normal conditions
- [ ] A suggestion can directly open the assessment page
- [ ] No suggestions displays `No matches found`

## Identity / Establishment

- [ ] Scientific name is visible
- [ ] Common name is visible or displays `Not available`
- [ ] Native / Introduced status is visible
- [ ] Degree of establishment is visible or displays `Not available`

## Local Occurrence

- [ ] VBA record count is visible
- [ ] Latest record year is visible
- [ ] Zero records uses the approved wording
- [ ] Zero records is not interpreted as species absence
- [ ] VBA source is visible

## Environmental Risk

- [ ] Exact match displays the Advisory List risk rating
- [ ] No exact match displays `Not Assessed`
- [ ] `Not Assessed` is not treated as Low Risk / Safe
- [ ] Rating includes a one-sentence explanation
- [ ] 2022 Advisory List source is visible

## Recommendation

- [ ] Exactly one verdict
- [ ] Very High / High → Reconsider Planting
- [ ] Moderately High / Medium → Use Caution
- [ ] Validated lower-concern category → Lower Concern
- [ ] No exact Advisory List assessment → Not Assessed
- [ ] VBA count does not independently change verdict severity
- [ ] Partial data failure is explicitly explained

## Explanation

- [ ] Explanation references the evidence that is actually available
- [ ] Uses natural language
- [ ] Does not display raw database fields
- [ ] Does not display internal enum values
- [ ] Does not display dataset column names
- [ ] Identity / Establishment / Occurrence / Risk sections remain visible

---

# 21. Final Iteration 1 Frontend/Backend Baseline

## Frontend Pages

```text
1. Home / Plant Search
2. Plant Assessment Result
```

## Backend API

```http
GET /api/v1/plants/search?q={keyword}
GET /api/v1/plants/{plantId}/assessment
```

## Architecture

```text
Vue 3 + Vuetify
       ↓
Spring Boot REST API
       ↓
Application Data Layer
   ├─ VicFlora
   ├─ VBA Flora Records
   └─ 2022 Advisory List
       ↓
Recommendation Rule Engine
       ↓
Frontend-ready Assessment JSON
```

> **Iteration 1 technical baseline: 2 core screens + 2 primary APIs, aligned with the revised USs and ACs.**
