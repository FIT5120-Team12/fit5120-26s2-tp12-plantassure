package com.plantky.domain.vo.catalog;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.OriginStatus;
import lombok.Builder;
import lombok.Getter;

/** Catalog 单个 assessed plant 卡片所需字段。 */
@Getter
@Builder
public class PlantCatalogItemVO {
    private final Long plantId;
    private final String commonName;
    private final String scientificName;
    private final String imageUrl;
    private final EnvironmentalConcern environmentalConcern;
    private final OriginStatus originStatus;
    private final String growthForm;
    private final String lifeHistory;
    private final String woodiness;
    private final String height;
}
