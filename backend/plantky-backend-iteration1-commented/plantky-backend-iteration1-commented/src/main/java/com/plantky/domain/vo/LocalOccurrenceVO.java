package com.plantky.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plantky.common.enums.OccurrenceStatus;
import lombok.Builder;
import lombok.Getter;

/** Assessment API 的 City of Monash Local Occurrence 区域响应对象。 */
@Getter
@Builder
public class LocalOccurrenceVO {

    private final OccurrenceStatus status;
    private final Integer recordCount;

    /**
     * I1 前端仍使用 mostRecentRecordYear，因此保留原字段名避免 breaking change。
     */
    private final Integer mostRecentRecordYear;

    private final String source;

    /**
     * Iteration 2 Frontend Contract 使用 latestRecordYear。
     *
     * <p>通过只读兼容 getter 同时序列化该字段，不删除 I1 的 mostRecentRecordYear。</p>
     */
    @JsonProperty("latestRecordYear")
    public Integer getLatestRecordYear() {
        return mostRecentRecordYear;
    }
}
