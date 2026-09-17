package com.plantky.common.enums;

/**
 * VicFlora establishment/origin 的稳定 API 枚举。
 *
 * <p>UNCERTAIN 只在真实数据中的 establishment_means 明确为 uncertain 时返回；
 * 后端不会为了满足前端选项而凭空生成该状态。</p>
 */
public enum OriginStatus {
    NATIVE,
    INTRODUCED,
    UNCERTAIN
}
