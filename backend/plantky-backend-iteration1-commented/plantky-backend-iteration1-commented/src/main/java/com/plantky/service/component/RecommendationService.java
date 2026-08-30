package com.plantky.service.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.plantky.common.enums.EnvironmentalRiskRating;
import com.plantky.common.enums.OccurrenceStatus;
import com.plantky.common.enums.RecommendationLevel;
import com.plantky.common.enums.RiskAssessmentStatus;
import com.plantky.domain.vo.EnvironmentalRiskVO;
import com.plantky.domain.vo.LocalOccurrenceVO;
import com.plantky.domain.vo.PlantIdentityVO;
import com.plantky.domain.vo.RecommendationVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Iteration 1 Recommendation 规则组件。
 *
 * <p>这是当前项目最关键的业务规则之一。根据已确认的 Acceptance Criteria：</p>
 *
 * <table>
 *     <caption>Environmental risk -> Recommendation</caption>
 *     <tr><th>Environmental Risk</th><th>Recommendation</th></tr>
 *     <tr><td>Very High</td><td>Reconsider Planting</td></tr>
 *     <tr><td>High</td><td>Reconsider Planting</td></tr>
 *     <tr><td>Moderately High</td><td>Use Caution</td></tr>
 *     <tr><td>Medium</td><td>Use Caution</td></tr>
 *     <tr><td>Lower</td><td>Lower Concern</td></tr>
 *     <tr><td>No exact assessment</td><td>Not Assessed</td></tr>
 * </table>
 *
 * <p><strong>业务边界：</strong></p>
 * <ul>
 *     <li>Recommendation 等级只由已验证的 environmental risk 决定；</li>
 *     <li>VicFlora establishment 和 VBA occurrence 只作为 explanation 中的 supporting evidence；</li>
 *     <li>绝对不能根据 VBA record count 的多少提高/降低 recommendation。</li>
 * </ul>
 */
@Service
public class RecommendationService {

    /**
     * 构建 Recommendation VO。
     *
     * <p>该方法分成两件事：</p>
     * <ol>
     *     <li>{@link #determineLevel(EnvironmentalRiskVO)}：计算唯一 verdict；</li>
     *     <li>{@link #buildExplanation(PlantIdentityVO, LocalOccurrenceVO, EnvironmentalRiskVO)}：
     *     使用真实可用证据生成自然语言解释。</li>
     * </ol>
     *
     * @param plant 植物身份与 establishment 信息
     * @param occurrence City of Monash VBA 本地记录信息
     * @param risk 2022 Advisory List 风险信息
     * @return RecommendationVO
     */
    public RecommendationVO build(
            PlantIdentityVO plant,
            LocalOccurrenceVO occurrence,
            EnvironmentalRiskVO risk) {

        RecommendationLevel level = determineLevel(risk);

        return RecommendationVO.builder()
                // level 给程序/前端逻辑使用，例如 USE_CAUTION。
                .level(level)
                // displayLabel 给最终用户显示，例如 Use Caution。
                .displayLabel(level.getDisplayLabel())
                // Explanation 可以引用三类证据，但不会反过来修改 level。
                .explanation(buildExplanation(plant, occurrence, risk))
                .build();
    }

    /**
     * 根据 Environmental Risk 计算 Recommendation Level。
     *
     * <p>这里故意不接收 occurrence record count 等数据，
     * 从代码结构上防止后续开发人员错误地把 VBA count 加入 verdict 计算。</p>
     *
     * @param risk 已标准化的 environmental risk
     * @return 唯一 RecommendationLevel
     */
    private RecommendationLevel determineLevel(EnvironmentalRiskVO risk) {
        // 只要没有一个“已完成且可信”的 risk assessment，就不能生成 Lower/Medium/High 类结论。
        if (risk.getAssessmentStatus() != RiskAssessmentStatus.ASSESSED) {
            return RecommendationLevel.NOT_ASSESSED;
        }

        // 把 API 文本再次转换为受控枚举，确保只处理系统明确支持的风险类别。
        EnvironmentalRiskRating rating = EnvironmentalRiskRating.fromApiValue(risk.getRating())
                .orElse(null);

        // 防御性处理：理论上 ASSESSED 时 rating 应该合法；如果出现不一致，宁可 Not Assessed，也不猜测。
        if (rating == null) {
            return RecommendationLevel.NOT_ASSESSED;
        }

        return switch (rating) {
            case VERY_HIGH, HIGH -> RecommendationLevel.RECONSIDER_PLANTING;
            case MODERATELY_HIGH, MEDIUM -> RecommendationLevel.USE_CAUTION;
            case LOWER -> RecommendationLevel.LOWER_CONCERN;
        };
    }

    /**
     * 使用实际可用证据生成 recommendation 的自然语言解释。
     *
     * <p>这里的 explanation 是“为什么用户看到这些证据”，并不是一个新的风险算法。</p>
     *
     * @param plant identity/establishment 证据
     * @param occurrence local occurrence 证据
     * @param risk environmental risk 证据
     * @return 完整英文解释句子
     */
    private String buildExplanation(
            PlantIdentityVO plant,
            LocalOccurrenceVO occurrence,
            EnvironmentalRiskVO risk) {
        List<String> evidence = new ArrayList<>();

        // Establishment 可能缺失，因此只有有文本时才加入 explanation。
        String establishmentEvidence = buildEstablishmentEvidence(plant);
        if (StringUtils.hasText(establishmentEvidence)) {
            evidence.add(establishmentEvidence);
        }

        // Occurrence/Risk service 本身都有明确状态，所以即使 unavailable，
        // 也要加入一段透明说明，而不是静默省略。
        evidence.add(buildOccurrenceEvidence(occurrence));
        evidence.add(buildRiskEvidence(risk));

        return joinEvidence(evidence);
    }

    /**
     * 生成 establishment 证据短语。
     *
     * <p>如果 establishmentMeans 和 degreeOfEstablishment 都存在且不相同：</p>
     * <pre>This plant is introduced and naturalised in Victoria</pre>
     *
     * <p>如果二者相同或只有一个：</p>
     * <pre>This plant is native in Victoria</pre>
     */
    private String buildEstablishmentEvidence(PlantIdentityVO plant) {
        String means = plant.getEstablishmentMeans();
        String degree = plant.getDegreeOfEstablishment();

        if (!StringUtils.hasText(means) && !StringUtils.hasText(degree)) {
            return null;
        }

        if (StringUtils.hasText(means)
                && StringUtils.hasText(degree)
                && !means.equalsIgnoreCase(degree)) {
            return "This plant is "
                    + means.toLowerCase(Locale.ROOT)
                    + " and "
                    + degree.toLowerCase(Locale.ROOT)
                    + " in Victoria";
        }

        String value = StringUtils.hasText(means) ? means : degree;
        return "This plant is " + value.toLowerCase(Locale.ROOT) + " in Victoria";
    }

    /**
     * 根据 VBA 状态生成本地记录证据短语。
     *
     * <p>NOT_FOUND 使用经过确认的保守措辞：只说没有 matching records，
     * 绝不说该物种“不存在于 Monash”。</p>
     */
    private String buildOccurrenceEvidence(LocalOccurrenceVO occurrence) {
        if (occurrence.getStatus() == OccurrenceStatus.FOUND) {
            Integer count = occurrence.getRecordCount();

            // 仅在语法上区分 record / records；count 大小不参与 recommendation 规则。
            if (count != null && count == 1) {
                return "has a documented occurrence record in the City of Monash";
            }
            return "has documented occurrence records in the City of Monash";
        }

        if (occurrence.getStatus() == OccurrenceStatus.NOT_FOUND) {
            return "no matching VBA records were found in the City of Monash";
        }

        return "local occurrence evidence is currently unavailable";
    }

    /** 根据风险评估状态生成 environmental risk 证据短语。 */
    private String buildRiskEvidence(EnvironmentalRiskVO risk) {
        if (risk.getAssessmentStatus() == RiskAssessmentStatus.ASSESSED) {
            return "has a " + risk.getRating() + " environmental weed risk rating";
        }

        if (risk.getAssessmentStatus() == RiskAssessmentStatus.NOT_ASSESSED) {
            return "no exact environmental weed risk assessment was found";
        }

        return "the environmental weed risk check is currently unavailable";
    }

    /**
     * 把多个证据短语拼成自然英文句子。
     *
     * <p>例如三段 evidence：</p>
     * <pre>
     * This plant is introduced and naturalised in Victoria,
     * has a documented occurrence record in the City of Monash,
     * and has a Moderately High environmental weed risk rating.
     * </pre>
     *
     * @param evidence 非空证据短语列表
     * @return 带句号的完整句子
     */
    private String joinEvidence(List<String> evidence) {
        if (evidence.isEmpty()) {
            return "Available evidence is insufficient to provide an explanation.";
        }

        if (evidence.size() == 1) {
            return ensurePeriod(evidence.get(0));
        }

        if (evidence.size() == 2) {
            return ensurePeriod(evidence.get(0) + ", and " + evidence.get(1));
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < evidence.size(); i++) {
            if (i == 0) {
                builder.append(evidence.get(i));
            } else if (i == evidence.size() - 1) {
                // 最后一项使用 Oxford-style ", and"，让解释更自然。
                builder.append(", and ").append(evidence.get(i));
            } else {
                builder.append(", ").append(evidence.get(i));
            }
        }

        return ensurePeriod(builder.toString());
    }

    /** 保证 explanation 最终有句号，避免多个调用方自己处理格式。 */
    private String ensurePeriod(String value) {
        return value.endsWith(".") ? value : value + ".";
    }
}
