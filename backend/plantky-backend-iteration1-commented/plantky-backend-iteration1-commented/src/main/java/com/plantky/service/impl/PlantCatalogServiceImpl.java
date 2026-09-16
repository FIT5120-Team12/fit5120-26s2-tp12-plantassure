package com.plantky.service.impl;

import java.util.List;
import java.util.Locale;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.ErrorCode;
import com.plantky.common.enums.OriginStatus;
import com.plantky.common.exception.BusinessException;
import com.plantky.common.util.DisplayValueUtils;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.query.PlantCatalogQuery;
import com.plantky.domain.vo.catalog.PlantCatalogItemVO;
import com.plantky.domain.vo.catalog.PlantCatalogResponse;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.PlantCatalogService;
import com.plantky.service.component.PlantClassificationService;
import com.plantky.service.component.TraitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Epic 3 Catalog 的 MyBatis-Plus 实现。
 *
 * <p>Catalog 只展示 assessed plants，因此基础查询永远限定在五个 documented concern 值。
 * NOT_ASSESSED / UNAVAILABLE 不会因为缺数据而被误当成低风险卡片。</p>
 */
@Service
@RequiredArgsConstructor
public class PlantCatalogServiceImpl implements PlantCatalogService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT = "commonName,asc";

    private final SpeciesDataMapper speciesDataMapper;
    private final PlantClassificationService plantClassificationService;
    private final TraitService traitService;

    @Override
    public PlantCatalogResponse browse(PlantCatalogQuery query) {
        int page = query.getPage() == null ? DEFAULT_PAGE : query.getPage();
        int size = query.getSize() == null ? DEFAULT_SIZE : query.getSize();
        String sort = StringUtils.hasText(query.getSort()) ? query.getSort().trim() : DEFAULT_SORT;

        validatePagination(page, size);
        SortDefinition sortDefinition = parseSort(sort);

        LambdaQueryWrapper<SpeciesDataEntity> wrapper = new LambdaQueryWrapper<>();

        // Epic 3 是 Browse Assessed Plants：基础条件只允许 documented concern。
        wrapper.in(
                SpeciesDataEntity::getRiskRating,
                List.of(
                        "Very High Risk",
                        "High Risk",
                        "Moderately High Risk",
                        "Medium Risk",
                        "Lower Risk"));

        applyNameSearch(wrapper, query.getQ());
        applyConcernFilter(wrapper, query.getEnvironmentalConcern());
        applyOriginFilter(wrapper, query.getOriginStatus());
        applyTraitFilters(wrapper, query);
        applySort(wrapper, sortDefinition);

        Page<SpeciesDataEntity> pageRequest = new Page<>((long) page + 1L, size);
        Page<SpeciesDataEntity> result = speciesDataMapper.selectPage(pageRequest, wrapper);

        List<PlantCatalogItemVO> items = result.getRecords().stream()
                .map(this::toItem)
                .toList();

        return PlantCatalogResponse.builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(result.getTotal())
                .totalPages(result.getPages())
                .sort(sortDefinition.normalized())
                .build();
    }

    private void applyNameSearch(LambdaQueryWrapper<SpeciesDataEntity> wrapper, String q) {
        if (!StringUtils.hasText(q)) {
            return;
        }
        String escaped = DisplayValueUtils.escapeLikeKeyword(q.trim());
        wrapper.and(item -> item
                .like(SpeciesDataEntity::getScientificName, escaped)
                .or()
                .like(SpeciesDataEntity::getVernacularName, escaped));
    }

    private void applyConcernFilter(
            LambdaQueryWrapper<SpeciesDataEntity> wrapper,
            List<EnvironmentalConcern> concerns) {
        if (concerns == null || concerns.isEmpty()) {
            return;
        }

        if (concerns.stream().anyMatch(item -> item == null || !item.isDocumented())) {
            throw new BusinessException(
                    ErrorCode.INVALID_FILTER,
                    "Catalog environmentalConcern only accepts documented concern levels.");
        }

        wrapper.in(
                SpeciesDataEntity::getRiskRating,
                concerns.stream().map(EnvironmentalConcern::getDatabaseValue).toList());
    }

    private void applyOriginFilter(
            LambdaQueryWrapper<SpeciesDataEntity> wrapper,
            List<OriginStatus> origins) {
        if (origins == null || origins.isEmpty()) {
            return;
        }

        wrapper.in(
                SpeciesDataEntity::getEstablishmentMeans,
                origins.stream()
                        .map(item -> item.name().toLowerCase(Locale.ROOT))
                        .toList());
    }

    private void applyTraitFilters(
            LambdaQueryWrapper<SpeciesDataEntity> wrapper,
            PlantCatalogQuery query) {
        if (StringUtils.hasText(query.getGrowthForm())) {
            wrapper.eq(SpeciesDataEntity::getGrowthForm, query.getGrowthForm().trim().toLowerCase(Locale.ROOT));
        }
        if (StringUtils.hasText(query.getLifeHistory())) {
            wrapper.eq(SpeciesDataEntity::getLifeHistory, query.getLifeHistory().trim().toLowerCase(Locale.ROOT));
        }
        if (StringUtils.hasText(query.getWoodiness())) {
            wrapper.eq(SpeciesDataEntity::getWoodiness, query.getWoodiness().trim().toLowerCase(Locale.ROOT));
        }

        // Size filter 使用真实 height range overlap；缺失高度会自然不匹配，不进行推断。
        if (query.getMinHeight() != null) {
            wrapper.ge(SpeciesDataEntity::getHeightMax, query.getMinHeight());
        }
        if (query.getMaxHeight() != null) {
            wrapper.le(SpeciesDataEntity::getHeightMin, query.getMaxHeight());
        }

        if (query.getMinHeight() != null
                && query.getMaxHeight() != null
                && query.getMinHeight() > query.getMaxHeight()) {
            throw new BusinessException(
                    ErrorCode.INVALID_FILTER,
                    "minHeight must not be greater than maxHeight.");
        }
    }

    private void applySort(
            LambdaQueryWrapper<SpeciesDataEntity> wrapper,
            SortDefinition sort) {
        boolean asc = sort.ascending();
        if ("commonName".equals(sort.field())) {
            wrapper.orderBy(true, asc, SpeciesDataEntity::getVernacularName)
                    .orderByAsc(SpeciesDataEntity::getScientificName);
            return;
        }
        wrapper.orderBy(true, asc, SpeciesDataEntity::getScientificName);
    }

    private PlantCatalogItemVO toItem(SpeciesDataEntity entity) {
        return PlantCatalogItemVO.builder()
                .plantId(entity.getId())
                .commonName(entity.getVernacularName())
                .scientificName(entity.getScientificName())
                .imageUrl(null)
                .environmentalConcern(plantClassificationService.resolveEnvironmentalConcern(entity))
                .originStatus(plantClassificationService.resolveOriginStatus(entity))
                .growthForm(entity.getGrowthForm())
                .lifeHistory(entity.getLifeHistory())
                .woodiness(entity.getWoodiness())
                .height(traitService.formatHeight(entity))
                .build();
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAGE);
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    private SortDefinition parseSort(String rawSort) {
        String[] parts = rawSort.split(",", -1);
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.INVALID_SORT);
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase(Locale.ROOT);
        if (!("commonName".equals(field) || "scientificName".equals(field))) {
            throw new BusinessException(ErrorCode.INVALID_SORT);
        }
        if (!("asc".equals(direction) || "desc".equals(direction))) {
            throw new BusinessException(ErrorCode.INVALID_SORT);
        }

        return new SortDefinition(field, "asc".equals(direction));
    }

    /** 内部排序定义，不作为 API model 暴露。 */
    private record SortDefinition(String field, boolean ascending) {
        private String normalized() {
            return field + "," + (ascending ? "asc" : "desc");
        }
    }
}
