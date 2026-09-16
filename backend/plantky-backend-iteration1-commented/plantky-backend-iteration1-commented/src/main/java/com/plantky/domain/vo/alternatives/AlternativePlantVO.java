package com.plantky.domain.vo.alternatives;

import java.util.List;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.LegalStatus;
import com.plantky.common.enums.OriginStatus;
import lombok.Builder;
import lombok.Getter;

/** 经后端重新验证后的 lower-concern alternative。 */
@Getter
@Builder
public class AlternativePlantVO {
    private final Long plantId;
    private final String commonName;
    private final String scientificName;
    private final String imageUrl;
    private final EnvironmentalConcern environmentalConcern;
    private final OriginStatus originStatus;
    private final LegalStatus legalStatus;
    private final String growthForm;
    private final String lifeHistory;
    private final String woodiness;
    private final String height;
    private final List<String> matchReasons;
}
