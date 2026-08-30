package com.plantky.service.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.plantky.common.enums.OccurrenceStatus;
import com.plantky.common.enums.RecommendationLevel;
import com.plantky.common.enums.RiskAssessmentStatus;
import com.plantky.domain.vo.EnvironmentalRiskVO;
import com.plantky.domain.vo.LocalOccurrenceVO;
import com.plantky.domain.vo.PlantIdentityVO;
import com.plantky.domain.vo.RecommendationVO;
import org.junit.jupiter.api.Test;

/**
 * RecommendationService 的核心业务规则测试。
 *
 * <p>重点验证：Recommendation 只由 environmental risk 决定，
 * VBA record count 只能作为 explanation evidence，不能改变 verdict。</p>
 */
class RecommendationServiceTest {

    private final RecommendationService recommendationService = new RecommendationService();

    /** High Risk -> Reconsider Planting。 */
    @Test
    void shouldReturnReconsiderPlantingForHighRisk() {
        RecommendationVO result = recommendationService.build(
                plant(),
                occurrence(1),
                risk("High", RiskAssessmentStatus.ASSESSED));

        assertThat(result.getLevel()).isEqualTo(RecommendationLevel.RECONSIDER_PLANTING);
        assertThat(result.getDisplayLabel()).isEqualTo("Reconsider Planting");
    }

    /** Moderately High -> Use Caution。 */
    @Test
    void shouldReturnUseCautionForModeratelyHighRisk() {
        RecommendationVO result = recommendationService.build(
                plant(),
                occurrence(99),
                risk("Moderately High", RiskAssessmentStatus.ASSESSED));

        assertThat(result.getLevel()).isEqualTo(RecommendationLevel.USE_CAUTION);
    }

    /**
     * 同一个 Medium Risk，在 VBA count=1 和 count=5000 时都必须是 Use Caution。
     * 这直接保护“record count 不参与 risk severity”的 Acceptance Criteria。
     */
    @Test
    void shouldNotUseVbaCountToChangeVerdict() {
        RecommendationVO lowCount = recommendationService.build(
                plant(),
                occurrence(1),
                risk("Medium", RiskAssessmentStatus.ASSESSED));

        RecommendationVO highCount = recommendationService.build(
                plant(),
                occurrence(5000),
                risk("Medium", RiskAssessmentStatus.ASSESSED));

        assertThat(lowCount.getLevel()).isEqualTo(RecommendationLevel.USE_CAUTION);
        assertThat(highCount.getLevel()).isEqualTo(RecommendationLevel.USE_CAUTION);
    }

    /** 无 exact risk assessment -> Not Assessed，而不是 Lower Concern。 */
    @Test
    void shouldReturnNotAssessedWhenNoExactRiskAssessmentExists() {
        RecommendationVO result = recommendationService.build(
                plant(),
                occurrence(0),
                risk(null, RiskAssessmentStatus.NOT_ASSESSED));

        assertThat(result.getLevel()).isEqualTo(RecommendationLevel.NOT_ASSESSED);
        assertThat(result.getExplanation())
                .contains("no exact environmental weed risk assessment was found");
    }

    /** 创建测试用固定 PlantIdentityVO，减少每个测试的重复代码。 */
    private PlantIdentityVO plant() {
        return PlantIdentityVO.builder()
                .plantId(3L)
                .scientificName("Acacia baileyana")
                .commonName("Cootamundra Wattle")
                .family("Fabaceae")
                .establishmentMeans("Introduced")
                .degreeOfEstablishment("Naturalised")
                .build();
    }

    /** 根据 count 快速创建 FOUND/NOT_FOUND 测试数据。 */
    private LocalOccurrenceVO occurrence(int count) {
        return LocalOccurrenceVO.builder()
                .status(count > 0 ? OccurrenceStatus.FOUND : OccurrenceStatus.NOT_FOUND)
                .recordCount(count)
                .mostRecentRecordYear(count > 0 ? 2020 : null)
                .source("Victorian Biodiversity Atlas")
                .build();
    }

    /** 创建测试用风险对象。 */
    private EnvironmentalRiskVO risk(String rating, RiskAssessmentStatus status) {
        return EnvironmentalRiskVO.builder()
                .assessmentStatus(status)
                .rating(rating)
                .explanation("test")
                .source("2022 Advisory List of Environmental Weeds in Victoria")
                .build();
    }
}
