package com.example.edukg_backend.ConfigHelper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "data")
@PropertySource("classpath:config/DefaultEntity.properties")
public class DefaultEntityConfig {

    /**
     * data.person.name
     * 这里map名需要和application.properties中的参数一致
     */
    private List<Map<String, String>> defaultEntity = new ArrayList<>();



    /**
     * 编写get，set方法方便使用
     */
    public List<Map<String, String>> getDefaultEntity() {
        return defaultEntity;
    }

    public void setDefaultEntity(List<Map<String, String>> d) {
        this.defaultEntity = d;
    }
}
