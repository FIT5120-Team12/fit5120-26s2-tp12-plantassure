package com.plantky.domain.vo.assessment;

import lombok.Builder;
import lombok.Getter;

/**
 * Iteration 2 AusTraits-backed trait block。
 *
 * <p>所有字段都允许为 null。缺失 trait 必须保持 unavailable，不能由其他字段猜测。</p>
 */
@Getter
@Builder
public class PlantTraitsVO {
    private final String growthForm;
    private final String woodiness;
    private final String lifeHistory;
    private final Double heightMinM;
    private final Double heightMaxM;

    /**
     * 供当前前端 Compare/Catalog 直接展示的高度文本，例如 "0.3–1 m"。
     * 如果 min/max 都缺失则为 null。
     */
    private final String height;
}
