package com.plantky.common.enums;

/**
 * Environmental Weed Risk 的评估状态。
 */
public enum RiskAssessmentStatus {

    /** 在 2022 Advisory List 中存在可识别的风险等级。 */
    ASSESSED,

    /** 没有 exact matching assessment；不能解释成 Low Risk。 */
    NOT_ASSESSED,

    /** 由于异常数据或数据不可用，本次风险检查无法完成。 */
    UNAVAILABLE
}
