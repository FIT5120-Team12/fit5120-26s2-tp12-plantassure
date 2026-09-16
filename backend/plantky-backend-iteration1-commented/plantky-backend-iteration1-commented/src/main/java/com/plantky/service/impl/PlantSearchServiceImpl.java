package com.plantky.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plantky.common.exception.InvalidSearchQueryException;
import com.plantky.common.util.DisplayValueUtils;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.PlantSearchItemVO;
import com.plantky.domain.vo.PlantSearchResponse;
import com.plantky.mapper.SpeciesDataMapper;
import com.plantky.service.PlantSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** MySQL + MyBatis-Plus 植物搜索实现。 */
@Service
@RequiredArgsConstructor
public class PlantSearchServiceImpl implements PlantSearchService {

    private static final int MAX_QUERY_LENGTH = 255;
    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 100;

    private final SpeciesDataMapper speciesDataMapper;

    /** I1 兼容入口：继续允许旧代码只传 query。 */
    @Override
    public PlantSearchResponse search(String query) {
        return search(query, DEFAULT_LIMIT);
    }

    /** I2 入口：支持显式 limit。 */
    @Override
    public PlantSearchResponse search(String query, Integer limit) {
        String normalizedQuery = normalizeQuery(query);
        int normalizedLimit = normalizeLimit(limit);
        String escapedQuery = DisplayValueUtils.escapeLikeKeyword(normalizedQuery);

        LambdaQueryWrapper<SpeciesDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(
                        SpeciesDataEntity::getId,
                        SpeciesDataEntity::getScientificName,
                        SpeciesDataEntity::getVernacularName,
                        SpeciesDataEntity::getFamily)
                .and(item -> item
                        .like(SpeciesDataEntity::getScientificName, escapedQuery)
                        .or()
                        .like(SpeciesDataEntity::getVernacularName, escapedQuery))
                .orderByAsc(SpeciesDataEntity::getScientificName)
                .last("LIMIT " + normalizedLimit);

        List<PlantSearchItemVO> results = speciesDataMapper.selectList(wrapper).stream()
                .map(entity -> PlantSearchItemVO.builder()
                        .plantId(entity.getId())
                        .scientificName(entity.getScientificName())
                        .commonName(entity.getVernacularName())
                        .family(entity.getFamily())
                        .imageUrl(null)
                        .build())
                .toList();

        return PlantSearchResponse.builder()
                .query(normalizedQuery)
                .results(results)
                .build();
    }

    private String normalizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            throw new InvalidSearchQueryException();
        }

        String normalizedQuery = query.trim();
        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            throw new InvalidSearchQueryException(
                    "Search query must not exceed 255 characters.");
        }
        return normalizedQuery;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new InvalidSearchQueryException("limit must be between 1 and 100.");
        }
        return limit;
    }
}
