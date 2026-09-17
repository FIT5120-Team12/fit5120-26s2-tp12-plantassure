package com.plantky.service.component;

import java.io.IOException;
import java.util.Set;

import com.plantky.common.enums.ErrorCode;
import com.plantky.common.exception.BusinessException;
import com.plantky.config.PlantIdentificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Epic 1 上传图片安全/格式校验组件。
 *
 * <p>不仅检查客户端声明的 MIME type，还检查文件 signature，防止把任意文件仅改后缀后上传。</p>
 */
@Service
@RequiredArgsConstructor
public class ImageValidationService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp");

    private final PlantIdentificationProperties properties;

    public void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            invalid("Please upload a plant image.");
        }

        long maxBytes = properties.getMaxImageSizeMb() * 1024L * 1024L;
        if (image.getSize() > maxBytes) {
            invalid("Image size must not exceed " + properties.getMaxImageSizeMb() + " MB.");
        }

        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType)
                || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            invalid("Please upload a valid JPG, PNG or WebP image.");
        }

        try {
            byte[] bytes = image.getBytes();
            if (!matchesSignature(contentType, bytes)) {
                invalid("The file content does not match the declared image format.");
            }
        } catch (IOException exception) {
            invalid("The uploaded image could not be read.");
        }
    }

    private boolean matchesSignature(String contentType, byte[] bytes) {
        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)) {
            return bytes.length >= 3
                    && unsigned(bytes[0]) == 0xFF
                    && unsigned(bytes[1]) == 0xD8
                    && unsigned(bytes[2]) == 0xFF;
        }

        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)) {
            int[] signature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            if (bytes.length < signature.length) {
                return false;
            }
            for (int i = 0; i < signature.length; i++) {
                if (unsigned(bytes[i]) != signature[i]) {
                    return false;
                }
            }
            return true;
        }

        if ("image/webp".equalsIgnoreCase(contentType)) {
            return bytes.length >= 12
                    && ascii(bytes, 0, "RIFF")
                    && ascii(bytes, 8, "WEBP");
        }

        return false;
    }

    private boolean ascii(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            if ((char) bytes[offset + i] != expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private void invalid(String message) {
        throw new BusinessException(ErrorCode.INVALID_IMAGE, message);
    }
}
