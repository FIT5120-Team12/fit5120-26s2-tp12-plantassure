package com.plantky.common.enums;

/**
 * City of Monash 本地出现记录检查状态。
 *
 * <p>该枚举表示“VBA 数据检查结果”，而不是植物是否真的存在于 Monash。</p>
 */
public enum OccurrenceStatus {

    /** VBA 中找到了至少一条匹配记录。 */
    FOUND,

    /** VBA 中没有找到匹配记录。注意：没有记录不等于物种一定不存在。 */
    NOT_FOUND,

    /** 数据本身不可用、缺失或无法完成检查。 */
    UNAVAILABLE
}
