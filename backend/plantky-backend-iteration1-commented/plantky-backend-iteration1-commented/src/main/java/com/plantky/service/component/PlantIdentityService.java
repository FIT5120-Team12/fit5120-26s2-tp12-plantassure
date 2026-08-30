package com.plantky.service.component;

import com.plantky.common.util.DisplayValueUtils;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.PlantIdentityVO;
import org.springframework.stereotype.Service;

/**
 * Plant Identity / Establishment 数据转换组件。
 *
 * <p>该组件职责非常单一：把数据库 Entity 中与植物身份相关的字段转换为 API VO。
 * 它不查数据库、不计算风险，也不生成 Recommendation。</p>
 */
@Service
public class PlantIdentityService {

    /**
     * 根据数据库实体构建 PlantIdentityVO。
     *
     * @param entity 已经由 Mapper 查询到的植物实体
     * @return 前端可直接使用的 identity/establishment 数据
     */
    public PlantIdentityVO build(SpeciesDataEntity entity) {
        return PlantIdentityVO.builder()
                // 数据库 id 对外统一命名为 plantId。
                .plantId(entity.getId())
                .scientificName(entity.getScientificName())
                // 数据库字段叫 vernacularName，API Contract 使用 commonName。
                .commonName(entity.getVernacularName())
                .family(entity.getFamily())
                // 数据库可能保存 introduced，API 转成更适合展示的 Introduced。
                .establishmentMeans(DisplayValueUtils.capitalizeFirst(entity.getEstablishmentMeans()))
                .degreeOfEstablishment(DisplayValueUtils.capitalizeFirst(entity.getDegreeOfEstablishment()))
                .build();
    }
}
