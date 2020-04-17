package com.xupp.storage.storage.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class OssStorageConfig {
    @Value("${storage.oss.endpoint}")
    private String endpoint;
    @Value("${storage.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${storage.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${storage.oss.bucketName}")
    private String bucketName;
}
