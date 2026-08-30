package com.plantky.domain.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * Assessment API 中的数据来源说明对象。
 *
 * <p>前端会在 Data Sources 区域展示这些信息，让用户知道每一类结论来自哪个公开数据源。</p>
 */
@Getter
@Builder
public class DataSourceVO {

    /** 数据源名称，例如 VicFlora。 */
    private final String name;

    /** 该数据源在当前业务中的作用，例如 Plant identity and establishment status。 */
    private final String role;
}
