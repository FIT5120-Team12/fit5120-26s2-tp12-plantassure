package com.plantky.service.component;

import java.util.Locale;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.OriginStatus;
import com.plantky.common.util.DisplayValueUtils;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.assessment.VictorianEstablishmentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Iteration 2 中负责把数据库原始分类文本转换为稳定领域值的组件。
 *
 * <p>该组件不查询数据库，也不做 recommendation。它只处理“已有字段代表什么”，
 * 从而让 Catalog、Alternatives、Compare 和 Assessment 使用完全相同的映射规则。</p>
 */
@Service
public class PlantClassificationService {

    /**
     * 解析 VicFlora origin status。
     *
     * <p>如果真实源值无法识别则返回 null，而不是编造 NATIVE/INTRODUCED。</p>
     */
    public OriginStatus resolveOriginStatus(SpeciesDataEntity entity) {
        String raw = entity.getEstablishmentMeans();
        if (!StringUtils.hasText(raw)) {
            return null;
        }

        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "native" -> OriginStatus.NATIVE;
            case "introduced" -> OriginStatus.INTRODUCED;
            case "uncertain" -> OriginStatus.UNCERTAIN;
            default -> null;
        };
    }

    /** 根据 2022 Advisory List 原始值返回 Iteration 2 environmental concern。 */
    public EnvironmentalConcern resolveEnvironmentalConcern(SpeciesDataEntity entity) {
        return EnvironmentalConcern.fromDatabaseValue(entity.getRiskRating());
    }

    /**
     * 构建 Victorian establishment block。
     *
     * <p>完整 allowed enum 尚未被项目资料定义，因此 status 直接由真实源文本转成 uppercase，
     * 不创建额外分类。</p>
     */
    public VictorianEstablishmentVO buildVictorianEstablishment(SpeciesDataEntity entity) {
        String raw = entity.getDegreeOfEstablishment();
        if (!StringUtils.hasText(raw) || "Not available".equalsIgnoreCase(raw.trim())) {
            return VictorianEstablishmentVO.builder()
                    .status(null)
                    .label(null)
                    .build();
        }

        String trimmed = raw.trim();
        return VictorianEstablishmentVO.builder()
                .status(trimmed.toUpperCase(Locale.ROOT).replace(' ', '_'))
                .label(DisplayValueUtils.capitalizeFirst(trimmed))
                .build();
    }
}
