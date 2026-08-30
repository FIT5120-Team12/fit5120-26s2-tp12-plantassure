package com.plantky.domain.vo;

import com.plantky.common.enums.OccurrenceStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Assessment API 的 City of Monash Local Occurrence 区域响应对象。
 */
@Getter
@Builder
public class LocalOccurrenceVO {

    /** VBA 检查状态：FOUND / NOT_FOUND / UNAVAILABLE。 */
    private final OccurrenceStatus status;

    /** 匹配的 VBA 记录数量；UNAVAILABLE 时可能为 null。 */
    private final Integer recordCount;

    /** 最新匹配记录年份；没有记录或年份不可用时为 null。 */
    private final Integer mostRecentRecordYear;

    /** 数据来源，当前固定为 Victorian Biodiversity Atlas。 */
    private final String source;
}
