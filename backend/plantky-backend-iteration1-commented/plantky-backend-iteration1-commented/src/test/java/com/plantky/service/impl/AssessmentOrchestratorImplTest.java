package com.plantky.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.LegalStatus;
import com.plantky.common.enums.RecommendationLevel;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.PlantAssessmentResponse;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.component.GriisEvidenceService;
import com.plantky.service.component.LegalStatusService;
import com.plantky.service.component.OccurrenceService;
import com.plantky.service.component.PlantClassificationService;
import com.plantky.service.component.PlantIdentityService;
import com.plantky.service.component.RecommendationService;
import com.plantky.service.component.RiskAssessmentService;
import com.plantky.service.component.SupportingEvidenceService;
import com.plantky.service.component.TraitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** AssessmentOrchestratorImpl I1 + I2 兼容流程测试。 */
@ExtendWith(MockitoExtension.class)
class AssessmentOrchestratorImplTest {

    @Mock
    private SpeciesDataMapper speciesDataMapper;

    private AssessmentOrchestratorImpl orchestrator;

    @BeforeEach
    void setUp() {
        PlantClassificationService classificationService = new PlantClassificationService();
        orchestrator = new AssessmentOrchestratorImpl(
                speciesDataMapper,
                new PlantIdentityService(),
                new OccurrenceService(),
                new RiskAssessmentService(),
                new RecommendationService(),
                classificationService,
                new TraitService(),
                new LegalStatusService(),
                new GriisEvidenceService(classificationService),
                new SupportingEvidenceService());
    }

    @Test
    void shouldBuildBackwardCompatibleAssessmentWithIteration2Fields() {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setId(3L);
        entity.setScientificName("Acacia baileyana");
        entity.setVernacularName("Cootamundra Wattle");
        entity.setFamily("Fabaceae");
        entity.setEstablishmentMeans("introduced");
        entity.setDegreeOfEstablishment("naturalised");
        entity.setRiskRating("Moderately High Risk");
        entity.setVba100RecordCount(2);
        entity.setVba100MostRecentYear(2000);
        entity.setAlaRecordCount(12);
        entity.setGrowthForm("shrub tree");
        entity.setWoodiness("woody");
        entity.setLifeHistory("perennial");
        entity.setHeightMin(3.0);
        entity.setHeightMax(10.0);
        entity.setGriisListed(true);
        entity.setGriisIsInvasive(false);

        when(speciesDataMapper.selectById(3L)).thenReturn(entity);

        PlantAssessmentResponse result = orchestrator.assess(3L);

        // I1 fields remain available.
        assertThat(result.getPlant().getScientificName()).isEqualTo("Acacia baileyana");
        assertThat(result.getLocalOccurrence().getRecordCount()).isEqualTo(2);
        assertThat(result.getEnvironmentalRisk().getRating()).isEqualTo("Moderately High");
        assertThat(result.getRecommendation().getLevel()).isEqualTo(RecommendationLevel.USE_CAUTION);

        // I2 additive fields are present and derived from verified sources.
        assertThat(result.getEnvironmentalConcern().getStatus())
                .isEqualTo(EnvironmentalConcern.MODERATELY_HIGH);
        assertThat(result.getLegalStatus()).isEqualTo(LegalStatus.UNAVAILABLE);
        assertThat(result.getTraits().getGrowthForm()).isEqualTo("shrub tree");
        assertThat(result.getTraits().getHeight()).isEqualTo("3–10 m");
        assertThat(result.getGriisSupplementaryEvidence()).isNotNull();
        assertThat(result.getSupportingEvidence().getAlaRecordCount()).isEqualTo(12);
        assertThat(result.getSources()).hasSize(6);
        assertThat(result.getWarnings()).isEmpty();
    }
}
