package com.plantky.service.component;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.plantky.common.exception.BusinessException;
import com.plantky.config.PlantIdentificationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/** ImageValidationService 对 Epic 1 允许格式和真实 file signature 的测试。 */
class ImageValidationServiceTest {

    private ImageValidationService service;

    @BeforeEach
    void setUp() {
        PlantIdentificationProperties properties = new PlantIdentificationProperties();
        properties.setMaxImageSizeMb(10);
        service = new ImageValidationService(properties);
    }

    @Test
    void shouldAcceptValidJpegSignature() {
        MockMultipartFile image = new MockMultipartFile(
                "image", "x.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x11});

        assertThatCode(() -> service.validate(image)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectExecutableContentRenamedAsJpeg() {
        MockMultipartFile image = new MockMultipartFile(
                "image", "fake.jpg", "image/jpeg",
                "MZ-not-a-jpeg".getBytes());

        assertThatThrownBy(() -> service.validate(image))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void shouldRejectUnsupportedMimeType() {
        MockMultipartFile image = new MockMultipartFile(
                "image", "x.gif", "image/gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.validate(image))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("JPG, PNG or WebP");
    }
}
