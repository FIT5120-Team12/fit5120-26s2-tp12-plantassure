package com.plantky.domain.vo.assessment;

import lombok.Builder;
import lombok.Getter;

/**
 * GRIIS supplementary evidence。
 *
 * <p>只对 VicFlora 明确为 INTRODUCED 的植物构建；native/uncertain 返回 null，
 * 避免让补充来源看起来像主风险评估。</p>
 */
@Getter
@Builder
public class GriisSupplementaryEvidenceVO {
    private final Boolean listed;
    private final Boolean invasive;
    private final String source;
}
