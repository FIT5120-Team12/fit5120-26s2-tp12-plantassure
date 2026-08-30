package com.plantky.domain.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * Assessment API 中 Plant Identity + Establishment 区域的数据对象。
 *
 * <p>该对象由 PlantIdentityService 从 SpeciesDataEntity 转换得到。
 * Entity 使用数据库原始格式，VO 使用对 API 更友好的字段名和展示值。</p>
 */
@Getter
@Builder
public class PlantIdentityVO {

    /** 数据库主键，对外作为 plantId。 */
    private final Long plantId;

    /** Scientific name，当前 Assessment 页面要求必须显示。 */
    private final String scientificName;

    /** Common name；数据缺失时允许为 null，由前端显示 Not available。 */
    private final String commonName;

    /** Family name。 */
    private final String family;

    /** Establishment means，例如 Introduced / Native。 */
    private final String establishmentMeans;

    /** Degree of establishment，例如 Naturalised。 */
    private final String degreeOfEstablishment;
}
