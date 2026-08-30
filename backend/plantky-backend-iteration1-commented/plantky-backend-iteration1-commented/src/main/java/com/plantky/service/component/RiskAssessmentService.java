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
 * Environmental Weed Risk 业务组件。
 *
 * <p>该组件只负责解释数据库中来自 2022 Advisory List 的 risk_rating 字段：</p>
 * <ul>
 *     <li>合法风险值 -> ASSESSED</li>
 *     <li>null/空值 -> NOT_ASSESSED</li>
 *     <li>存在文本但系统无法识别 -> UNAVAILABLE + warning</li>
 * </ul>
 *
 * <p><strong>特别注意：</strong>NOT_ASSESSED 不等于 Low Risk / Safe。</p>
 */
@Slf4j
@Service
public class RiskAssessmentService {

    private static final String UNAVAILABLE_WARNING =
            "Environmental weed risk check is unavailable because the stored rating is not recognised.";

    /**
     * 把数据库 risk_rating 转换成 API environmentalRisk 对象。
     *
     * @param entity 植物实体
     * @param warnings assessment warning 集合
     * @return EnvironmentalRiskVO
     */
    public EnvironmentalRiskVO build(SpeciesDataEntity entity, List<String> warnings) {
        String rawRiskRating = entity.getRiskRating();

        // 没有 exact Advisory List assessment：返回 NOT_ASSESSED。
        // 不能为了“有结论”而自行生成 Low/Medium 等风险等级。
        if (!StringUtils.hasText(rawRiskRating)) {
            return EnvironmentalRiskVO.builder()
                    .assessmentStatus(RiskAssessmentStatus.NOT_ASSESSED)
                    .rating(null)
                    .explanation("No exact matching assessment was found in the 2022 Advisory List.")
                    .source(DataSourceConstants.ADVISORY_LIST)
                    .build();
        }

        // 将数据库文本转换为受控枚举。
        // 这样 RecommendationService 只处理已验证的标准风险类别。
        Optional<EnvironmentalRiskRating> rating =
                EnvironmentalRiskRating.fromDatabaseValue(rawRiskRating);

        // 数据库字段有值但不属于系统允许的风险枚举，说明数据格式异常。
        // 这里不能猜测，因此返回 UNAVAILABLE，同时写日志和 warning。
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

        // 数据库中的 "Moderately High Risk" 等值被标准化为 API 的 "Moderately High"。
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
