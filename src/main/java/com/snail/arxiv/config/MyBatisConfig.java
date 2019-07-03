package com.snail.arxiv.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {
    // 自定义MyBatis相关配置

    public ConfigurationCustomizer configurationCustomizer(){
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                // 设置自动适用驼峰命名法规则
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }
}
