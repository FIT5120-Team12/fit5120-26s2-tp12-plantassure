package com.plantky.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.plantky.common.enums.IdentificationStatus;
import com.plantky.common.handler.GlobalExceptionHandler;
import com.plantky.domain.vo.identification.IdentificationMatchVO;
import com.plantky.domain.vo.identification.PlantIdentificationResponse;
import com.plantky.service.PlantIdentificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

/** Epic 1 Identification Controller API contract tests。 */
class PlantIdentificationControllerTest {

    private PlantIdentificationService plantIdentificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        plantIdentificationService = mock(PlantIdentificationService.class);
        PlantIdentificationController controller =
                new PlantIdentificationController(plantIdentificationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnPossibleMatchesWithoutOpeningAssessment() throws Exception {
        when(plantIdentificationService.identify(any(MultipartFile.class)))
                .thenReturn(PlantIdentificationResponse.builder()
                        .status(IdentificationStatus.MATCHES_FOUND)
                        .matches(List.of(
                                IdentificationMatchVO.builder()
                                        .plantId(3L)
                                        .commonName("Cootamundra Wattle")
                                        .scientificName("Acacia baileyana")
                                        .identificationConfidence(0.92d)
                                        .plantAssureMatch(true)
                                        .build()))
                        .build());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "plant.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});

        mockMvc.perform(multipart("/api/v1/plants/identify").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MATCHES_FOUND"))
                .andExpect(jsonPath("$.matches[0].plantId").value(3))
                .andExpect(jsonPath("$.matches[0].identificationConfidence").value(0.92))
                .andExpect(jsonPath("$.matches[0].plantAssureMatch").value(true))
                // Epic 1 identification response 不允许混入 assessment/risk 数据。
                .andExpect(jsonPath("$.matches[0].environmentalConcern").doesNotExist());
    }

    @Test
    void shouldReturnInvalidImageWhenMultipartImagePartIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/plants/identify"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
    }
}
