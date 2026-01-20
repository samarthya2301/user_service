package com.samarthya_dev.user_service.service.token;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.config.properties.common.SecretsJwtConfig;
import com.samarthya_dev.user_service.config.properties.common.ServiceConfig;
import com.samarthya_dev.user_service.entity.user.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@EnableConfigurationProperties({SecretsJwtConfig.class, ServiceConfig.class})
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

	private final SecretsJwtConfig secretsJwtConfig;
	private final ServiceConfig serviceConfig;

	private SecretKey signingKey() {

		byte[] keyBytes = secretsJwtConfig.getKey().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);

	}

	@Override
	public String generateToken(UserEntity userEntity) {
		return Jwts
			.builder()
			.subject(userEntity.getEmail())
			.claim(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId(), userEntity.getId().toString())
			.claim(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail(), userEntity.getEmail())
			.claim(serviceConfig.getToken().getJwt().getClaim().getKey().getUserRole(), userEntity.getRole().getFirst())
			.issuedAt(Date.from(Instant.now()))
			.expiration(Date.from(Instant.now().plus(serviceConfig.getToken().getJwt().getExpireAfter())))
			.signWith(signingKey())
			.compact();
	}

	@Override
	public String extractUserEmail(String token) {

		Claims tokenClaims = Jwts
			.parser()
			.verifyWith(signingKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return tokenClaims.get(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail(), String.class);

	}

	@Override
	public Boolean isTokenValid(UserEntity userEntity, String token) {

		try {

			Claims tokenClaims = Jwts
				.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

			if (
				!tokenClaims.get(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId()).equals(userEntity.getId().toString()) ||
				!tokenClaims.get(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail()).equals(userEntity.getEmail())
			) {
				throw new JwtException("User Identity/E-Mail Does Not Match");
			}

			if (tokenClaims.getExpiration().toInstant().isBefore(Instant.now())) {
				throw new JwtException("User JWT is Expired");
			}

		} catch (JwtException | IllegalArgumentException e) {

			log.info("Provided JWT is Not Valid, JWT Parsing Failed");
			log.error("JwtService#isTokenValid: " + e.getMessage());
			return Boolean.FALSE;

		} catch (Exception e) {

			log.info("Provided JWT is Not Valid, General JWT Error");
			log.error("JwtService#isTokenValid: " + e.getMessage());
			return Boolean.FALSE;

		}

		return Boolean.TRUE;

	}
	
}
