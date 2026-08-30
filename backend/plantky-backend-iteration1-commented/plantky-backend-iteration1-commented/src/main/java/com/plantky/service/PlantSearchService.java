package com.plantky.service;

import com.plantky.domain.vo.PlantSearchResponse;

/**
 * 植物搜索业务接口。
 *
 * <p>Controller 依赖接口而不是具体实现类，这样可以降低耦合：</p>
 * <pre>
 * PlantController -> PlantSearchService -> PlantSearchServiceImpl
 * </pre>
 *
 * <p>未来如果搜索实现从 MySQL 改为 Elasticsearch，Controller 不需要修改。</p>
 */
public interface PlantSearchService {

    /**
     * 按 scientific name 或 common name 搜索植物。
     *
     * @param query 用户输入的搜索关键词
     * @return 包含 query 和所有匹配植物的响应对象
     * @throws com.plantky.common.exception.InvalidSearchQueryException 当 query 为空或过长时抛出
     */
    PlantSearchResponse search(String query);
}
