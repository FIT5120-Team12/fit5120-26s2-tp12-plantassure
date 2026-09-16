package com.plantky.service;

import com.plantky.domain.vo.PlantAssessmentResponse;

/**
 * Plant Assessment 应用层编排接口。
 *
 * <p>Iteration 2 继续沿用 Orchestrator 模式：一个请求只查一次植物记录，
 * 然后协调 identity、occurrence、risk、recommendation、traits、legal availability、
 * GRIIS 与 supporting evidence 组件。</p>
 */
public interface AssessmentOrchestrator {

    /**
     * 根据稳定 plantId 构建完整 Assessment。
     *
     * @param plantId species_data 主键 / API plantId
     * @return I1 兼容 + I2 扩展的 assessment response
     */
    PlantAssessmentResponse assess(Long plantId);
}
