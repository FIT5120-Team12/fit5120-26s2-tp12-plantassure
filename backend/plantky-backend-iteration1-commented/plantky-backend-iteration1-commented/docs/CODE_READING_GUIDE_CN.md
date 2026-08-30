# Plantky Iteration 1 后端代码阅读指南（详细注释版）

## 1. 推荐阅读顺序

```text
HTTP 请求
  ↓
PlantController
  ↓
PlantSearchService / AssessmentOrchestrator
  ↓
Impl
  ↓
Business Components
  ↓
SpeciesDataMapper
  ↓
SpeciesDataEntity / MySQL
  ↓
VO
  ↓
JSON Response
```

## 2. 两条核心调用链

### Search

```text
GET /api/v1/plants/search?q=wattle
  ↓
PlantController.searchPlants()
  ↓
PlantSearchService.search()
  ↓
PlantSearchServiceImpl.search()
  ↓
LambdaQueryWrapper
  ↓
SpeciesDataMapper.selectList()
  ↓
SpeciesDataEntity -> PlantSearchItemVO
  ↓
PlantSearchResponse
```

### Assessment

```text
GET /api/v1/plants/3/assessment
  ↓
PlantController.getAssessment()
  ↓
AssessmentOrchestrator.assess()
  ↓
AssessmentOrchestratorImpl.assess()
  ↓
SpeciesDataMapper.selectById()
  ↓
PlantIdentityService
OccurrenceService
RiskAssessmentService
  ↓
RecommendationService
  ↓
PlantAssessmentResponse
```

## 3. 学习时重点关注

- `@RestController`：HTTP 接口 Bean。
- `@Service`：业务 Bean。
- `@RequiredArgsConstructor`：为 final 依赖生成构造器，Spring 用它完成依赖注入。
- `@Builder`：为 Response/VO 提供链式对象构造方式。
- `@Mapper + BaseMapper`：MyBatis-Plus 数据访问。
- `Entity`：数据库结构映射。
- `VO`：前端 API 数据结构。
- `Orchestrator`：业务流程编排，不把所有规则塞在一个 Service 中。
- `GlobalExceptionHandler`：异常统一转换成 HTTP Error Response。

## 4. 最重要的 Iteration 1 业务边界

Recommendation severity 只由 2022 Advisory List 中验证过的 environmental risk rating 决定。
VicFlora establishment 和 VBA local occurrence 是 supporting/contextual evidence，不能根据 VBA record count 改变 verdict。

```text
Very High / High       -> Reconsider Planting
Moderately High/Medium -> Use Caution
Lower                  -> Lower Concern
No exact assessment    -> Not Assessed
```
