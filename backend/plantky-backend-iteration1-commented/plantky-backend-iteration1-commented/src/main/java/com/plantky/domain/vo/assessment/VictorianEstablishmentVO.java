package com.plantky.domain.vo.assessment;

import lombok.Builder;
import lombok.Getter;

/**
 * VicFlora Victorian establishment 兼容对象。
 *
 * <p>完整枚举尚未由项目资料定义，因此 status 直接由真实数据规范化为 uppercase 值，
 * 而不是后端创造不存在的 establishment categories。</p>
 */
@Getter
@Builder
public class VictorianEstablishmentVO {
    private final String status;
    private final String label;
}
