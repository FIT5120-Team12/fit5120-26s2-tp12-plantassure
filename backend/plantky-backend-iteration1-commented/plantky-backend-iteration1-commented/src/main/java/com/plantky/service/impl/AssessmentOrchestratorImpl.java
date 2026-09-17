package com.plantky.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.plantky.common.constant.DataSourceConstants;
import com.plantky.common.exception.PlantNotFoundException;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.DataSourceVO;
import com.plantky.domain.vo.EnvironmentalRiskVO;
import com.plantky.domain.vo.LocalOccurrenceVO;
import com.plantky.domain.vo.PlantAssessmentResponse;
import com.plantky.domain.vo.PlantIdentityVO;
import com.plantky.domain.vo.RecommendationVO;
import com.plantky.domain.vo.assessment.EnvironmentalConcernVO;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.AssessmentOrchestrator;
import com.plantky.service.component.GriisEvidenceService;
import com.plantky.service.component.LegalStatusService;
import com.plantky.service.component.OccurrenceService;
import com.plantky.service.component.PlantClassificationService;
import com.plantky.service.component.PlantIdentityService;
import com.plantky.service.component.RecommendationService;
import com.plantky.service.component.RiskAssessmentService;
import com.plantky.service.component.SupportingEvidenceService;
import com.plantky.service.component.TraitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AssessmentOrchestrator 的 Iteration 2 实现。
 *
 * <p>仍然只查询数据库一次，然后把同一个 SpeciesDataEntity 分发给单一职责组件。
 * I2 只是增加 traits/legal/GRIIS/ALA 等 section，不改变 I1 recommendation 的所有权。</p>
 */
@Service
@RequiredArgsConstructor
public class AssessmentOrchestratorImpl implements AssessmentOrchestrator {

    private final SpeciesDataMapper speciesDataMapper;
    private final PlantIdentityService plantIdentityService;
    private final OccurrenceService occurrenceService;
    private final RiskAssessmentService riskAssessmentService;
    private final RecommendationService recommendationService;

    /** Iteration 2 新增：统一 origin/environmental concern/establishment 映射。 */
    private final PlantClassificationService plantClassificationService;

    /** Iteration 2 新增：AusTraits。 */
    private final TraitService traitService;

    /** Iteration 2 新增：当前数据缺失 legal classification，明确返回 UNAVAILABLE。 */
    private final LegalStatusService legalStatusService;

    /** Iteration 2 新增：GRIIS supplementary evidence。 */
    private final GriisEvidenceService griisEvidenceService;

    /** Iteration 2 新增：VBA100 + ALA 原始 supporting evidence 摘要。 */
    private final SupportingEvidenceService supportingEvidenceService;

    @Override
    public PlantAssessmentResponse assess(Long plantId) {
        SpeciesDataEntity entity = speciesDataMapper.selectById(plantId);
        if (entity == null) {
            throw new PlantNotFoundException();
        }

        List<String> warnings = new ArrayList<>();

        PlantIdentityVO plant = plantIdentityService.build(entity);
        LocalOccurrenceVO localOccurrence = occurrenceService.build(entity, warnings);
        EnvironmentalRiskVO environmentalRisk = riskAssessmentService.build(entity, warnings);
        RecommendationVO recommendation = recommendationService.build(
                plant,
                localOccurrence,
                environmentalRisk);

        /*
         * Iteration 1 原始 response builder：
         *
         * return PlantAssessmentResponse.builder()
         *         .plant(plant)
         *         .localOccurrence(localOccurrence)
         *         .environmentalRisk(environmentalRisk)
         *         .recommendation(recommendation)
         *         .sources(buildSources())
         *         .warnings(List.copyOf(warnings))
         *         .build();
         *
         * Iteration 2 不删除这些字段，而是在下面继续追加新 section，保证现有前端可继续运行。
         */
        return PlantAssessmentResponse.builder()
                .plant(plant)
                .localOccurrence(localOccurrence)
                .environmentalRisk(environmentalRisk)
                .recommendation(recommendation)
                .sources(buildSources())
                .warnings(List.copyOf(warnings))
                .originStatus(plantClassificationService.resolveOriginStatus(entity))
                .victorianEstablishment(plantClassificationService.buildVictorianEstablishment(entity))
                .environmentalConcern(EnvironmentalConcernVO.builder()
                        .status(plantClassificationService.resolveEnvironmentalConcern(entity))
                        .source(DataSourceConstants.ADVISORY_LIST)
                        .build())
                .legalStatus(legalStatusService.resolve(entity))
                .legalStatusDetail(legalStatusService.buildDetail(entity))
                .traits(traitService.build(entity))
                .griisSupplementaryEvidence(griisEvidenceService.build(entity))
                .supportingEvidence(supportingEvidenceService.build(entity))
                .build();
    }

    /**
     * I2 更新后的 data source 列表。
     *
     * <p>VBA/ALA/AusTraits/GRIIS 的角色被明确拆开，防止前端把 occurrence 或 GRIIS
     * supplementary evidence 当作 environmental concern。</p>
     */
    private List<DataSourceVO> buildSources() {
        return List.of(
                DataSourceVO.builder()
                        .name(DataSourceConstants.VICFLORA)
                        .role(DataSourceConstants.VICFLORA_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.ADVISORY_LIST)
                        .role(DataSourceConstants.ADVISORY_LIST_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.VBA)
                        .role(DataSourceConstants.VBA_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.ALA)
                        .role(DataSourceConstants.ALA_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.AUSTRAITS)
                        .role(DataSourceConstants.AUSTRAITS_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.GRIIS)
                        .role(DataSourceConstants.GRIIS_ROLE)
                        .build());
    }
}
