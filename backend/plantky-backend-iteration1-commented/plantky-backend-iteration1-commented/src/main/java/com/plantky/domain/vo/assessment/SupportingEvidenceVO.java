package com.plantky.domain.vo.assessment;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

/**
 * Iteration 2 occurrence supporting evidence 原始摘要。
 *
 * <p>字段名称明确写出 VBA100/ALA，避免前端或后端误以为它们还是 I1 的 VBA25/iNaturalist。</p>
 */
@Getter
@Builder
public class SupportingEvidenceVO {
    private final Integer vba100RecordCount;
    private final Integer vba100MostRecentYear;
    private final Integer alaRecordCount;
    private final LocalDate alaMostRecentDate;
}
