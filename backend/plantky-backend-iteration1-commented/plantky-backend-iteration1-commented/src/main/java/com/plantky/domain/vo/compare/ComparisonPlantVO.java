package com.plantky.domain.vo.compare;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.LegalStatus;
import com.plantky.common.enums.OriginStatus;
import com.plantky.domain.vo.LocalOccurrenceVO;
import lombok.Builder;
import lombok.Getter;

/** Compare 表格中一列植物的固定字段。 */
@Getter
@Builder
public class ComparisonPlantVO {
    private final Long plantId;
    private final String commonName;
    private final String scientificName;
    private final String imageUrl;
    private final EnvironmentalConcern environmentalConcern;
    private final LegalStatus legalStatus;
    private final OriginStatus originStatus;
    private final String growthForm;
    private final String lifeHistory;
    private final String woodiness;
    private final String height;
    private final LocalOccurrenceVO localOccurrence;
}
