package com.plantky.client.identification;

import java.util.List;

import com.plantky.client.identification.dto.AiIdentificationCandidate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 外部 AI plant-identification provider 抽象。
 *
 * <p>业务 Service 只依赖该接口，不直接依赖某个 Python/FastAPI/第三方 provider。
 * 将来 AI 服务更换时，只需要替换 Client adapter。</p>
 */
public interface PlantIdentificationClient {

    /**
     * 把用户图片发送给 AI provider，并返回 provider 产生的候选。
     *
     * @param image 已经过后端基本安全校验的图片
     * @return AI 候选；无法识别时返回空集合而不是 null
     */
    List<AiIdentificationCandidate> identify(MultipartFile image);
}
