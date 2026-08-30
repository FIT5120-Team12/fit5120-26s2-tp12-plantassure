# Iteration 1 前端与 API 技术规范（US/AC 修订同步版）

## 项目：Plantky / Check Before You Plant — City of Monash

**面向对象：** 前端开发 / 后端开发 / 测试  
**范围：** Iteration 1  
**状态：** 已根据最新修订后的 User Stories 与 Acceptance Criteria 更新（包含教师对 US 1.1 正向搜索验收的反馈）  
**核心页面：** 2 个  
**核心 API：** 2 个

---

# 1. 文档目的

本文档用于将 Iteration 1 的最新 User Stories（US）和 Acceptance Criteria（AC）转换为可实施、可测试的前后端技术要求。

Iteration 1 的核心用户流程为：

```text
Home / Plant Search
   ↓
Search / Autocomplete
   ↓
用户选择一个明确的 species
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

# 2. Iteration 1 功能边界

## 2.1 本 Iteration 包含

- Home 页面上的 Plant Search
- Common name / scientific name search
- Autocomplete
- 多匹配 species 选择
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

## 2.2 本 Iteration 不包含

- 用户 postcode / address
- 动态定位
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

# 3. 页面结构

Iteration 1 只需要两个核心页面。

## 3.1 Screen A — Home / Plant Search

### 目的

满足：

- US 1.1 — Search for a Plant
- US 1.2 — Autocomplete While Typing

### 页面要求

Home screen 加载完成后，plant search field 必须直接可见并可输入。

### UI 组件

| UI 元素 | 技术要求 |
|---|---|
| Search field | 支持 common name / scientific name |
| Search submit action | 用户可主动提交关键词 |
| Autocomplete dropdown | 输入至少 3 个字符后显示 suggestions |
| Search results list | 显示匹配 species |
| Multiple-match state | 多个 species 时全部展示并允许用户选择 |
| No-match state | 统一显示 `No matches found` |
| Loading state | 搜索请求期间可见 |
| Error state | API 错误时显示可恢复提示 |

### 建议 Route

```text
/
```

或：

```text
/home
```

不再要求单独的 `/plants/search` 页面。

---

## 3.2 Screen B — Plant Assessment Result

### 目的

满足：

- US 1.3 — Plant Identity
- US 1.4 — Degree of Establishment
- US 2.1 — Local Occurrence
- US 2.2 — Environmental Weed Risk
- US 3.1 — Combined Planting Recommendation
- US 3.2 — Understand the Recommendation

### 建议 Route

```text
/plants/:plantId/assessment
```

### 页面必须包含

| 区域 | 内容 |
|---|---|
| Verdict Summary | Lower Concern / Use Caution / Reconsider Planting / Not Assessed |
| Plain-Language Explanation | 对 verdict 的自然语言解释 |
| Identity | Scientific name / common name |
| Establishment | Native / Introduced / degree of establishment |
| Local Occurrence | VBA matching record count / latest year / no-record state |
| Environmental Risk | Assessed / Not Assessed / rating / explanation |
| Data Sources | VicFlora / VBA / 2022 Advisory List |
| Warning Area | partial data failure / unavailable source |

Supporting evidence 必须继续保留在 verdict 下方，不需要独立第三页。

---

# 4. API 总览

| Method | Endpoint | 用途 |
|---|---|---|
| GET | `/api/v1/plants/search?q={keyword}` | Search + autocomplete |
| GET | `/api/v1/plants/{plantId}/assessment` | 返回完整 plant assessment |

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

Frontend 必须显示：

```text
No matches found
```

不得显示空白区域。

## 5.4 Valid Search Behaviour

对于有效的 common name 或 scientific name：

- 如果数据库中存在匹配植物，Search API 必须返回一个或多个 matching results。
- 每个 result 至少应包含 `plantId`、`scientificName` 和 `commonName`（如可用）。
- Valid search 的正向路径是 US 1.1 的独立验收要求，不应只通过“有结果时可以点击”间接验证。

示例：

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

# 6. US 1.1 — Search for a Plant 技术映射

## User Story

> As a residential gardener in the City of Monash, I want to search for a plant by its common or scientific name, so that I can quickly find information about it before deciding whether to plant it.

## AC 1.1.1 — Plant search is available

Given the app has loaded,  
When the tester views the home screen,  
Then a plant search field is visible and ready for input.

### 技术要求

- Search field 必须出现在 Home screen。
- 页面加载完成后无需额外导航即可输入。
- Search input 不得默认 disabled。

## AC 1.1.2 — Valid search returns matching results

Given the plant search field is available,  
When the tester enters a valid common name or scientific name and submits the search,  
Then the system displays one or more matching plant results containing the relevant scientific name and common name where available.

### 技术要求

- Search API 必须同时支持 valid common name 和 valid scientific name 查询。
- 当数据库中存在匹配 species 时，response 必须返回一个或多个 matching results。
- 每个 search result 至少返回：
  - `plantId`
  - `scientificName`
  - `commonName`（如可用）
- 前端必须将 matching results 显示给用户，不得把有效查询错误显示为 `No matches found`。

成功返回示例：

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

### 技术要求

后端 search response 必须返回 `plantId`。前端选择结果后：

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

### 技术要求

- 空 search result 使用 HTTP 200。
- Response 使用 `results: []`。
- UI wording 固定为 `No matches found`。

## AC 1.1.5 — Ambiguous matches are resolved

Given a term matching multiple distinct species has been entered,  
When the tester submits the search,  
Then the system displays a selectable list rather than auto-selecting a species.

### 技术要求

- Backend 必须返回多个 distinct taxa。
- Frontend 不得自动选择第一项。
- 不得将多个 taxa 静默合并。

---

# 7. US 1.2 — Autocomplete While Typing 技术映射

## User Story

> As a residential gardener in the City of Monash, I want to see autocomplete suggestions as I type a plant name, so that I can find the correct plant faster even if I'm unsure of the exact spelling.

## AC 1.2.1 — Suggestions appear while typing

Given the search field is open,  
When the tester types 3 or more characters,  
Then a dropdown of matching species names appears within 1 second.

### 技术要求

- Minimum autocomplete trigger length：`3 characters`
- 建议前端 debounce：`200–300 ms`
- 正常运行环境下 suggestion dropdown 应在 1 秒内显示

## AC 1.2.2 — Suggestion can be selected

Given the autocomplete dropdown is visible,  
When the tester selects a suggestion,  
Then the app navigates to that plant's assessment page.

### 技术要求

Autocomplete item 必须包含 `plantId`。选择 suggestion 后直接导航：

```text
/plants/{plantId}/assessment
```

不需要再次执行第二次 search。

## AC 1.2.3 — No matches while typing

Given the search field is open,  
When the tester types 3 or more characters that match no species name,  
Then the dropdown displays `No matches found` rather than remaining empty.

### 技术要求

Autocomplete dropdown 空结果时仍保持可见，并显示 `No matches found`。

---

# 8. Plant Identity / Establishment 数据结构

Assessment API 建议返回：

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

前端规则：

- `scientificName`：必须显示
- `commonName = null`：显示 `Not available`
- `establishmentMeans = null`：显示 `Not available`
- `degreeOfEstablishment = null`：显示 `Not available`
- 不显示 raw VicFlora column names

---

# 9. US 2.1 — Local Occurrence 技术要求

VBA 的角色固定为：

> City of Monash local occurrence evidence

不是 environmental risk score。

建议 response：

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

支持：

```text
FOUND
NOT_FOUND
UNAVAILABLE
```

当无匹配 VBA records：

```text
No matching VBA records were found in the City of Monash.
```

禁止显示：

```text
This plant does not occur in Monash.
```

因为：

```text
absence of records ≠ evidence of absence
```

---

# 10. US 2.2 — Check Environmental Weed Risk 技术映射

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

支持：

```text
ASSESSED
NOT_ASSESSED
UNAVAILABLE
```

## AC 2.2.1 — Risk rating is displayed when assessed

如果 selected plant 在 Advisory List 中有 exact match，Frontend 显示 dataset 中记录的 environmental weed risk rating。Backend 不自行生成新的 risk rating。

## AC 2.2.2 — Not Assessed is stated clearly

无 exact match 时：

```json
{
  "assessmentStatus": "NOT_ASSESSED",
  "rating": null
}
```

Frontend 显示：

```text
Not Assessed
```

并说明：

```text
No exact matching assessment was found in the 2022 Advisory List.
```

禁止将其解释为 `Low Risk` 或 `Safe`。

## AC 2.2.3 — Rating includes a plain-language explanation

Risk section 必须同时包含：

- rating
- one-sentence plain-language explanation

不得只显示 unexplained technical value / label。

## AC 2.2.4 — Data source is attributed

统一 source 名称：

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

Assessment process 完成后必须返回 exactly one：

```text
Lower Concern
Use Caution
Reconsider Planting
Not Assessed
```

Frontend 使用 `recommendation.displayLabel`，不得自行重新计算 verdict。

## AC 3.1.2 — Verdict logic reflects validated environmental risk evidence

| Environmental Risk | Verdict |
|---|---|
| Very High | Reconsider Planting |
| High | Reconsider Planting |
| Moderately High | Use Caution |
| Medium | Use Caution |
| Lower / validated lower-concern category | Lower Concern |
| No exact Advisory List assessment | Not Assessed |

核心限制：

- VicFlora establishment status 和 VBA local occurrence 仅作为 supporting/contextual evidence。
- VBA record count 不能独立提高或降低 environmental risk category。
- 禁止使用 `many local records → Reconsider Planting`、`few local records → Use Caution`、`no local records → Lower Concern` 这类规则。

## AC 3.1.3 — Partial data failures are handled transparently

如果某个 evidence source unavailable：

- Backend 在对应 section 返回 `UNAVAILABLE`
- `warnings[]` 指明无法完成的 check
- Frontend 必须显示该 warning
- 不静默填充 missing data
- 不显示误导性 verdict

---

# 13. US 3.2 — Understand the Recommendation

## User Story

> As a residential gardener in the City of Monash, I want a plain-language explanation of why I received the recommendation, so that I can understand and trust the result.

## AC 3.2.1 — Explanation references specific evidence

Explanation 应引用当前实际 available evidence，例如：

```text
This plant is introduced and naturalised in Victoria, has documented occurrence records in the City of Monash, and has a High environmental weed risk rating.
```

应明确区分：

- establishment evidence
- local occurrence evidence
- environmental risk evidence

不得将三种 evidence 表述成同一种 risk metric。

## AC 3.2.2 — Explanation avoids raw technical fields

允许：

```text
This plant is introduced and naturalised in Victoria.
```

禁止：

```text
establishment_means=INTRODUCED
record_count=8
risk_code=H
```

不得暴露：

- raw database field names
- internal enums
- source dataset column names
- unexplained technical scores

## AC 3.2.3 — Supporting data remains visible

Verdict 下方必须继续显示：

```text
Identity
Establishment
Occurrence
Risk
```

这四个 supporting sections 不得因为 summary verdict 出现而被替代或隐藏。

---

# 14. Recommendation Logic — Backend Ownership

Recommendation logic 必须由 Spring Boot backend 执行。

Frontend 不负责：

- 计算 risk
- 组合 verdict
- 根据 VBA count 调整 risk
- 根据 missing data 猜测 verdict

Backend 推荐结构：

```text
PlantIdentityService
OccurrenceService
RiskAssessmentService
RecommendationService
AssessmentOrchestrator
```

---

# 15. Data Matching Rules

1. Primary matching key：accepted scientific name
2. 优先 exact scientific-name match
3. Synonym 仅在 verified accepted-name mapping 存在时使用
4. 不使用 uncontrolled fuzzy match 强行 join
5. Subspecies / variety 没有 exact risk match 时默认 `Not Assessed`
6. 除非有验证过的 parent-taxon rule，不自动继承 parent species risk
7. Missing data 不得自行补全

---

# 16. Missing Data / Error Handling

| 场景 | Backend | Frontend |
|---|---|---|
| Search no match | `results: []` | `No matches found` |
| Common name missing | `null` | `Not available` |
| Establishment data missing | `null` | `Not available` |
| No VBA match | `NOT_FOUND` | Approved no-record message |
| No Advisory exact match | `NOT_ASSESSED` | `Not Assessed` |
| Source failed | `UNAVAILABLE` + warning | 指明未完成 check |
| Invalid plantId | HTTP 404 | Plant not found |
| Backend failure | HTTP 500 | Retry / generic error |

---

# 17. HTTP Status

| HTTP | 用途 |
|---|---|
| 200 | Search / assessment success，包括空搜索结果 |
| 400 | Invalid request |
| 404 | plantId not found |
| 500 | Unexpected backend error |
| 503 | Critical data/service unavailable（如采用） |

---

# 18. Frontend State

建议 Pinia：

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

# 19. 最新 US / AC Traceability

| US | Acceptance Criteria | 技术覆盖 |
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

- [ ] Home screen 上 search field 可见
- [ ] 支持 common name
- [ ] 支持 scientific name
- [ ] Valid common name 查询会返回一个或多个 matching plant results（存在匹配数据时）
- [ ] Valid scientific name 查询会返回一个或多个 matching plant results（存在匹配数据时）
- [ ] Matching result 显示 `scientificName`，并在可用时显示 `commonName`
- [ ] Search results 可选择
- [ ] Multiple distinct matches 不自动选择
- [ ] No results 显示 `No matches found`

## Autocomplete

- [ ] 输入 3 个或以上字符后触发
- [ ] 正常环境下 suggestions 在 1 秒内显示
- [ ] Suggestion 可直接进入 assessment
- [ ] No suggestions 显示 `No matches found`

## Identity / Establishment

- [ ] Scientific name 可见
- [ ] Common name 可见或 `Not available`
- [ ] Native / Introduced 可见
- [ ] Degree of establishment 可见或 `Not available`

## Local Occurrence

- [ ] VBA record count 可见
- [ ] Latest record year 可见
- [ ] Zero records 使用 approved wording
- [ ] Zero records 不被解释为 species absence
- [ ] VBA source 可见

## Environmental Risk

- [ ] Exact match 时显示 Advisory List risk rating
- [ ] No exact match 时显示 `Not Assessed`
- [ ] `Not Assessed` 不等于 Low Risk / Safe
- [ ] Rating 有 one-sentence explanation
- [ ] 2022 Advisory List source 可见

## Recommendation

- [ ] Exactly one verdict
- [ ] Very High / High → Reconsider Planting
- [ ] Moderately High / Medium → Use Caution
- [ ] Validated lower-concern category → Lower Concern
- [ ] No exact Advisory List assessment → Not Assessed
- [ ] VBA count 不独立改变 verdict severity
- [ ] Partial data failure 被明确说明

## Explanation

- [ ] Explanation 引用实际 available evidence
- [ ] 使用自然语言
- [ ] 不显示 raw database fields
- [ ] 不显示 internal enum values
- [ ] 不显示 dataset column names
- [ ] Identity / Establishment / Occurrence / Risk sections 保持可见

---

# 21. Iteration 1 最终前后端基线

## 前端页面

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
