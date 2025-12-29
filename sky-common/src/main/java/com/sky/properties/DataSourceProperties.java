package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
// 指定当前类为配置类
@ConfigurationProperties(prefix = "sky.datasource")
@Data
public class DataSourceProperties {
    private String driverClassName;
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
}
