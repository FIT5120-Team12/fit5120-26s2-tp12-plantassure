package com.plantky.service.component;

import com.plantky.common.util.DisplayValueUtils;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.assessment.PlantTraitsVO;
import org.springframework.stereotype.Service;

/**
 * AusTraits 数据转换组件。
 *
 * <p>这里只把数据库中真实存在的 trait 转成 API 字段。缺失值保持 null，
 * 不允许根据 family、common name 或其他 trait 去猜测。</p>
 */
@Service
public class TraitService {

    /** 构建 Assessment traits block。 */
    public PlantTraitsVO build(SpeciesDataEntity entity) {
        return PlantTraitsVO.builder()
                .growthForm(entity.getGrowthForm())
                .woodiness(entity.getWoodiness())
                .lifeHistory(entity.getLifeHistory())
                .heightMinM(entity.getHeightMin())
                .heightMaxM(entity.getHeightMax())
                .height(DisplayValueUtils.formatHeightRange(entity.getHeightMin(), entity.getHeightMax()))
                .build();
    }

    /** 复用统一高度展示规则。 */
    public String formatHeight(SpeciesDataEntity entity) {
        return DisplayValueUtils.formatHeightRange(entity.getHeightMin(), entity.getHeightMax());
    }
}
