package com.samarthya_dev.user_service.service.register;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.samarthya_dev.user_service.dto.request.RegisterRequest;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserRole;
import com.samarthya_dev.user_service.entity.user.UserStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegisterRequestToUserEntityTransformation {

	@Autowired
	private final PasswordEncoder passwordEncoder;

	public RegisterRequestToUserEntityTransformation(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public UserEntity transform(RegisterRequest registerRequest) {

		log.info("Creating User Entity from Register Request");

		UserEntity userEntity = UserEntity
			.builder()
			.email(registerRequest.getEmail().toLowerCase())
			.emailVerified(Boolean.FALSE)
			.hashedPassword(passwordEncoder.encode(registerRequest.getPassword()))
			.role(List.of(UserRole.USER))
			.status(UserStatus.DISABLED)
			.createdTimestamp(Instant.now())
			.updatedTimestamp(null)
			.build();

		log.info("User Entity from Register Requesst created Successfully");

		return userEntity;

	}

}
