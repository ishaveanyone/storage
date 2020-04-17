package com.xupp.storage.storage.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class MongoStorageConfig {


    @Value("${storage.mongo.host}")
    private String host;
    @Value("${storage.mongo.port}")
    private Integer port;
    @Value("${storage.mongo.auth.username}")
    private String userName;
    @Value("${storage.mongo.auth.password}")
    private String passWord;
    @Value("${storage.mongo.auth.enable}")
    private String authEnable;
    @Value("${storage.mongo.database}")
    private String database;
    @Value("${storage.mongo.collection}")
    private String collection;
    @Value("${storage.mongo.auth.database}")
    private String authDatabase;



}
