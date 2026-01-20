package com.samarthya_dev.user_service.config.properties.common;

import lombok.Value;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "service")
public class ServiceConfig {

    Otp otp;
    Token token;

    @Value
    public static class Otp {

        Email email;
        Creator creator;

        @Value
        public static class Email {

            String sendEmailSource;

        }

        @Value
        public static class Creator {

            Duration expireAfter;

        }

    }

    @Value
    public static class Token {

        Jwt jwt;
        Refresh refresh;

        @Value
        public static class Jwt {

            Claim claim;
            Duration expireAfter;

            @Value
            public static class Claim {

                Key key;

                @Value
                public static class Key {

                    String userId;
                    String userEmail;
                    String userRole;

                }

            }

        }

        @Value
        public static class Refresh {

            Duration expireAfter;

        }

    }

}
