package com.samarthya_dev.user_service.service.token;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.entity.refresh_token.RefreshTokenEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshServiceImpl implements RefreshService {

	private final RefreshTokenRepository refreshTokenRepository;

	private String random512BitBase64() {

        int numberOfBytes = 512 / 8;
        byte[] randomBytes = new byte[numberOfBytes];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        return Base64.getEncoder().encodeToString(randomBytes);

    }

	/**
	 * Generates a token with an expiry of 7 days after its creation
	 * Expiry is 7 days = 604800000 (7 * 24 * 60 * 60 * 1000) milliseconds after creation
	 */
	@Override
	public String generateToken(UserEntity userEntity) {

		RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity
			.builder()
			.user(userEntity)
			.token(random512BitBase64())
			.createdTimestamp(Instant.now())
			.expiresTimestamp(Instant.now().plusMillis(604_800_000L))
			.revoked(Boolean.FALSE)
			.build();

		log.info("Saving Refresh Token Entity into Database");
		refreshTokenRepository.save(refreshTokenEntity);
		log.info("Refresh Token Entity saved successfully into Database");

		return refreshTokenEntity.getToken();

	}
	
}
