package com.plantky.service.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.plantky.common.enums.OccurrenceStatus;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.LocalOccurrenceVO;
import org.junit.jupiter.api.Test;

/**
 * OccurrenceService 业务规则单元测试。
 *
 * <p>该 Service 无外部依赖，因此直接 new 即可，不需要启动 Spring 容器。</p>
 */
class OccurrenceServiceTest {

    private final OccurrenceService occurrenceService = new OccurrenceService();

    /** recordCount > 0 时必须得到 FOUND，并保留 count/year。 */
    @Test
    void shouldReturnFoundWhenRecordCountIsPositive() {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setVbaRecordCount(8);
        entity.setVbaMostRecentYear(2025);

        LocalOccurrenceVO result = occurrenceService.build(
                entity,
                new ArrayList<>());

        assertThat(result.getStatus()).isEqualTo(OccurrenceStatus.FOUND);
        assertThat(result.getRecordCount()).isEqualTo(8);
        assertThat(result.getMostRecentRecordYear()).isEqualTo(2025);
    }

    /**
     * 0 条 VBA 记录必须返回 NOT_FOUND，且不能把这个状态解释成物种一定不存在。
     */
    @Test
    void shouldReturnNotFoundWithoutClaimingSpeciesAbsence() {
        SpeciesDataEntity entity = new SpeciesDataEntity();
        entity.setVbaRecordCount(0);
        entity.setVbaMostRecentYear(null);
        List<String> warnings = new ArrayList<>();

        LocalOccurrenceVO result = occurrenceService.build(entity, warnings);

        assertThat(result.getStatus()).isEqualTo(OccurrenceStatus.NOT_FOUND);
        assertThat(result.getRecordCount()).isZero();
        assertThat(result.getMostRecentRecordYear()).isNull();
        assertThat(warnings).isEmpty();
    }
}
