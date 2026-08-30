package com.plantky.domain.vo;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * GET /api/v1/plants/search 的完整成功响应。
 *
 * <pre>
 * {
 *   "query": "wattle",
 *   "results": [ ... ]
 * }
 * </pre>
 *
 * <p>没有匹配植物时仍然返回 HTTP 200，并且 results 是空数组，而不是返回 404。</p>
 */
@Getter
@Builder
public class PlantSearchResponse {

    /** 标准化后的原始搜索关键词，例如去除首尾空格后的 wattle。 */
    private final String query;

    /** 所有匹配结果。多个结果必须全部保留，后端不能自动选择第一条。 */
    private final List<PlantSearchItemVO> results;
}
