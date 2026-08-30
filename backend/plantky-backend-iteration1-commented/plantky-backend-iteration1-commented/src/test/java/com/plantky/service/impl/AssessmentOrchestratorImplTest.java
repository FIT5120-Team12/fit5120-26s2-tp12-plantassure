package com.plantky.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.plantky.common.enums.RecommendationLevel;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.PlantAssessmentResponse;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.component.OccurrenceService;
import com.plantky.service.component.PlantIdentityService;
import com.plantky.service.component.RecommendationService;
import com.plantky.service.component.RiskAssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AssessmentOrchestratorImpl 流程级单元测试。
 *
 * <p>Mapper 使用 Mockito 模拟，但四个业务组件使用真实实现，
 * 因此可以验证“数据库实体 -> 各业务组件 -> 最终 Response”的完整编排逻辑。</p>
 */
@ExtendWith(MockitoExtension.class)
class AssessmentOrchestratorImplTest {

    /** 不连接真实 MySQL，使用 Mockito 模拟数据库层。 */
    @Mock
    private SpeciesDataMapper speciesDataMapper;

    private AssessmentOrchestratorImpl orchestrator;

    /** 每个测试前创建新的 Orchestrator。 */
    @BeforeEach
    void setUp() {
        orchestrator = new AssessmentOrchestratorImpl(
                speciesDataMapper,
                new PlantIdentityService(),
                new OccurrenceService(),
                new RiskAssessmentService(),
                new RecommendationService());
    }

    /**
     * 使用真实数据样例 Acacia baileyana 验证完整 Assessment 的关键字段。
     */
    @Test
    void shouldBuildCompleteAssessmentForAcaciaBaileyana() {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setId(3L);
        entity.setScientificName("Acacia baileyana");
        entity.setVernacularName("Cootamundra Wattle");
        entity.setFamily("Fabaceae");
        entity.setEstablishmentMeans("introduced");
        entity.setDegreeOfEstablishment("naturalised");
        entity.setRiskRating("Moderately High Risk");
        entity.setVbaRecordCount(1);
        entity.setVbaMostRecentYear(2020);

        // 指定 Mapper 在 selectById(3) 时返回上面的测试 Entity。
        when(speciesDataMapper.selectById(3L)).thenReturn(entity);

        PlantAssessmentResponse result = orchestrator.assess(3L);

        // 验证各 section 都被正确构建。
        assertThat(result.getPlant().getScientificName()).isEqualTo("Acacia baileyana");
        assertThat(result.getLocalOccurrence().getRecordCount()).isEqualTo(1);
        assertThat(result.getEnvironmentalRisk().getRating()).isEqualTo("Moderately High");
        assertThat(result.getRecommendation().getLevel()).isEqualTo(RecommendationLevel.USE_CAUTION);
        assertThat(result.getSources()).hasSize(3);
        assertThat(result.getWarnings()).isEmpty();
    }
}
