package com.samarthya_dev.user_service.service.register;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.samarthya_dev.user_service.entity.role.RoleEntity;
import com.samarthya_dev.user_service.entity.role.RoleName;
import com.samarthya_dev.user_service.entity.user.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RoleForUserEntityCreator {
	public RoleEntity create(UserEntity user, RoleName roleName) {

		log.info("Creating Role Entity for User Entity");

		RoleEntity authProviderEntity = RoleEntity
			.builder()
			.user(user)
			.roleName(roleName)
			.createdTimestamp(Instant.now())
			.build();

		log.info("Role Entity for User Entity created Successfully");

		return authProviderEntity;

	}
}
