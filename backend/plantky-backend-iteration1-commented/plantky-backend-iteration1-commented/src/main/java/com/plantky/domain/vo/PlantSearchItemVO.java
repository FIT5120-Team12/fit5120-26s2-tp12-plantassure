package com.plantky.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * Search API results 数组中的单条植物结果。
 *
 * <p>只返回前端搜索列表真正需要的三个字段，避免把完整 SpeciesDataEntity 暴露出去。</p>
 */
@Getter
@Builder
public class PlantSearchItemVO {

    /**
     * 植物唯一 ID。
     * 前端选择搜索结果后，用该 ID 访问 /plants/{plantId}/assessment。
     */
    @Schema(example = "3")
    private final Long plantId;

    /** Scientific name。 */
    @Schema(example = "Acacia baileyana")
    private final String scientificName;

    /** Common name；部分植物没有 common name，因此允许为 null。 */
    @Schema(example = "Cootamundra Wattle", nullable = true)
    private final String commonName;
}
