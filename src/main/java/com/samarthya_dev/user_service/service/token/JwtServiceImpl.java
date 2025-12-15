package com.samarthya_dev.user_service.service.token;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.entity.user.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

	@Value("${secrets.jwt.key}")
	private String SECRETS_JWT_KEY;

	private SecretKey signingKey() {

		byte[] keyBytes = SECRETS_JWT_KEY.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);

	}

	@Override
	public String generateToken(UserEntity userEntity) {
		return Jwts
			.builder()
			.subject(userEntity.getEmail())
			.claim("user_id", userEntity.getId())
			.claim("user_email", userEntity.getEmail())
			.claim("user_role", userEntity.getRole().getFirst())
			.issuedAt(Date.from(Instant.now()))
			.expiration(Date.from(Instant.now().plusSeconds(86_400L)))
			.signWith(signingKey())
			.compact();
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

			if ( !tokenClaims.get("user_id").equals(userEntity.getId()) || !tokenClaims.get("user_email").equals(userEntity.getEmail())) {
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
