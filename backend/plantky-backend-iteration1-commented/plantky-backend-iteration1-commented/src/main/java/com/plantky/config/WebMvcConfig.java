package com.plantky.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 全局配置。
 *
 * <p>当前 Iteration 1 主要配置 CORS，使 Vue 前端可以从不同端口/域名访问 Spring Boot API。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 通过构造器注入读取到的 CORS 配置。
     *
     * <p>{@code @RequiredArgsConstructor} 会为 final 字段生成构造器，Spring 自动使用该构造器完成依赖注入。</p>
     */
    private final CorsProperties corsProperties;

    /**
     * 注册全局 CORS 规则。
     *
     * @param registry Spring MVC 提供的 CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 只允许配置文件明确列出的前端 Origin，生产环境不要直接写 "*"。
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                // Iteration 1 只有只读查询接口，因此目前只允许 GET。
                .allowedMethods("GET")
                // 允许前端携带普通请求头；未来如果接入认证，可以再精细化限制。
                .allowedHeaders("*")
                // 当前项目无 Cookie/Session 登录，因此不允许跨域携带 credentials。
                .allowCredentials(false)
                // 浏览器可缓存预检请求 3600 秒，减少重复 OPTIONS 请求。
                .maxAge(3600);
    }
}
