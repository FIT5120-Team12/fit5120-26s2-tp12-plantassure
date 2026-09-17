package com.plantky.controller;

import com.plantky.domain.vo.PlantAssessmentResponse;
import com.plantky.domain.vo.PlantSearchResponse;
import com.plantky.service.AssessmentOrchestrator;
import com.plantky.service.PlantSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * I1 Search + Assessment Controller。
 *
 * <p>Iteration 2 不把 Catalog/Alternatives/Compare 全塞进这个类，而是新增独立 Controller，
 * 让每个 feature 的 HTTP 接入职责保持清晰。</p>
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant", description = "Plant search and assessment APIs")
public class PlantController {

    private final PlantSearchService plantSearchService;
    private final AssessmentOrchestrator assessmentOrchestrator;

    @GetMapping("/search")
    @Operation(summary = "Search plants", description = "Search by common or scientific name.")
    public PlantSearchResponse searchPlants(
            @Parameter(description = "Common or scientific name keyword", example = "wattle")
            @RequestParam("q") String query,
            @Parameter(description = "Maximum results", example = "8")
            @Min(1) @Max(100)
            @RequestParam(value = "limit", required = false) Integer limit) {

        /*
         * Iteration 1 旧调用：
         * return plantSearchService.search(query);
         *
         * Iteration 2 增加可选 limit；不传 limit 时 Service 自动使用默认值，旧前端仍可调用。
         */
        return plantSearchService.search(query, limit);
    }

    @GetMapping("/{plantId}/assessment")
    @Operation(summary = "Get plant assessment", description = "Returns the complete verified assessment.")
    public PlantAssessmentResponse getAssessment(
            @Parameter(description = "Stable PlantAssure plantId", example = "3")
            @Positive @PathVariable Long plantId) {
        return assessmentOrchestrator.assess(plantId);
    }
}
