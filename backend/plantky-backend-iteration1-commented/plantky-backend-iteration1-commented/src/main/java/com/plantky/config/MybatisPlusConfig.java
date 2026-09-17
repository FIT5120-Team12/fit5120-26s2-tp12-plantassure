package com.plantky.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 扩展配置。
 *
 * <p>Iteration 1 只有简单列表查询，不需要分页插件。Iteration 2 Catalog 要求服务端分页，
 * 因此增加官方 PaginationInnerInterceptor，让 {@code selectPage} 在 MySQL 中生成真正的
 * LIMIT/OFFSET + COUNT 查询，而不是把全部数据加载到 JVM 再切片。</p>
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 插件链。
     *
     * @return 包含 MySQL 分页插件的 interceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
