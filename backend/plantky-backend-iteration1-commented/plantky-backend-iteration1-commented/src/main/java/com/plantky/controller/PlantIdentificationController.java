package com.plantky.controller;

import com.plantky.domain.vo.identification.PlantIdentificationResponse;
import com.plantky.service.PlantIdentificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Epic 1 — AI-Assisted Plant Identification HTTP Controller。
 *
 * <p>Controller 只接收 multipart image、调用 Service、返回 DTO；不放 AI/数据库匹配业务逻辑。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Identification", description = "AI-assisted possible species identification")
public class PlantIdentificationController {

    private final PlantIdentificationService plantIdentificationService;

    @PostMapping(
            value = "/identify",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Identify possible plant species from an image",
            description = "Returns possible AI species matches. It never opens or generates an environmental assessment.")
    public PlantIdentificationResponse identifyPlant(
            @RequestPart("image") MultipartFile image) {
        return plantIdentificationService.identify(image);
    }
}
