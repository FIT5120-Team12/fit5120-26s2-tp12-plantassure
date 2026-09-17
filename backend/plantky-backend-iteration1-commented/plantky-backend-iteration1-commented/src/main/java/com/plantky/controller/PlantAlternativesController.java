package com.plantky.controller;

import com.plantky.domain.vo.alternatives.PlantAlternativesResponse;
import com.plantky.service.PlantAlternativeService;
import io.swagger.v3.oas.annotations.Operation;
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

/** Iteration 2 Epic 2.1 Find a Better Plant Controller。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Alternatives", description = "Iteration 2 lower-concern alternatives")
public class PlantAlternativesController {

    private final PlantAlternativeService plantAlternativeService;

    @GetMapping("/{plantId}/alternatives")
    @Operation(summary = "Find lower-concern alternatives")
    public PlantAlternativesResponse findAlternatives(
            @Positive @PathVariable Long plantId,
            @Min(1) @Max(20)
            @RequestParam(value = "limit", required = false) Integer limit) {
        return plantAlternativeService.findAlternatives(plantId, limit);
    }
}
