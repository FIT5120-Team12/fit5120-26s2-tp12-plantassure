package com.plantky.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.plantky.client.identification.PlantIdentificationClient;
import com.plantky.client.identification.dto.AiIdentificationCandidate;
import com.plantky.common.enums.IdentificationStatus;
import com.plantky.config.PlantIdentificationProperties;
import com.plantky.domain.entity.SpeciesDataEntity;
import com.plantky.domain.vo.identification.PlantIdentificationResponse;
import com.plantky.service.component.ImageValidationService;
import com.plantky.service.component.SpeciesMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/** Epic 1 business orchestration tests。 */
class PlantIdentificationServiceImplTest {

    private ImageValidationService imageValidationService;
    private PlantIdentificationClient client;
    private SpeciesMatchingService speciesMatchingService;
    private PlantIdentificationServiceImpl service;

    @BeforeEach
    void setUp() {
        PlantIdentificationProperties properties = new PlantIdentificationProperties();
        properties.setTopCandidates(3);
        properties.setMinimumConfidence(0.0d);

        imageValidationService = mock(ImageValidationService.class);
        client = mock(PlantIdentificationClient.class);
        speciesMatchingService = mock(SpeciesMatchingService.class);
        service = new PlantIdentificationServiceImpl(
                imageValidationService,
                client,
                speciesMatchingService,
                properties);
    }

    @Test
    void shouldReturnTopThreeCandidatesAndNeverAutoConfirmFirstCandidate() {
        when(client.identify(any(MultipartFile.class))).thenReturn(List.of(
                new AiIdentificationCandidate("Species D", 0.40d),
                new AiIdentificationCandidate("Species A", 0.95d),
                new AiIdentificationCandidate("Species B", 0.80d),
                new AiIdentificationCandidate("Species C", 0.60d)));

        SpeciesDataEntity matched = new SpeciesDataEntity();
        matched.setId(100L);
        matched.setScientificName("Species A");
        matched.setVernacularName("Plant A");
        when(speciesMatchingService.findPlantAssureRecord("Species A")).thenReturn(matched);

        PlantIdentificationResponse response = service.identify(mock(MultipartFile.class));

        assertThat(response.getStatus()).isEqualTo(IdentificationStatus.MATCHES_FOUND);
        assertThat(response.getMatches()).hasSize(3);
        assertThat(response.getMatches().get(0).getScientificName()).isEqualTo("Species A");
        assertThat(response.getMatches().get(0).isPlantAssureMatch()).isTrue();
        assertThat(response.getMatches().get(1).getScientificName()).isEqualTo("Species B");
        assertThat(response.getMatches().get(2).getScientificName()).isEqualTo("Species C");
    }

    @Test
    void shouldReturnNoConfidentMatchWhenProviderReturnsNoCandidates() {
        when(client.identify(any(MultipartFile.class))).thenReturn(List.of());

        PlantIdentificationResponse response = service.identify(mock(MultipartFile.class));

        assertThat(response.getStatus()).isEqualTo(IdentificationStatus.NO_CONFIDENT_MATCH);
        assertThat(response.getMatches()).isEmpty();
    }

    @Test
    void shouldKeepUnmappedAiCandidateInsteadOfInventingAssessmentRecord() {
        when(client.identify(any(MultipartFile.class))).thenReturn(List.of(
                new AiIdentificationCandidate("Unknown species", 0.81d)));
        when(speciesMatchingService.findPlantAssureRecord("Unknown species")).thenReturn(null);

        PlantIdentificationResponse response = service.identify(mock(MultipartFile.class));

        assertThat(response.getMatches()).singleElement().satisfies(match -> {
            assertThat(match.getPlantId()).isNull();
            assertThat(match.getCommonName()).isNull();
            assertThat(match.isPlantAssureMatch()).isFalse();
            assertThat(match.getIdentificationConfidence()).isEqualTo(0.81d);
        });
    }
}
