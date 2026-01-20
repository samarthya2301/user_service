package com.samarthya_dev.user_service.config.properties.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Value;

@Value
@ConfigurationProperties(prefix = "secrets.jwt")
public class SecretsJwtConfig {

	String key;

}
