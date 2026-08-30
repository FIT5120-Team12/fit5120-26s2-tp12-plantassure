package com.plantky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Plantky 后端应用启动类。
 *
 * <p>该类位于根包 {@code com.plantky} 下，因此 Spring Boot 默认会从当前包开始，
 * 递归扫描所有子包中的 Spring Bean，例如：</p>
 *
 * <ul>
 *     <li>{@code controller}：REST Controller</li>
 *     <li>{@code service}：业务服务</li>
 *     <li>{@code mapper}：MyBatis Mapper</li>
 *     <li>{@code config}：Spring 配置类</li>
 *     <li>{@code common.handler}：全局异常处理器</li>
 * </ul>
 *
 * <p>{@link SpringBootApplication} 是一个组合注解，核心包含：</p>
 * <ul>
 *     <li>{@code @Configuration}：声明当前类可以提供 Spring 配置。</li>
 *     <li>{@code @EnableAutoConfiguration}：启用 Spring Boot 自动配置。</li>
 *     <li>{@code @ComponentScan}：扫描当前包及其子包中的组件。</li>
 * </ul>
 */
@SpringBootApplication
public class PlantkyApplication {

    /**
     * Java 程序入口。
     *
     * <p>{@link SpringApplication#run(Class, String...)} 会完成以下主要工作：</p>
     * <ol>
     *     <li>创建并启动 Spring IoC 容器；</li>
     *     <li>加载 application.yml 及当前 profile 的配置；</li>
     *     <li>创建 DataSource、MyBatis-Plus、Spring MVC 等基础设施；</li>
     *     <li>启动内嵌 Web Server（默认 Tomcat）；</li>
     *     <li>扫描并注册 Controller、Service、Mapper 等 Bean。</li>
     * </ol>
     *
     * @param args JVM 启动参数，例如可以通过命令行覆盖 Spring 配置
     */
    public static void main(String[] args) {
        SpringApplication.run(PlantkyApplication.class, args);
    }
}
