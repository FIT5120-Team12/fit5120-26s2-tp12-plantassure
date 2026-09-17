package com.plantky.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/** Search API 单条植物结果。 */
@Getter
@Builder
public class PlantSearchItemVO {

    @Schema(example = "3")
    private final Long plantId;

    @Schema(example = "Acacia baileyana")
    private final String scientificName;

    @Schema(example = "Cootamundra Wattle", nullable = true)
    private final String commonName;

    /** Iteration 2 contract 补充字段；缺失时为 null。 */
    private final String family;

    /** 当前数据集没有稳定图片 URL，先返回 null 供前端 fallback。 */
    private final String imageUrl;
}
