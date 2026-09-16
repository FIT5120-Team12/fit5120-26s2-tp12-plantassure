package com.plantky.domain.query;

import java.util.List;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.OriginStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Catalog 查询参数对象。
 *
 * <p>Controller 只做 HTTP 参数绑定，Service 接收一个 query object，避免方法签名随着 filter 增加不断膨胀。</p>
 */
@Getter
@Builder
public class PlantCatalogQuery {
    private final String q;
    private final List<EnvironmentalConcern> environmentalConcern;
    private final List<OriginStatus> originStatus;
    private final String growthForm;
    private final String lifeHistory;
    private final String woodiness;
    private final Double minHeight;
    private final Double maxHeight;
    private final Integer page;
    private final Integer size;
    private final String sort;
}
