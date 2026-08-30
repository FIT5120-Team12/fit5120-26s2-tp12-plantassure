package com.plantky.service;

import com.plantky.domain.vo.PlantAssessmentResponse;

/**
 * Plant Assessment 应用层编排接口。
 *
 * <p>“Orchestrator”表示该服务负责协调多个业务组件，而不是把所有规则写在一个大方法中。</p>
 *
 * <pre>
 * AssessmentOrchestrator
 * ├── PlantIdentityService
 * ├── OccurrenceService
 * ├── RiskAssessmentService
 * └── RecommendationService
 * </pre>
 */
public interface AssessmentOrchestrator {

    /**
     * 根据 plantId 构建完整 Iteration 1 Assessment。
     *
     * @param plantId species_data 主键
     * @return 完整 assessment API 响应
     * @throws com.plantky.common.exception.PlantNotFoundException plantId 不存在时抛出
     */
    PlantAssessmentResponse assess(Long plantId);
}
