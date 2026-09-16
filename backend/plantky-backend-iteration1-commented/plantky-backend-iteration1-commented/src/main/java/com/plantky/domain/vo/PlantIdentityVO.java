package com.plantky.domain.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * Assessment API 中 Plant Identity + Establishment 区域的数据对象。
 *
 * <p>Iteration 2 在保持 I1 字段不变的基础上增加 imageUrl。当前数据集没有可靠图片 URL，
 * 因此实际值为 null；前端可以继续使用自己的 fallback。</p>
 */
@Getter
@Builder
public class PlantIdentityVO {

    private final Long plantId;
    private final String scientificName;
    private final String commonName;
    private final String family;
    private final String imageUrl;
    private final String establishmentMeans;
    private final String degreeOfEstablishment;
}
