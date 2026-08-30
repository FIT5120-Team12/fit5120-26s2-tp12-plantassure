package com.plantky.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Plantky CORS 配置属性对象。
 *
 * <p>{@code @ConfigurationProperties(prefix = "plantky.cors")} 会把 YAML 中：</p>
 * <pre>
 * plantky:
 *   cors:
 *     allowed-origins:
 *       - http://localhost:5173
 * </pre>
 * <p>自动绑定到 {@link #allowedOrigins}。</p>
 *
 * <p>把 CORS 地址放在配置文件而不是写死在 Java 代码中，可以让 local/prod 环境使用不同前端域名。</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "plantky.cors")
public class CorsProperties {

    /**
     * 允许访问后端 API 的前端 Origin 列表。
     *
     * <p>初始化为空 ArrayList，避免配置缺失时出现 null。</p>
     */
    private List<String> allowedOrigins = new ArrayList<>();
}
