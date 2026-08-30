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

/**
 * PlantSearchService 的 MySQL + MyBatis-Plus 实现。
 *
 * <p>该类负责 Search API 的核心后端逻辑：</p>
 * <ol>
 *     <li>校验并标准化 q；</li>
 *     <li>搜索 scientific_name / vernacular_name；</li>
 *     <li>保留所有 distinct 数据库结果，不自动选择第一条；</li>
 *     <li>只查询并返回搜索列表真正需要的字段。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class PlantSearchServiceImpl implements PlantSearchService {

    /**
     * 搜索词最大长度。
     * 防止异常超长请求，同时数据库对应字段最大长度也是可控的。
     */
    private static final int MAX_QUERY_LENGTH = 255;

    /**
     * MyBatis-Plus Mapper，由 Lombok 生成构造器后由 Spring 自动注入。
     */
    private final SpeciesDataMapper speciesDataMapper;

    /**
     * 执行植物搜索。
     *
     * @param query 用户输入的 common/scientific name 关键词
     * @return 搜索响应；无匹配时 results 为空数组
     */
    @Override
    public PlantSearchResponse search(String query) {
        // 先去除首尾空格并验证非空/长度，确保后续方法拿到的是有效文本。
        String normalizedQuery = normalizeQuery(query);

        // 对 LIKE 特殊字符进行文本转义，避免用户输入 % 或 _ 时意外变成通配符。
        String escapedQuery = DisplayValueUtils.escapeLikeKeyword(normalizedQuery);

        // LambdaQueryWrapper 使用 Java 方法引用指定字段，避免手写数据库列名字符串。
        LambdaQueryWrapper<SpeciesDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(
                        // Search 只需要三列，减少不必要的数据读取和传输。
                        SpeciesDataEntity::getId,
                        SpeciesDataEntity::getScientificName,
                        SpeciesDataEntity::getVernacularName)
                .and(item -> item
                        // 同一个 q 同时匹配 scientific name 和 common name。
                        .like(SpeciesDataEntity::getScientificName, escapedQuery)
                        .or()
                        .like(SpeciesDataEntity::getVernacularName, escapedQuery))
                // 结果排序保持稳定，有利于前端展示和自动化测试。
                .orderByAsc(SpeciesDataEntity::getScientificName);

        // Mapper 返回 Entity；通过 stream 映射成稳定的 API VO。
        List<PlantSearchItemVO> results = speciesDataMapper.selectList(wrapper).stream()
                .map(entity -> PlantSearchItemVO.builder()
                        .plantId(entity.getId())
                        .scientificName(entity.getScientificName())
                        .commonName(entity.getVernacularName())
                        .build())
                .toList();

        // Search API 的成功响应不使用额外 code/msg/data envelope，保持与既定 API Contract 一致。
        return PlantSearchResponse.builder()
                .query(normalizedQuery)
                .results(results)
                .build();
    }

    /**
     * 校验并标准化搜索关键词。
     *
     * @param query 原始 q 参数
     * @return trim 后的合法关键词
     * @throws InvalidSearchQueryException query 为空白或超过最大长度
     */
    private String normalizeQuery(String query) {
        // StringUtils.hasText 会拒绝 null、空字符串和只包含空格的字符串。
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
}
