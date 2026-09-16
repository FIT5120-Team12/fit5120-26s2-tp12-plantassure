package com.plantky.client.identification.dto;

/**
 * AI provider → PlantAssure Backend 的内部候选模型。
 *
 * <p>该 DTO 不直接暴露给前端。provider-specific JSON 会先由 Client adapter 转换成
 * 这个稳定结构，业务层只依赖 scientificName + identificationConfidence。</p>
 *
 * @param scientificName AI 判断的 scientific name
 * @param identificationConfidence 图片识别置信度，标准化为 0.0–1.0
 */
public record AiIdentificationCandidate(
        String scientificName,
        double identificationConfidence) {
}
