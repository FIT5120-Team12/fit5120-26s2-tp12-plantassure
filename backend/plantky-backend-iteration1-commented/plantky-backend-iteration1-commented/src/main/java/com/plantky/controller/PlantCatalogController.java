package com.plantky.controller;

import java.util.List;

import com.plantky.common.enums.EnvironmentalConcern;
import com.plantky.common.enums.OriginStatus;
import com.plantky.domain.query.PlantCatalogQuery;
import com.plantky.domain.vo.catalog.PlantCatalogResponse;
import com.plantky.service.PlantCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Iteration 2 Epic 3 Catalog / Browse Controller。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Catalog", description = "Iteration 2 assessed plant catalog")
public class PlantCatalogController {

    private final PlantCatalogService plantCatalogService;

    @GetMapping
    @Operation(summary = "Browse assessed plants")
    public PlantCatalogResponse browse(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<EnvironmentalConcern> environmentalConcern,
            @RequestParam(required = false) List<OriginStatus> originStatus,
            @RequestParam(required = false) String growthForm,
            @RequestParam(required = false) String lifeHistory,
            @RequestParam(required = false) String woodiness,
            @RequestParam(required = false) Double minHeight,
            @RequestParam(required = false) Double maxHeight,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {

        PlantCatalogQuery query = PlantCatalogQuery.builder()
                .q(q)
                .environmentalConcern(environmentalConcern)
                .originStatus(originStatus)
                .growthForm(growthForm)
                .lifeHistory(lifeHistory)
                .woodiness(woodiness)
                .minHeight(minHeight)
                .maxHeight(maxHeight)
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        return plantCatalogService.browse(query);
    }
}
