package com.plantky.domain.vo;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * GET /api/v1/plants/{plantId}/assessment 的完整成功响应对象。
 *
 * <p>它本身不负责计算任何业务规则，只负责把各业务组件已经生成的结果组合成 API Contract。</p>
 *
 * <pre>
 * PlantAssessmentResponse
 * ├── plant
 * ├── localOccurrence
 * ├── environmentalRisk
 * ├── recommendation
 * ├── sources
 * └── warnings
 * </pre>
 */
@Getter
@Builder
public class PlantAssessmentResponse {

    /** 植物身份与 establishment 信息。 */
    private final PlantIdentityVO plant;

    /** City of Monash VBA 本地记录信息。 */
    private final LocalOccurrenceVO localOccurrence;

    /** 2022 Advisory List 环境杂草风险。 */
    private final EnvironmentalRiskVO environmentalRisk;

    /** 后端计算得到的唯一 recommendation。 */
    private final RecommendationVO recommendation;

    /** 本次 assessment 使用的数据来源及其角色。 */
    private final List<DataSourceVO> sources;

    /**
     * Partial data failure 等非致命问题。
     * 即使存在 warning，其他可用数据仍可正常返回，前端必须把 warning 展示给用户。
     */
    private final List<String> warnings;
}
