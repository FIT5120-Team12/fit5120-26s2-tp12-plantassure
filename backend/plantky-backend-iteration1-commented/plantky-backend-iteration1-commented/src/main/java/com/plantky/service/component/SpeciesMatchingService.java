package com.plantky.service.component;

import java.util.List;
import java.util.Locale;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.mapper.SpeciesDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI scientific name → PlantAssure record 的受控匹配组件。
 *
 * <p>当前数据库没有经过验证的 synonym table，因此本实现不会用 fuzzy/LIKE 距离强行猜物种。</p>
 *
 * <p>匹配顺序：</p>
 * <ol>
 *     <li>accepted scientific_name exact match；</li>
 *     <li>标准化 species-level match_key，并且结果必须唯一；</li>
 *     <li>否则明确返回 no PlantAssure match。</li>
 * </ol>
 *
 * <p>未来获得 verified synonym dataset 后，可在 exact 与 match_key 之间加入 synonym lookup。</p>
 */
@Service
@RequiredArgsConstructor
public class SpeciesMatchingService {

    private final SpeciesDataMapper speciesDataMapper;

    public SpeciesDataEntity findPlantAssureRecord(String scientificName) {
        if (!StringUtils.hasText(scientificName)) {
            return null;
        }

        String normalizedInput = scientificName.trim();

        LambdaQueryWrapper<SpeciesDataEntity> exactWrapper = new LambdaQueryWrapper<>();
        exactWrapper.eq(SpeciesDataEntity::getScientificName, normalizedInput)
                .last("LIMIT 2");
        List<SpeciesDataEntity> exact = speciesDataMapper.selectList(exactWrapper);
        if (exact.size() == 1) {
            return exact.get(0);
        }

        String matchKey = toSpeciesLevelMatchKey(normalizedInput);
        if (!StringUtils.hasText(matchKey)) {
            return null;
        }

        LambdaQueryWrapper<SpeciesDataEntity> keyWrapper = new LambdaQueryWrapper<>();
        keyWrapper.eq(SpeciesDataEntity::getMatchKey, matchKey)
                .last("LIMIT 2");
        List<SpeciesDataEntity> byKey = speciesDataMapper.selectList(keyWrapper);

        // I2 中 match_key 可能对应多个 subspecies；有歧义时不能擅自挑一条。
        return byKey.size() == 1 ? byKey.get(0) : null;
    }

    /**
     * 将 scientific name 规整为 genus + species 级别的 match key。
     * 仅做确定性的空白/大小写规范化，不做模糊拼写纠错。
     */
    String toSpeciesLevelMatchKey(String scientificName) {
        if (!StringUtils.hasText(scientificName)) {
            return null;
        }

        String compact = scientificName.trim().replaceAll("\\s+", " ");
        String[] parts = compact.split(" ");
        if (parts.length < 2) {
            return compact.toLowerCase(Locale.ROOT);
        }
        return (parts[0] + " " + parts[1]).toLowerCase(Locale.ROOT);
    }
}
