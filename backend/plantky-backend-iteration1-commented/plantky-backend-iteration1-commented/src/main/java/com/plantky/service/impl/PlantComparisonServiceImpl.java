package com.plantky.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.plantky.common.enums.ErrorCode;
import com.plantky.common.exception.BusinessException;
import com.plantky.common.exception.PlantNotFoundException;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.LocalOccurrenceVO;
import com.plantky.domain.vo.compare.ComparisonPlantVO;
import com.plantky.domain.vo.compare.PlantComparisonResponse;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.PlantComparisonService;
import com.plantky.service.component.LegalStatusService;
import com.plantky.service.component.OccurrenceService;
import com.plantky.service.component.PlantClassificationService;
import com.plantky.service.component.TraitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Epic 2.2 Compare Plants 实现。
 *
 * <p>Compare 只返回规范化事实，不计算 winner / safestPlant / aggregate risk score。
 * 请求顺序会被保留，使前端列顺序保持确定性。</p>
 */
@Service
@RequiredArgsConstructor
public class PlantComparisonServiceImpl implements PlantComparisonService {

    private final SpeciesDataMapper speciesDataMapper;
    private final PlantClassificationService plantClassificationService;
    private final LegalStatusService legalStatusService;
    private final OccurrenceService occurrenceService;
    private final TraitService traitService;

    @Override
    public PlantComparisonResponse compare(String plantIds) {
        List<Long> requestedIds = parsePlantIds(plantIds);

        List<SpeciesDataEntity> records = speciesDataMapper.selectBatchIds(requestedIds);
        Map<Long, SpeciesDataEntity> byId = records.stream()
                .collect(Collectors.toMap(SpeciesDataEntity::getId, Function.identity()));

        List<ComparisonPlantVO> plants = new ArrayList<>();
        for (Long id : requestedIds) {
            SpeciesDataEntity entity = byId.get(id);
            if (entity == null) {
                throw new PlantNotFoundException();
            }

            // Compare 中某一个 local occurrence 数据不可用时，OccurrenceService 会返回
            // UNAVAILABLE；这里不让 optional evidence failure 使整个 compare 失败。
            LocalOccurrenceVO occurrence = occurrenceService.build(entity, new ArrayList<>());

            plants.add(ComparisonPlantVO.builder()
                    .plantId(entity.getId())
                    .commonName(entity.getVernacularName())
                    .scientificName(entity.getScientificName())
                    .imageUrl(null)
                    .environmentalConcern(
                            plantClassificationService.resolveEnvironmentalConcern(entity))
                    .legalStatus(legalStatusService.resolve(entity))
                    .originStatus(plantClassificationService.resolveOriginStatus(entity))
                    .growthForm(entity.getGrowthForm())
                    .lifeHistory(entity.getLifeHistory())
                    .woodiness(entity.getWoodiness())
                    .height(traitService.formatHeight(entity))
                    .localOccurrence(occurrence)
                    .build());
        }

        return PlantComparisonResponse.builder()
                .plants(List.copyOf(plants))
                .build();
    }

    /**
     * 校验 2–3 个 unique positive integer IDs。
     *
     * <p>使用 LinkedHashSet 同时检查重复并保留请求顺序。</p>
     */
    private List<Long> parsePlantIds(String plantIds) {
        if (!StringUtils.hasText(plantIds)) {
            throw new BusinessException(ErrorCode.INVALID_COMPARE_SELECTION);
        }

        String[] parts = plantIds.split(",", -1);
        if (parts.length < 2 || parts.length > 3) {
            throw new BusinessException(ErrorCode.INVALID_COMPARE_SELECTION);
        }

        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String part : parts) {
            try {
                long id = Long.parseLong(part.trim());
                if (id <= 0 || !ids.add(id)) {
                    throw new BusinessException(ErrorCode.INVALID_COMPARE_SELECTION);
                }
            } catch (NumberFormatException exception) {
                throw new BusinessException(ErrorCode.INVALID_COMPARE_SELECTION);
            }
        }

        if (ids.size() < 2 || ids.size() > 3) {
            throw new BusinessException(ErrorCode.INVALID_COMPARE_SELECTION);
        }
        return List.copyOf(ids);
    }
}
