package com.plantky.domain.vo.compare;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** GET /api/v1/plants/compare 的描述性比较响应；不包含 winner 或综合分数。 */
@Getter
@Builder
public class PlantComparisonResponse {
    private final List<ComparisonPlantVO> plants;
}
