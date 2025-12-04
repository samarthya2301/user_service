package com.samarthya_dev.user_service.service.register;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderEntity;
import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderType;
import com.samarthya_dev.user_service.entity.user.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthProviderForUserEntityCreator {
	public AuthProviderEntity create(UserEntity user, AuthProviderType authProviderType) {

		log.info("Creating Auth Provider Entity for User Entity");

		AuthProviderEntity authProviderEntity = AuthProviderEntity
			.builder()
			.user(user)
			.provider(authProviderType)
			.providerDetailsJson(null)
			.createdTimestamp(Instant.now())
			.build();

		log.info("Auth Provider Entity for User Entity created Successfully");

		return authProviderEntity;

	}
}
