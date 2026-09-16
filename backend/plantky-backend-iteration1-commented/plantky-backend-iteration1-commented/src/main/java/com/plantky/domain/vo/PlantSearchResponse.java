package com.plantky.domain.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * GET /api/v1/plants/search 成功响应。
 *
 * <p>I1 前端读取 {@code results}，新的 Frontend Contract 使用 {@code items}。
 * 为保证连续开发，本迭代临时同时序列化两个名称，内容完全相同；后续前端完成迁移后
 * 可以在统一版本升级中移除旧别名。</p>
 */
@Getter
@Builder
public class PlantSearchResponse {

    private final String query;
    private final List<PlantSearchItemVO> results;

    /** I2 contract 兼容别名，不复制业务数据。 */
    @JsonProperty("items")
    public List<PlantSearchItemVO> getItems() {
        return results;
    }
}
