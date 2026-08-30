# Plantky Iteration 1 后端实现说明

## 1. 两个核心请求链路

### 1.1 搜索 / 自动补全

```text
GET /api/v1/plants/search?q=wattle
        ↓
PlantController.searchPlants
        ↓
PlantSearchServiceImpl.search
        ↓
SpeciesDataMapper.selectList
        ↓
MySQL species_data
        ↓
PlantSearchResponse
```

后端同时查询 `scientific_name` 和 `vernacular_name`，返回所有匹配物种。无结果返回 HTTP 200 + `results: []`。

### 1.2 完整植物评估

```text
GET /api/v1/plants/{plantId}/assessment
        ↓
PlantController.getAssessment
        ↓
AssessmentOrchestratorImpl.assess
        ↓
SpeciesDataMapper.selectById      ← 数据库只查询一次
        ↓
├─ PlantIdentityService
├─ OccurrenceService
├─ RiskAssessmentService
└─ RecommendationService
        ↓
PlantAssessmentResponse
```

`AssessmentOrchestratorImpl` 负责组织流程，不把具体业务规则堆在 Controller 中。

## 2. 各模块职责

| 模块 | 职责 |
|---|---|
| controller | HTTP 路径、参数接收、返回响应 |
| service | 用例接口 |
| service/impl | 搜索流程、评估流程编排 |
| service/component | 单一业务规则 |
| mapper | MySQL 数据访问 |
| domain/entity | 数据库实体 |
| domain/vo | 对前端返回的数据结构 |
| common/enums | 稳定状态值、风险值、推荐等级 |
| common/exception | 业务异常 |
| common/handler | 全局异常转换 |
| config | CORS、OpenAPI |

## 3. Recommendation 为什么单独拆 Service

Recommendation 是 Iteration 1 最重要的业务规则，因此不能写在 Controller 或 Vue 中。

规则：

```text
Very High / High   → RECONSIDER_PLANTING
Moderately High    → USE_CAUTION
Medium             → USE_CAUTION
Lower              → LOWER_CONCERN
无 exact assessment → NOT_ASSESSED
```

VBA `recordCount` 不参与等级计算，只进入说明文字。

## 4. 错误处理

所有异常统一经过 `GlobalExceptionHandler`：

```text
InvalidSearchQueryException → 400 INVALID_SEARCH_QUERY
PlantNotFoundException      → 404 PLANT_NOT_FOUND
参数类型/校验错误             → 400 INVALID_REQUEST
其他异常                     → 500 INTERNAL_SERVER_ERROR
```

前端不会收到 SQL、Java 异常类或 stack trace。

## 5. MyBatis-Plus 映射

MySQL：

```text
scientific_name
risk_rating
vba_record_count
```

Java：

```text
scientificName
riskRating
vbaRecordCount
```

`application.yml` 已显式启用 `map-underscore-to-camel-case: true`。

## 6. 当前没有加入的技术

- Redis：777 条只读数据，没有证据表明需要缓存。
- Cookie / Session / JWT：Iteration 1 没有登录。
- 微服务：当前保持一个 Spring Boot 单体。
- 写事务：两个接口都是单次只读查询，没有多表写一致性问题。

后续 Iteration 增加登录、收藏、用户 Profile、实时数据同步等功能后，再根据实际需求增加对应基础设施。
