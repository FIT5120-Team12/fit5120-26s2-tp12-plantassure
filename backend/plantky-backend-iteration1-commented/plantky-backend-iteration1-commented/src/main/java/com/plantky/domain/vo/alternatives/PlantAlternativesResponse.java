package com.plantky.domain.vo.alternatives;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * GET /api/v1/plants/{plantId}/alternatives 成功响应。
 *
 * <p>status 是对数据状态的透明说明，并不替代 alternatives 数组：</p>
 * <ul>
 *     <li>matched</li>
 *     <li>no_strict_match_found</li>
 *     <li>insufficient_trait_data</li>
 *     <li>not_applicable</li>
 * </ul>
 */
@Getter
@Builder
public class PlantAlternativesResponse {
    private final String status;
    private final CurrentPlantVO currentPlant;
    private final List<AlternativePlantVO> alternatives;
}
