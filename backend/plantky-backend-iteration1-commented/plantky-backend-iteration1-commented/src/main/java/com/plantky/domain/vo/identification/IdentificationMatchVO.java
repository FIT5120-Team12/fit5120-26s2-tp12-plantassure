package com.plantky.domain.vo.identification;

import lombok.Builder;
import lombok.Getter;

/**
 * Epic 1 返回前端的单个 possible species match。
 *
 * <p>identificationConfidence 只表示“图片像这个物种的程度”，绝不能当作环境风险分数。</p>
 */
@Getter
@Builder
public class IdentificationMatchVO {

    /** 匹配到 PlantAssure 数据库时返回稳定 plantId；否则 null。 */
    private final Long plantId;

    /** PlantAssure verified common name；数据库无匹配时为 null。 */
    private final String commonName;

    /** AI provider 返回的 scientific name。 */
    private final String scientificName;

    /** AI 图片识别置信度，0.0–1.0。 */
    private final Double identificationConfidence;

    /** scientificName 是否成功映射到现有 PlantAssure record。 */
    private final boolean plantAssureMatch;
}
