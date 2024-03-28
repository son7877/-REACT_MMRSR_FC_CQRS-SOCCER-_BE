package com.example.learnMongoRedis.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationPropertiesConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean(name = "jwtSecretKey")
    public String getSecretKey() {
        return secretKey;
    }

}