package com.plantky.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Epic 1 外部 AI plant-identification 服务配置。
 *
 * <p>真实 provider 的 URL/API key 不写死在 Java 中，部署时通过环境变量注入。
 * 这样 AI 团队替换模型或服务地址时，PlantAssure 业务层无需重写。</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "plantky.identification")
public class PlantIdentificationProperties {

    /** 是否启用真实 AI provider。未配置 provider 时保持 false。 */
    private boolean enabled = false;

    /** AI provider base URL，例如 http://localhost:8000。 */
    private String baseUrl = "http://localhost:8000";

    /** provider 图片识别 endpoint。 */
    private String endpoint = "/identify";

    /** 可选 Bearer API key；为空时不会发送 Authorization header。 */
    private String apiKey;

    /** Epic/开发文档要求的最大图片大小，默认 10 MB。 */
    private int maxImageSizeMb = 10;

    /** 默认只向前端返回置信度最高的 3 个 possible matches。 */
    private int topCandidates = 3;

    /**
     * 最低识别置信度。
     *
     * <p>现有需求没有规定具体阈值，因此默认 0.0（不过滤 provider 返回结果）。
     * AI 团队完成模型评估后可通过环境变量配置真实阈值，而不是在代码中拍脑袋写死。</p>
     */
    private double minimumConfidence = 0.0d;

    /** 建立 AI HTTP 连接的最大等待时间。 */
    private Duration connectTimeout = Duration.ofSeconds(200);

    /** 等待 AI inference 返回的最大时间。 */
    private Duration readTimeout = Duration.ofSeconds(200);
}
