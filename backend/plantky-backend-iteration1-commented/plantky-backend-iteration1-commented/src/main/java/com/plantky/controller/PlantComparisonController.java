package com.plantky.controller;

import com.plantky.domain.vo.compare.PlantComparisonResponse;
import com.plantky.service.PlantComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Iteration 2 Epic 2.2 Compare Controller。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Compare", description = "Iteration 2 side-by-side plant comparison")
public class PlantComparisonController {

    private final PlantComparisonService plantComparisonService;

    @GetMapping("/compare")
    @Operation(summary = "Compare 2 to 3 plants")
    public PlantComparisonResponse compare(@RequestParam("plantIds") String plantIds) {
        return plantComparisonService.compare(plantIds);
    }
}
