package com.plantky.service.component;

import com.plantky.common.enums.LegalStatus;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.assessment.LegalStatusDetailVO;
import org.springframework.stereotype.Service;

/**
 * Victorian legal status 业务边界。
 *
 * <p>当前 Iteration 2 数据没有可验证的 statutory/legal classification。
 * 因此本服务明确返回 UNAVAILABLE，而不是把“没有数据”错误解释成 NOT_REGULATED。</p>
 */
@Service
public class LegalStatusService {

    private static final String UNAVAILABLE_NOTE =
            "Verified Victorian legal classification is unavailable in the current Iteration 2 dataset.";

    /** 当前数据条件下始终返回 UNAVAILABLE；保留 Entity 参数便于未来接入真实字段。 */
    public LegalStatus resolve(SpeciesDataEntity entity) {
        return LegalStatus.UNAVAILABLE;
    }

    /** 返回前端可透明展示的 availability + note。 */
    public LegalStatusDetailVO buildDetail(SpeciesDataEntity entity) {
        return LegalStatusDetailVO.builder()
                .available(false)
                .note(UNAVAILABLE_NOTE)
                .build();
    }
}
