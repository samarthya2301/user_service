package com.samarthya_dev.user_service.service.register;

import java.time.Instant;
import org.springframework.stereotype.Component;

import com.samarthya_dev.user_service.dto.request.RegisterRequest;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegisterRequestToUserEntityTransformation {
	public UserEntity transform(RegisterRequest registerRequest) {

		log.info("Creating User Entity from Register Request");

		UserEntity userEntity = UserEntity.builder()
			.email(registerRequest.getEmail().toLowerCase())
			.emailVerified(Boolean.FALSE)
			.hashedPassword(registerRequest.getPassword())
			.status(UserStatus.DISABLED)
			.createdTimestamp(Instant.now())
			.updatedTimestamp(null)
			.build();

		log.info("User Entity from Register Requesst created Successfully");

		return userEntity;

	}
}
