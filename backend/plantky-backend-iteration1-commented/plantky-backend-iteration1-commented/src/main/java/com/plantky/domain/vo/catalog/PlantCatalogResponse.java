package com.plantky.domain.vo.catalog;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** GET /api/v1/plants 的分页响应。 */
@Getter
@Builder
public class PlantCatalogResponse {
    private final List<PlantCatalogItemVO> items;
    private final long page;
    private final long size;
    private final long totalElements;
    private final long totalPages;
    private final String sort;
}
