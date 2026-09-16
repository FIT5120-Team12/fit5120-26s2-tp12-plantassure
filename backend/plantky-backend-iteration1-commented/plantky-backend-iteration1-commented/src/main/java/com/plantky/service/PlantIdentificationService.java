package com.plantky.service;

import com.plantky.domain.vo.identification.PlantIdentificationResponse;
import org.springframework.web.multipart.MultipartFile;

/** Epic 1 AI-assisted plant identification 应用服务。 */
public interface PlantIdentificationService {

    /**
     * 校验用户图片、调用 AI、映射 PlantAssure record，并返回 possible matches。
     *
     * @param image 用户上传的植物图片
     * @return possible species matches；不会自动打开 assessment
     */
    PlantIdentificationResponse identify(MultipartFile image);
}
