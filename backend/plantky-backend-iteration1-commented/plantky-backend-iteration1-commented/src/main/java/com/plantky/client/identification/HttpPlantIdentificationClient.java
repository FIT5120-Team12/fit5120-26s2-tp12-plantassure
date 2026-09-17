package com.plantky.client.identification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.plantky.client.identification.dto.AiIdentificationCandidate;
import com.plantky.common.enums.ErrorCode;
import com.plantky.common.exception.BusinessException;
import com.plantky.config.PlantIdentificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 基于 HTTP 的 AI provider adapter。
 *
 * <p><strong>Iteration 2 / Epic 1 新增：</strong>现有需求文件没有提供 AI 团队最终的真实
 * response schema，因此所有 provider-specific 兼容逻辑集中在本类。业务层不会因为 AI
 * 团队后续调整 JSON 字段而被污染。</p>
 *
 * <p>当前 adapter 接受常见候选数组字段 candidates/results/matches/suggestions，候选 scientific
 * name 支持 scientificName/scientific_name/name，置信度支持 identificationConfidence/
 * confidence/probability/score。真实 AI 接口确定后，应在本类收紧为最终契约并补 integration test。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpPlantIdentificationClient implements PlantIdentificationClient {

    @Qualifier("plantIdentificationRestClient")
    private final RestClient restClient;
    private final PlantIdentificationProperties properties;

    @Override
    public List<AiIdentificationCandidate> identify(MultipartFile image) {

        if (!properties.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.IDENTIFICATION_SERVICE_UNAVAILABLE,
                    "Plant identification provider is not configured.");
        }

        long startedAt = System.nanoTime();

        try {
            /*
             * Epic 1 原实现：
             *
             * MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
             * bodyBuilder.part("image", toResource(image))
             *         .contentType(
             *                 resolveContentType(image.getContentType()));
             *
             * 修改原因：
             * MultipartBodyBuilder 依赖 Reactive Streams Publisher，
             * 而当前项目使用 Spring MVC + RestClient，
             * 没有必要为了 multipart 请求引入 WebFlux/reactive 依赖。
             */

            MultiValueMap<String, Object> multipartBody =
                    new LinkedMultiValueMap<>();

            HttpHeaders imageHeaders = new HttpHeaders();
            imageHeaders.setContentType(
                    resolveContentType(image.getContentType()));

            HttpEntity<ByteArrayResource> imagePart =
                    new HttpEntity<>(
                            toResource(image),
                            imageHeaders);

            multipartBody.add("image", imagePart);

            RestClient.RequestBodySpec request = restClient.post()
                    .uri(properties.getEndpoint())
                    .contentType(MediaType.MULTIPART_FORM_DATA);

            if (StringUtils.hasText(properties.getApiKey())) {
                request.header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getApiKey().trim());
            }

            JsonNode response = request
                    .body(multipartBody)
                    .retrieve()
                    .body(JsonNode.class);

            List<AiIdentificationCandidate> candidates =
                    parseCandidates(response);

            long elapsedMs =
                    (System.nanoTime() - startedAt) / 1_000_000L;

            log.info(
                    "AI identification completed. "
                            + "providerCandidates={}, elapsedMs={}",
                    candidates.size(),
                    elapsedMs);

            return candidates;

        } catch (RestClientException exception) {

            log.warn(
                    "AI identification provider request failed: {}",
                    exception.getMessage());

            throw new BusinessException(
                    ErrorCode.IDENTIFICATION_SERVICE_UNAVAILABLE,
                    "Plant identification is temporarily unavailable. "
                            + "Please try again or search for the plant by name.");

        } catch (IOException exception) {

            throw new BusinessException(
                    ErrorCode.IDENTIFICATION_FAILED,
                    "The uploaded image could not be read for identification.");
        }
    }

    private ByteArrayResource toResource(MultipartFile image) throws IOException {
        byte[] bytes = image.getBytes();
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                // 不信任/传递用户原始文件名，使用后端生成的中性文件名。
                return "plant-image" + extensionFor(image.getContentType());
            }
        };
    }

    private MediaType resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return MediaType.parseMediaType(contentType);
    }

    private String extensionFor(String contentType) {
        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".bin";
    }

    /**
     * Provider-specific JSON 适配层。
     *
     * <p>当 AI 团队正式给出固定 schema 后，只需要修改这里，不需要改 Controller/Service/VO。</p>
     */
    private List<AiIdentificationCandidate> parseCandidates(JsonNode response) {
        if (response == null || response.isNull()) {
            return List.of();
        }

        JsonNode array = response.isArray() ? response : findCandidateArray(response);
        if (array == null || !array.isArray()) {
            return List.of();
        }

        List<AiIdentificationCandidate> candidates = new ArrayList<>();
        for (JsonNode node : array) {
            String scientificName = firstText(
                    node,
                    "scientificName",
                    "scientific_name",
                    "name");

            if (!StringUtils.hasText(scientificName)) {
                String genus = firstText(node, "genus");
                String species = firstText(node, "species");
                if (StringUtils.hasText(genus) && StringUtils.hasText(species)) {
                    scientificName = genus.trim() + " " + species.trim();
                }
            }

            Double confidence = firstNumber(
                    node,
                    "identificationConfidence",
                    "confidence",
                    "probability",
                    "score");

            if (!StringUtils.hasText(scientificName) || confidence == null) {
                continue;
            }

            double normalizedConfidence = normalizeConfidence(confidence);
            if (normalizedConfidence < 0.0d || normalizedConfidence > 1.0d) {
                continue;
            }

            candidates.add(new AiIdentificationCandidate(
                    scientificName.trim(),
                    normalizedConfidence));
        }

        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        AiIdentificationCandidate::identificationConfidence).reversed())
                .toList();
    }

    private JsonNode findCandidateArray(JsonNode response) {
        for (String field : List.of("candidates", "results", "matches", "suggestions")) {
            JsonNode value = response.get(field);
            if (value != null && value.isArray()) {
                return value;
            }
        }
        return null;
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual() && StringUtils.hasText(value.asText())) {
                return value.asText();
            }
        }
        return null;
    }

    private Double firstNumber(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isNumber()) {
                return value.asDouble();
            }
            if (value.isTextual()) {
                try {
                    return Double.parseDouble(value.asText().trim());
                } catch (NumberFormatException ignored) {
                    // 尝试下一个字段；不能解析的 provider 值不应中断整个候选列表。
                }
            }
        }
        return null;
    }

    /**
     * 兼容 provider 使用 0–1 或 0–100 两种常见 probability 表达。
     * 大于 100 或负数会在调用方被视为无效候选。
     */
    private double normalizeConfidence(double confidence) {
        if (confidence > 1.0d && confidence <= 100.0d) {
            return confidence / 100.0d;
        }
        return confidence;
    }
}
