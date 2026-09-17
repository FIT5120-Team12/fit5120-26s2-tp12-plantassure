package com.plantky.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.ErrorCode;
import com.plantky.common.enums.LegalStatus;
import com.plantky.common.exception.BusinessException;
import com.plantky.common.exception.PlantNotFoundException;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.alternatives.AlternativePlantVO;
import com.plantky.domain.vo.alternatives.CurrentPlantVO;
import com.plantky.domain.vo.alternatives.PlantAlternativesResponse;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.PlantAlternativeService;
import com.plantky.service.component.LegalStatusService;
import com.plantky.service.component.PlantClassificationService;
import com.plantky.service.component.TraitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Epic 2.1 Find a Better Plant 实现。
 *
 * <p><strong>为什么不直接读取 alternatives_json：</strong>当前 pipeline 输出中可能把
 * NOT_ASSESSED 植物列为 alternative，但最新 Epic 要求 candidate 必须有更低的
 * documented environmental concern。因此 Backend 在请求时重新执行 concern eligibility +
 * supported-trait similarity 验证。</p>
 *
 * <p>environmental concern 只负责“是否有资格成为更低 concern candidate”；trait similarity
 * 只负责候选之间的相似性排序，两者不会合并成 riskScore/safetyScore。</p>
 */
@Service
@RequiredArgsConstructor
public class PlantAlternativeServiceImpl implements PlantAlternativeService {

    private static final int DEFAULT_LIMIT = 6;
    private static final int MAX_LIMIT = 20;

    private final SpeciesDataMapper speciesDataMapper;
    private final PlantClassificationService plantClassificationService;
    private final TraitService traitService;
    private final LegalStatusService legalStatusService;

    @Override
    public PlantAlternativesResponse findAlternatives(Long plantId, Integer limit) {
        int normalizedLimit = normalizeLimit(limit);
        SpeciesDataEntity current = speciesDataMapper.selectById(plantId);
        if (current == null) {
            throw new PlantNotFoundException();
        }

        EnvironmentalConcern currentConcern =
                plantClassificationService.resolveEnvironmentalConcern(current);
        CurrentPlantVO currentPlant = toCurrentPlant(current, currentConcern);

        // Find a Better Plant 只对有 documented concern 且存在更低等级的情况有意义。
        List<String> lowerRiskValues = EnvironmentalConcern.lowerDatabaseValuesThan(currentConcern);
        if (lowerRiskValues.isEmpty()) {
            return response("not_applicable", currentPlant, List.of());
        }

        if (!hasAnySupportedTrait(current)) {
            return response("insufficient_trait_data", currentPlant, List.of());
        }

        LambdaQueryWrapper<SpeciesDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SpeciesDataEntity::getId, current.getId())
                .in(SpeciesDataEntity::getRiskRating, lowerRiskValues)
                .orderByAsc(SpeciesDataEntity::getScientificName);

        List<RankedAlternative> ranked = new ArrayList<>();
        for (SpeciesDataEntity candidate : speciesDataMapper.selectList(wrapper)) {
            EnvironmentalConcern candidateConcern =
                    plantClassificationService.resolveEnvironmentalConcern(candidate);

            // 防御性再次校验：即使 SQL 条件未来修改，也不能让 NOT_ASSESSED/UNAVAILABLE 混入。
            if (!candidateConcern.isLowerThan(currentConcern)) {
                continue;
            }

            // Contract 要求 regulated plants 不应返回。
            // 当前 legal dataset 缺失时服务返回 UNAVAILABLE，因此无法证明 regulated/non-regulated；
            // 一旦未来接入真实 legal 数据，这一判断会自动开始排除 REGULATED。
            if (legalStatusService.resolve(candidate) == LegalStatus.REGULATED) {
                continue;
            }

            Similarity similarity = calculateSimilarity(current, candidate);
            if (similarity.reasons().isEmpty()) {
                continue;
            }

            ranked.add(new RankedAlternative(candidate, candidateConcern, similarity));
        }

        List<AlternativePlantVO> alternatives = ranked.stream()
                // 只按 supported-trait similarity 排序；不产生或暴露综合风险分数。
                .sorted(Comparator
                        .comparingInt((RankedAlternative item) -> item.similarity().score())
                        .reversed()
                        .thenComparing(item -> item.entity().getScientificName(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .limit(normalizedLimit)
                .map(this::toAlternative)
                .toList();

        return response(
                alternatives.isEmpty() ? "no_strict_match_found" : "matched",
                currentPlant,
                alternatives);
    }

    private Similarity calculateSimilarity(
            SpeciesDataEntity current,
            SpeciesDataEntity candidate) {
        List<String> reasons = new ArrayList<>();
        int score = 0;

        if (sameText(current.getGrowthForm(), candidate.getGrowthForm())) {
            reasons.add("Similar growth form");
            score += 4;
        }
        if (sameText(current.getLifeHistory(), candidate.getLifeHistory())) {
            reasons.add("Same life-history category");
            score += 3;
        }
        if (sameText(current.getWoodiness(), candidate.getWoodiness())) {
            reasons.add("Similar woodiness");
            score += 2;
        }
        if (heightRangesOverlap(current, candidate)) {
            reasons.add("Similar mature height");
            score += 1;
        }

        return new Similarity(score, List.copyOf(reasons));
    }

    /**
     * 高度只在双方 min/max 都有真实值时参与比较；任一缺失都不推断。
     */
    private boolean heightRangesOverlap(SpeciesDataEntity left, SpeciesDataEntity right) {
        if (left.getHeightMin() == null
                || left.getHeightMax() == null
                || right.getHeightMin() == null
                || right.getHeightMax() == null) {
            return false;
        }

        return left.getHeightMin() <= right.getHeightMax()
                && right.getHeightMin() <= left.getHeightMax();
    }

    private boolean hasAnySupportedTrait(SpeciesDataEntity entity) {
        return StringUtils.hasText(entity.getGrowthForm())
                || StringUtils.hasText(entity.getLifeHistory())
                || StringUtils.hasText(entity.getWoodiness())
                || (entity.getHeightMin() != null && entity.getHeightMax() != null);
    }

    private boolean sameText(String left, String right) {
        return StringUtils.hasText(left)
                && StringUtils.hasText(right)
                && left.trim().toLowerCase(Locale.ROOT)
                        .equals(right.trim().toLowerCase(Locale.ROOT));
    }

    private CurrentPlantVO toCurrentPlant(
            SpeciesDataEntity entity,
            EnvironmentalConcern concern) {
        return CurrentPlantVO.builder()
                .plantId(entity.getId())
                .commonName(entity.getVernacularName())
                .scientificName(entity.getScientificName())
                .imageUrl(null)
                .environmentalConcern(concern)
                .growthForm(entity.getGrowthForm())
                .lifeHistory(entity.getLifeHistory())
                .woodiness(entity.getWoodiness())
                .height(traitService.formatHeight(entity))
                .build();
    }

    private AlternativePlantVO toAlternative(RankedAlternative item) {
        SpeciesDataEntity entity = item.entity();
        return AlternativePlantVO.builder()
                .plantId(entity.getId())
                .commonName(entity.getVernacularName())
                .scientificName(entity.getScientificName())
                .imageUrl(null)
                .environmentalConcern(item.concern())
                .originStatus(plantClassificationService.resolveOriginStatus(entity))
                .legalStatus(legalStatusService.resolve(entity))
                .growthForm(entity.getGrowthForm())
                .lifeHistory(entity.getLifeHistory())
                .woodiness(entity.getWoodiness())
                .height(traitService.formatHeight(entity))
                .matchReasons(item.similarity().reasons())
                .build();
    }

    private PlantAlternativesResponse response(
            String status,
            CurrentPlantVO currentPlant,
            List<AlternativePlantVO> alternatives) {
        return PlantAlternativesResponse.builder()
                .status(status)
                .currentPlant(currentPlant)
                .alternatives(alternatives)
                .build();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "limit must be between 1 and 20.");
        }
        return limit;
    }

    private record Similarity(int score, List<String> reasons) {
    }

    private record RankedAlternative(
            SpeciesDataEntity entity,
            EnvironmentalConcern concern,
            Similarity similarity) {
    }
}
