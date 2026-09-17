package com.plantky.service.impl;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.plantky.client.identification.PlantIdentificationClient;
import com.plantky.client.identification.dto.AiIdentificationCandidate;
import com.plantky.common.enums.IdentificationStatus;
import com.plantky.config.PlantIdentificationProperties;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.identification.IdentificationMatchVO;
import com.plantky.domain.vo.identification.PlantIdentificationResponse;
import com.plantky.service.PlantIdentificationService;
import com.plantky.service.component.ImageValidationService;
import com.plantky.service.component.SpeciesMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Epic 1 AI-assisted identification 核心编排服务。
 *
 * <p><strong>关键业务边界：</strong>AI 只提供 possible species matches，不提供 environmental
 * assessment。后端不会自动确认第一名，也不会在该 Service 中调用 AssessmentOrchestrator。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlantIdentificationServiceImpl implements PlantIdentificationService {

    private final ImageValidationService imageValidationService;
    private final PlantIdentificationClient plantIdentificationClient;
    private final SpeciesMatchingService speciesMatchingService;
    private final PlantIdentificationProperties properties;

    @Override
    public PlantIdentificationResponse identify(MultipartFile image) {
        imageValidationService.validate(image);
        log.info("Identification request received. contentType={}, sizeBytes={}",
                image.getContentType(), image.getSize());

        List<AiIdentificationCandidate> providerCandidates =
                plantIdentificationClient.identify(image);

        double minimumConfidence = Math.max(0.0d, properties.getMinimumConfidence());
        int topCandidates = Math.max(1, properties.getTopCandidates());

        /*
         * Epic 1 要求 possible matches，而不是自动决定“正确植物”。
         * 因此这里只排序 + 截取 Top N，绝不会把 rank #1 自动传给 Assessment。
         */
        List<AiIdentificationCandidate> filtered = providerCandidates.stream()
                .filter(candidate -> candidate != null)
                .filter(candidate -> candidate.identificationConfidence() >= minimumConfidence)
                .sorted(Comparator.comparingDouble(
                        AiIdentificationCandidate::identificationConfidence).reversed())
                .toList();

        // 同一 scientific name 如果 provider 重复返回，只保留置信度最高的一项。
        Map<String, AiIdentificationCandidate> uniqueByName = new LinkedHashMap<>();
        for (AiIdentificationCandidate candidate : filtered) {
            String key = candidate.scientificName().trim().toLowerCase();
            uniqueByName.putIfAbsent(key, candidate);
            if (uniqueByName.size() >= topCandidates) {
                break;
            }
        }

        List<IdentificationMatchVO> matches = uniqueByName.values().stream()
                .map(this::toMatch)
                .toList();

        long plantAssureMatches = matches.stream()
                .filter(IdentificationMatchVO::isPlantAssureMatch)
                .count();
        log.info("Identification response prepared. candidates={}, plantAssureMatches={}",
                matches.size(), plantAssureMatches);

        return PlantIdentificationResponse.builder()
                .status(matches.isEmpty()
                        ? IdentificationStatus.NO_CONFIDENT_MATCH
                        : IdentificationStatus.MATCHES_FOUND)
                .matches(matches)
                .build();
    }

    private IdentificationMatchVO toMatch(AiIdentificationCandidate candidate) {
        SpeciesDataEntity matched =
                speciesMatchingService.findPlantAssureRecord(candidate.scientificName());

        if (matched == null) {
            return IdentificationMatchVO.builder()
                    .plantId(null)
                    .commonName(null)
                    .scientificName(candidate.scientificName())
                    .identificationConfidence(candidate.identificationConfidence())
                    .plantAssureMatch(false)
                    .build();
        }

        return IdentificationMatchVO.builder()
                .plantId(matched.getId())
                .commonName(matched.getVernacularName())
                .scientificName(matched.getScientificName())
                .identificationConfidence(candidate.identificationConfidence())
                .plantAssureMatch(true)
                .build();
    }
}
