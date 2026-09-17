package com.plantky.service.component;

import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.assessment.SupportingEvidenceVO;
import org.springframework.stereotype.Service;

/** Iteration 2 VBA100 + ALA supporting evidence 映射组件。 */
@Service
public class SupportingEvidenceService {

    public SupportingEvidenceVO build(SpeciesDataEntity entity) {
        return SupportingEvidenceVO.builder()
                .vba100RecordCount(entity.getVba100RecordCount())
                .vba100MostRecentYear(entity.getVba100MostRecentYear())
                .alaRecordCount(entity.getAlaRecordCount())
                .alaMostRecentDate(entity.getAlaMostRecentDate())
                .build();
    }
}
