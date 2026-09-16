package com.plantky.service;

import com.plantky.domain.vo.PlantSearchResponse;

/** 植物搜索业务接口。 */
public interface PlantSearchService {

    /**
     * Iteration 1 保留的方法签名，便于既有单测/调用方继续使用。
     * 实现类会使用默认 limit。
     */
    PlantSearchResponse search(String query);

    /**
     * Iteration 2 新增 limit 支持，对应 Frontend API Contract。
     *
     * @param query common/scientific name
     * @param limit 最大返回条数
     */
    PlantSearchResponse search(String query, Integer limit);
}
