package com.plantky.domain.vo.alternatives;

import com.plantky.common.enums.EnvironmentalConcern;
import lombok.Builder;
import lombok.Getter;

/** Find a Better Plant 页面顶部的当前植物摘要。 */
@Getter
@Builder
public class CurrentPlantVO {
    private final Long plantId;
    private final String commonName;
    private final String scientificName;
    private final String imageUrl;
    private final EnvironmentalConcern environmentalConcern;
    private final String growthForm;
    private final String lifeHistory;
    private final String woodiness;
    private final String height;
}
