package com.plantky.service.component;

import com.plantky.common.util.DisplayValueUtils;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.PlantIdentityVO;
import org.springframework.stereotype.Service;

/** Plant Identity / Establishment 数据转换组件。 */
@Service
public class PlantIdentityService {

    public PlantIdentityVO build(SpeciesDataEntity entity) {
        return PlantIdentityVO.builder()
                .plantId(entity.getId())
                .scientificName(entity.getScientificName())
                .commonName(entity.getVernacularName())
                .family(entity.getFamily())
                // Iteration 2 数据暂时没有 verified image URL；明确返回 null，不构造假地址。
                .imageUrl(null)
                .establishmentMeans(DisplayValueUtils.capitalizeFirst(entity.getEstablishmentMeans()))
                .degreeOfEstablishment(normalizeDegree(entity.getDegreeOfEstablishment()))
                .build();
    }

    /**
     * 数据中的 "Not available" 属于展示占位符，不应作为真实领域值传给 API。
     */
    private String normalizeDegree(String value) {
        if (value == null || value.isBlank() || "Not available".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return DisplayValueUtils.capitalizeFirst(value);
    }
}
