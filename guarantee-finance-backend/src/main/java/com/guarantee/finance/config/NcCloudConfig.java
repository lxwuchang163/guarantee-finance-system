package com.guarantee.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nc.cloud")
public class NcCloudConfig {
    private String baseUrl;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String dataSource; // 数据源编码
    private String groupCode; // 集团编码
    private Integer connectTimeout = 10000;
    private Integer readTimeout = 30000;
    private Boolean enabled = true;
}
