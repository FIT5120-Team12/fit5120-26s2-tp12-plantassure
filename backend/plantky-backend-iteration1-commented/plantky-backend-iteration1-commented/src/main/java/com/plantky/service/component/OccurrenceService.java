package com.plantky.service.component;

import java.util.List;

import com.plantky.common.constant.DataSourceConstants;
import com.plantky.common.enums.OccurrenceStatus;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.LocalOccurrenceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * City of Monash Local Occurrence 业务组件。
 *
 * <p>Iteration 2 的主 local occurrence 来源仍然是 Victorian Biodiversity Atlas，
 * 但底层 extraction 从 I1 的 VBA25 更新为 VBA_FLORA100。</p>
 *
 * <p><strong>业务边界：</strong>occurrence evidence 绝不能改变 environmental concern
 * 或 recommendation。</p>
 */
@Slf4j
@Service
public class OccurrenceService {

    private static final String UNAVAILABLE_WARNING =
            "City of Monash VBA_FLORA100 occurrence check is unavailable.";

    private static final String YEAR_UNAVAILABLE_WARNING =
            "The most recent VBA_FLORA100 record year is unavailable for this plant.";

    /**
     * 根据 Iteration 2 VBA_FLORA100 字段构建 local occurrence。
     */
    public LocalOccurrenceVO build(SpeciesDataEntity entity, List<String> warnings) {
        /*
         * Iteration 1 旧实现：
         * Integer recordCount = entity.getVbaRecordCount();
         * Integer mostRecentYear = entity.getVbaMostRecentYear();
         *
         * Iteration 2 必须改成 VBA100 字段。继续使用旧 getter 会把新的数据来源错误标记成 I1 VBA25。
         */
        Integer recordCount = entity.getVba100RecordCount();
        Integer mostRecentYear = entity.getVba100MostRecentYear();

        if (recordCount == null) {
            warnings.add(UNAVAILABLE_WARNING);
            return LocalOccurrenceVO.builder()
                    .status(OccurrenceStatus.UNAVAILABLE)
                    .recordCount(null)
                    .mostRecentRecordYear(null)
                    .source(DataSourceConstants.VBA)
                    .build();
        }

        if (recordCount <= 0) {
            if (mostRecentYear != null) {
                log.warn(
                        "Inconsistent VBA100 data ignored. plantId={}, recordCount={}, mostRecentYear={}",
                        entity.getId(),
                        recordCount,
                        mostRecentYear);
            }

            return LocalOccurrenceVO.builder()
                    .status(OccurrenceStatus.NOT_FOUND)
                    .recordCount(0)
                    .mostRecentRecordYear(null)
                    .source(DataSourceConstants.VBA)
                    .build();
        }

        if (mostRecentYear == null) {
            warnings.add(YEAR_UNAVAILABLE_WARNING);
        }

        return LocalOccurrenceVO.builder()
                .status(OccurrenceStatus.FOUND)
                .recordCount(recordCount)
                .mostRecentRecordYear(mostRecentYear)
                .source(DataSourceConstants.VBA)
                .build();
    }
}
