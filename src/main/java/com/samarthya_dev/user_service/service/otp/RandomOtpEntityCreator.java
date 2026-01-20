package com.samarthya_dev.user_service.service.otp;

import java.time.Instant;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.samarthya_dev.user_service.config.properties.common.ServiceConfig;
import com.samarthya_dev.user_service.entity.otp.OtpEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableConfigurationProperties(ServiceConfig.class)
@RequiredArgsConstructor
public class RandomOtpEntityCreator {

	private final ServiceConfig serviceConfig;

	public OtpEntity create(UserEntity userEntity) {

		log.info("Creating Otp Entity for User Entity");

		OtpEntity otpEntity = OtpEntity
			.builder()
			.user(userEntity)
			.otpCode(RandomUtils.secure().randomInt(100000, 1000000))
			.createdTimestamp(Instant.now())
			.expiresTimestamp(Instant.now().plus(serviceConfig.getOtp().getExpireAfter()))
			.consumed(Boolean.FALSE)
			.build();

		log.info("Otp Entity for User Entity created Successfully");

		return otpEntity;

	}

}
