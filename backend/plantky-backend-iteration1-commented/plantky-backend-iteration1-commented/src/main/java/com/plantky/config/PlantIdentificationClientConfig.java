package com.plantky.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Epic 1 AI HTTP Client 基础设施配置。 */
@Configuration
public class PlantIdentificationClientConfig {

    /**
     * 为 AI provider 创建独立 RestClient。
     *
     * <p>AI inference 属于外部网络依赖，必须有连接/读取 timeout，避免请求无限占用
     * Tomcat worker thread。这里只配置 transport，provider JSON 解析由 Client adapter 负责。</p>
     */
    @Bean("plantIdentificationRestClient")
    public RestClient plantIdentificationRestClient(
            RestClient.Builder builder,
            PlantIdentificationProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(toMillis(properties.getConnectTimeout()));
        requestFactory.setReadTimeout(toMillis(properties.getReadTimeout()));

        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    private int toMillis(Duration duration) {
        long millis = duration == null ? 0L : duration.toMillis();
        if (millis <= 0L) {
            return 1;
        }
        return (int) Math.min(millis, Integer.MAX_VALUE);
    }
}
