package io.renren.config;

import io.renren.modules.tencent.Credential;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfig {

    @Value("${sms.secretId}")
    private String secretId;

    @Value("${sms.secretKey}")
    private String secretKey;

    @Bean
    public Credential credential() {
        return new Credential(secretId, secretKey);
    }

}