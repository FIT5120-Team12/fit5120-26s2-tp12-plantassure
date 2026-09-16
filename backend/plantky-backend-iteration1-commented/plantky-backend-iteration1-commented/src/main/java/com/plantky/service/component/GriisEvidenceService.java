package com.plantky.service.component;

import com.plantky.common.constant.DataSourceConstants;
import com.plantky.common.enums.OriginStatus;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.assessment.GriisSupplementaryEvidenceVO;
import org.springframework.stereotype.Service;

/**
 * GRIIS supplementary evidence 组件。
 *
 * <p>GRIIS 不是 2022 Advisory List 的替代物，因此其结果不会修改 environmental concern
 * 或 recommendation。根据当前数据设计，仅对 VicFlora 判定为 INTRODUCED 的植物暴露。</p>
 */
@Service
public class GriisEvidenceService {

    private final PlantClassificationService plantClassificationService;

    public GriisEvidenceService(PlantClassificationService plantClassificationService) {
        this.plantClassificationService = plantClassificationService;
    }

    /**
     * @return introduced 植物的 GRIIS 证据；其他 origin 返回 null
     */
    public GriisSupplementaryEvidenceVO build(SpeciesDataEntity entity) {
        OriginStatus originStatus = plantClassificationService.resolveOriginStatus(entity);
        if (originStatus != OriginStatus.INTRODUCED) {
            return null;
        }

        return GriisSupplementaryEvidenceVO.builder()
                .listed(entity.getGriisListed())
                .invasive(entity.getGriisIsInvasive())
                .source(DataSourceConstants.GRIIS)
                .build();
    }
}
