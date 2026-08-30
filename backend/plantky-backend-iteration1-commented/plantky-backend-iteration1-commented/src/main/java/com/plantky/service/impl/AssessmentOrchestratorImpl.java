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
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.AssessmentOrchestrator;
import com.plantky.service.component.OccurrenceService;
import com.plantky.service.component.PlantIdentityService;
import com.plantky.service.component.RecommendationService;
import com.plantky.service.component.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AssessmentOrchestrator 的具体实现。
 *
 * <p>该类位于“应用编排层”，核心职责是控制完整 assessment 的执行顺序，
 * 而不是亲自实现每一条业务规则。</p>
 *
 * <pre>
 * plantId
 *   ↓
 * SpeciesDataMapper.selectById
 *   ↓
 * SpeciesDataEntity
 *   ├── PlantIdentityService
 *   ├── OccurrenceService
 *   └── RiskAssessmentService
 *                ↓
 *       RecommendationService
 *                ↓
 *     PlantAssessmentResponse
 * </pre>
 *
 * <p>这种拆分使每一块业务都能独立测试、独立修改，避免一个 Service 膨胀成几百行。</p>
 */
@Service
@RequiredArgsConstructor
public class AssessmentOrchestratorImpl implements AssessmentOrchestrator {

    /** 数据访问：根据 plantId 查询 species_data。 */
    private final SpeciesDataMapper speciesDataMapper;

    /** 构建 plant identity / establishment。 */
    private final PlantIdentityService plantIdentityService;

    /** 构建 City of Monash VBA occurrence evidence。 */
    private final OccurrenceService occurrenceService;

    /** 构建 2022 Advisory List environmental risk。 */
    private final RiskAssessmentService riskAssessmentService;

    /** 根据已验证风险生成唯一 recommendation，并组织自然语言解释。 */
    private final RecommendationService recommendationService;

    /**
     * 构建一株植物的完整 Assessment。
     *
     * <p>整个流程只查询数据库一次，然后把同一个 Entity 分发给不同组件。
     * 对当前单表/777 条数据场景来说，这比每个 component 再分别查询数据库更清晰也更高效。</p>
     *
     * @param plantId species_data.id
     * @return 完整 PlantAssessmentResponse
     */
    @Override
    public PlantAssessmentResponse assess(Long plantId) {
        // 1. 先根据主键获取一整行植物数据。
        SpeciesDataEntity entity = speciesDataMapper.selectById(plantId);

        // Assessment URL 指向的是明确资源；不存在时返回 404，而不是构造空 Assessment。
        if (entity == null) {
            throw new PlantNotFoundException();
        }

        // 2. 收集“非致命数据问题”。
        // 例如 VBA 最新年份缺失时，API 仍可返回其他有效数据，但 warnings 必须透明告知前端。
        List<String> warnings = new ArrayList<>();

        // 3. 分别交给单一职责组件构建各 section。
        PlantIdentityVO plant = plantIdentityService.build(entity);
        LocalOccurrenceVO localOccurrence = occurrenceService.build(entity, warnings);
        EnvironmentalRiskVO environmentalRisk = riskAssessmentService.build(entity, warnings);

        // 4. Recommendation 在风险结果已经标准化之后计算。
        // Occurrence/establishment 参与 explanation，但不会改变 environmental-risk verdict。
        RecommendationVO recommendation = recommendationService.build(
                plant,
                localOccurrence,
                environmentalRisk);

        // 5. 使用 Builder 将所有 section 组合成最终 API Response。
        return PlantAssessmentResponse.builder()
                .plant(plant)
                .localOccurrence(localOccurrence)
                .environmentalRisk(environmentalRisk)
                .recommendation(recommendation)
                .sources(buildSources())
                // copyOf 生成不可修改列表，避免 response 构建后又被外部代码继续 add/remove。
                .warnings(List.copyOf(warnings))
                .build();
    }

    /**
     * 构建固定的数据来源说明。
     *
     * <p>这部分是 Iteration 1 的 API 展示元数据，不需要每次从数据库读取。</p>
     *
     * @return VicFlora、VBA、2022 Advisory List 三个来源说明
     */
    private List<DataSourceVO> buildSources() {
        // List.of 返回不可修改集合，适合固定配置数据。
        return List.of(
                DataSourceVO.builder()
                        .name(DataSourceConstants.VICFLORA)
                        .role(DataSourceConstants.VICFLORA_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.VBA)
                        .role(DataSourceConstants.VBA_ROLE)
                        .build(),
                DataSourceVO.builder()
                        .name(DataSourceConstants.ADVISORY_LIST)
                        .role(DataSourceConstants.ADVISORY_LIST_ROLE)
                        .build());
    }
}
