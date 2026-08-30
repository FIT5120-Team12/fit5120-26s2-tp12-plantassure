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
 * <p>数据来源固定为 Victorian Biodiversity Atlas (VBA)。</p>
 *
 * <p><strong>重要业务规则：</strong>VBA 记录只表示“本地记录证据”，不能直接作为 environmental risk。
 * 因此这个 Service 只生成 LocalOccurrenceVO，不参与 Recommendation 风险等级计算。</p>
 */
@Slf4j
@Service
public class OccurrenceService {

    /** 当 VBA recordCount 本身不可用时返回给前端的 warning。 */
    private static final String UNAVAILABLE_WARNING =
            "City of Monash VBA occurrence check is unavailable.";

    /** 有记录数量但最新年份缺失时的 partial-data warning。 */
    private static final String YEAR_UNAVAILABLE_WARNING =
            "The most recent VBA record year is unavailable for this plant.";

    /**
     * 根据数据库中的 VBA 字段生成本地出现记录结果。
     *
     * <p>状态判断规则：</p>
     * <ol>
     *     <li>recordCount == null -> UNAVAILABLE</li>
     *     <li>recordCount <= 0 -> NOT_FOUND</li>
     *     <li>recordCount > 0 -> FOUND</li>
     * </ol>
     *
     * @param entity 植物数据库实体
     * @param warnings 当前 assessment 的 warning 集合；本组件可以向其中追加非致命数据问题
     * @return LocalOccurrenceVO
     */
    public LocalOccurrenceVO build(SpeciesDataEntity entity, List<String> warnings) {
        Integer recordCount = entity.getVbaRecordCount();
        Integer mostRecentYear = entity.getVbaMostRecentYear();

        // 情况 1：连 recordCount 都没有，说明不是“0 条记录”，而是“这项检查无法完成”。
        if (recordCount == null) {
            warnings.add(UNAVAILABLE_WARNING);

            return LocalOccurrenceVO.builder()
                    .status(OccurrenceStatus.UNAVAILABLE)
                    .recordCount(null)
                    .mostRecentRecordYear(null)
                    .source(DataSourceConstants.VBA)
                    .build();
        }

        // 情况 2：明确得到 0 条记录。
        // 注意 NOT_FOUND 的语义是“No matching VBA records”，不是“This plant does not occur in Monash”。
        if (recordCount <= 0) {
            // 如果 count <= 0 但数据库仍保存了年份，属于不一致数据。
            // 不让这种脏数据影响 API，忽略年份并在服务端日志中记录，便于数据团队排查。
            if (mostRecentYear != null) {
                log.warn(
                        "Inconsistent VBA data ignored. plantId={}, recordCount={}, mostRecentYear={}",
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

        // 情况 3：存在记录，但最新年份缺失。
        // 这不是致命错误，所以仍然返回 FOUND，只通过 warnings 告知前端存在 partial data。
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
