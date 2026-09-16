package com.plantky.domain.vo.identification;

import java.util.List;

import com.plantky.common.enums.IdentificationStatus;
import lombok.Builder;
import lombok.Getter;

/** Epic 1 图片识别成功 HTTP 响应。 */
@Getter
@Builder
public class PlantIdentificationResponse {

    /** 明确区分有候选和“正常完成但没有可信候选”的情况。 */
    private final IdentificationStatus status;

    /** 最多返回配置的 Top N possible matches；空结果必须为 [] 而不是 null。 */
    private final List<IdentificationMatchVO> matches;
}
