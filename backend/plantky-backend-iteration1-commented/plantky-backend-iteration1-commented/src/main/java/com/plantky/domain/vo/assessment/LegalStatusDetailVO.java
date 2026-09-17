package com.plantky.domain.vo.assessment;

import lombok.Builder;
import lombok.Getter;

/**
 * Legal status 的可用性说明。
 *
 * <p>主 API 仍返回稳定的 LegalStatus 枚举；该对象额外解释为什么当前为 UNAVAILABLE，
 * 方便前端透明展示而不是把 unavailable 误解成 not regulated。</p>
 */
@Getter
@Builder
public class LegalStatusDetailVO {
    private final boolean available;
    private final String note;
}
