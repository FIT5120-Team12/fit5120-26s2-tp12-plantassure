package com.plantky.common.enums;

/**
 * Victorian legal/regulatory status。
 *
 * <p>当前 Iteration 2 数据没有可验证的法定分类字段，因此实际响应统一为
 * UNAVAILABLE。保留另外两个值是为了后续接入真实法律数据时无需破坏 API。</p>
 */
public enum LegalStatus {
    NOT_REGULATED,
    REGULATED,
    UNAVAILABLE
}
