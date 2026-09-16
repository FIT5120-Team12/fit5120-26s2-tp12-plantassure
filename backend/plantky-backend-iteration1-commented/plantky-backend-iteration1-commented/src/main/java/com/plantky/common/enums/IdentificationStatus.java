package com.plantky.common.enums;

/**
 * Epic 1 AI plant identification 的业务结果状态。
 *
 * <p>该状态只描述“图片识别是否产生可展示候选”，与环境风险完全无关。</p>
 */
public enum IdentificationStatus {
    /** AI 返回了至少一个达到当前识别规则的 possible match。 */
    MATCHES_FOUND,

    /** AI 正常完成请求，但没有返回可用/足够可信的候选。 */
    NO_CONFIDENT_MATCH
}
