package com.plantky.service.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.plantky.common.enums.RiskAssessmentStatus;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.EnvironmentalRiskVO;
import org.junit.jupiter.api.Test;

/**
 * RiskAssessmentService 的核心风险映射测试。
 */
class RiskAssessmentServiceTest {

    private final RiskAssessmentService riskAssessmentService = new RiskAssessmentService();

    /** 数据库 Moderately High Risk 必须映射为 API 的 Moderately High。 */
    @Test
    void shouldMapDatabaseRiskToApiRisk() {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setRiskRating("Moderately High Risk");
        List<String> warnings = new ArrayList<>();

        EnvironmentalRiskVO result = riskAssessmentService.build(entity, warnings);

        assertThat(result.getAssessmentStatus()).isEqualTo(RiskAssessmentStatus.ASSESSED);
        assertThat(result.getRating()).isEqualTo("Moderately High");
        assertThat(warnings).isEmpty();
    }

    /**
     * risk_rating = null 表示没有 exact assessment，必须返回 NOT_ASSESSED，
     * 绝对不能偷偷转换成 Low Risk。
     */
    @Test
    void shouldTreatNullRiskAsNotAssessedNotLowRisk() {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setRiskRating(null);
        List<String> warnings = new ArrayList<>();

        EnvironmentalRiskVO result = riskAssessmentService.build(entity, warnings);

        assertThat(result.getAssessmentStatus()).isEqualTo(RiskAssessmentStatus.NOT_ASSESSED);
        assertThat(result.getRating()).isNull();
        assertThat(result.getExplanation()).contains("No exact matching assessment");
    }
}
