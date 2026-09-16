package com.plantky.service.component;

import java.util.List;
import java.util.Optional;

import com.plantky.common.constant.DataSourceConstants;
import com.plantky.common.enums.EnvironmentalRiskRating;
import com.plantky.common.enums.RiskAssessmentStatus;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.EnvironmentalRiskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 2022 Advisory List environmental risk 业务组件。
 *
 * <p>Iteration 2 数据与 I1 最大差异是：没有 exact match 时可能不再保存 null，
 * 而是显式保存 {@code Not Assessed / No exact match}。因此该文本必须映射为
 * NOT_ASSESSED，不能误报成 UNAVAILABLE。</p>
 */
@Slf4j
@Service
public class RiskAssessmentService {

    private static final String NOT_ASSESSED_MARKER = "Not Assessed / No exact match";

    private static final String UNAVAILABLE_WARNING =
            "Environmental weed risk check is unavailable because the stored rating is not recognised.";

    public EnvironmentalRiskVO build(SpeciesDataEntity entity, List<String> warnings) {
        String rawRiskRating = entity.getRiskRating();

        /*
         * Iteration 1 旧判断：
         * if (!StringUtils.hasText(rawRiskRating)) { ... NOT_ASSESSED ... }
         *
         * Iteration 2 新 SQL 会把“无 exact assessment”显式写成文本，因此只检查 null/blank 不够。
         */
        if (!StringUtils.hasText(rawRiskRating)
                || NOT_ASSESSED_MARKER.equalsIgnoreCase(rawRiskRating.trim())
                || "Not Assessed".equalsIgnoreCase(rawRiskRating.trim())) {
            return EnvironmentalRiskVO.builder()
                    .assessmentStatus(RiskAssessmentStatus.NOT_ASSESSED)
                    .rating(null)
                    .explanation("No exact matching assessment was found in the 2022 Advisory List.")
                    .source(DataSourceConstants.ADVISORY_LIST)
                    .build();
        }

        Optional<EnvironmentalRiskRating> rating =
                EnvironmentalRiskRating.fromDatabaseValue(rawRiskRating);

        if (rating.isEmpty()) {
            log.warn(
                    "Unrecognised environmental risk rating. plantId={}, riskRating={}",
                    entity.getId(),
                    rawRiskRating);
            warnings.add(UNAVAILABLE_WARNING);

            return EnvironmentalRiskVO.builder()
                    .assessmentStatus(RiskAssessmentStatus.UNAVAILABLE)
                    .rating(null)
                    .explanation("The environmental weed risk check is currently unavailable.")
                    .source(DataSourceConstants.ADVISORY_LIST)
                    .build();
        }

        String apiRating = rating.get().getApiValue();
        return EnvironmentalRiskVO.builder()
                .assessmentStatus(RiskAssessmentStatus.ASSESSED)
                .rating(apiRating)
                .explanation(
                        "This species has a "
                                + apiRating
                                + " environmental weed risk rating in the 2022 Advisory List.")
                .source(DataSourceConstants.ADVISORY_LIST)
                .build();
    }
}
