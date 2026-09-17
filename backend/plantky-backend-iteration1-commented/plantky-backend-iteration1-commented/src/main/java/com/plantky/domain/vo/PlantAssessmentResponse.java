package com.plantky.domain.vo;

import java.util.List;

import com.plantky.common.enums.LegalStatus;
import com.plantky.common.enums.OriginStatus;
import com.plantky.domain.vo.assessment.EnvironmentalConcernVO;
import com.plantky.domain.vo.assessment.GriisSupplementaryEvidenceVO;
import com.plantky.domain.vo.assessment.LegalStatusDetailVO;
import com.plantky.domain.vo.assessment.PlantTraitsVO;
import com.plantky.domain.vo.assessment.SupportingEvidenceVO;
import com.plantky.domain.vo.assessment.VictorianEstablishmentVO;
import lombok.Builder;
import lombok.Getter;

/**
 * GET /api/v1/plants/{plantId}/assessment 的完整响应。
 *
 * <p>Iteration 2 使用“向后兼容扩展”策略：</p>
 * <ul>
 *     <li>I1 的 plant/localOccurrence/environmentalRisk/recommendation/sources/warnings 全部保留；</li>
 *     <li>I2 在同一响应中增加 normalized origin/concern、traits、legal availability、GRIIS 和 supporting evidence；</li>
 *     <li>不把 occurrence/GRIIS 当成 environmental concern。</li>
 * </ul>
 */
@Getter
@Builder
public class PlantAssessmentResponse {

    // ---------------------------------------------------------------------
    // Iteration 1 fields — kept unchanged for existing frontend compatibility
    // ---------------------------------------------------------------------

    private final PlantIdentityVO plant;
    private final LocalOccurrenceVO localOccurrence;
    private final EnvironmentalRiskVO environmentalRisk;
    private final RecommendationVO recommendation;
    private final List<DataSourceVO> sources;
    private final List<String> warnings;

    // ---------------------------------------------------------------------
    // Iteration 2 additive fields
    // ---------------------------------------------------------------------

    /** Stable enum view of VicFlora origin status. */
    private final OriginStatus originStatus;

    /** VicFlora degree-of-establishment block for the new contract. */
    private final VictorianEstablishmentVO victorianEstablishment;

    /** Normalized environmental concern kept separate from local occurrence evidence. */
    private final EnvironmentalConcernVO environmentalConcern;

    /** Current legal state. With the supplied I2 dataset this is UNAVAILABLE. */
    private final LegalStatus legalStatus;

    /** Additional explanation for why legalStatus is unavailable. */
    private final LegalStatusDetailVO legalStatusDetail;

    /** AusTraits-backed trait block. Missing values remain null. */
    private final PlantTraitsVO traits;

    /** Only populated for VicFlora INTRODUCED plants; otherwise null. */
    private final GriisSupplementaryEvidenceVO griisSupplementaryEvidence;

    /** Explicit VBA100 + ALA evidence block matching the I2 data-source changes. */
    private final SupportingEvidenceVO supportingEvidence;
}
