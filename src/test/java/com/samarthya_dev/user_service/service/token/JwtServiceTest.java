package com.samarthya_dev.user_service.service.token;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

	private JwtServiceImpl jwtService;

	private static final String TEST_JWT_SECRET = "this-is-a-very-secure-jwt-secret-key-256-bit-minimum";

	private UserEntity userEntity;

	@BeforeEach
	void setUp() throws Exception {

		jwtService = new JwtServiceImpl();

		Field secretField = JwtServiceImpl.class.getDeclaredField("SECRETS_JWT_KEY");
		secretField.setAccessible(true);
		secretField.set(jwtService, TEST_JWT_SECRET);

		userEntity = UserEntity.builder()
				.id(UUID.randomUUID())
				.email("user@test.com")
				.role(List.of(UserRole.USER))
				.build();

	}

	@Test
	@DisplayName("Generate token - token is not null and parsable")
	void generateTokenSuccess() {

		String token = jwtService.generateToken(userEntity);

		assertNotNull(token);
		assertFalse(token.isBlank());

	}

	@Test
	@DisplayName("Extract user email from valid token")
	void extractUserEmailSuccess() {

		String token = jwtService.generateToken(userEntity);

		String extractedEmail = jwtService.extractUserEmail(token);

		assertEquals(userEntity.getEmail(), extractedEmail);

	}

	@Test
	@DisplayName("Token validation success - valid token and matching user")
	void tokenValidationSuccess() {

		String token = jwtService.generateToken(userEntity);

		Boolean isValid = jwtService.isTokenValid(userEntity, token);

		assertTrue(isValid);

	}

	@Test
	@DisplayName("Token validation failure - email mismatch")
	void tokenValidationFailsForEmailMismatch() {

		String token = jwtService.generateToken(userEntity);

		UserEntity differentUser = UserEntity.builder()
				.id(userEntity.getId())
				.email("other@test.com")
				.role(List.of(UserRole.USER))
				.build();

		Boolean isValid = jwtService.isTokenValid(differentUser, token);

		assertFalse(isValid);

	}

	@Test
	@DisplayName("Token validation failure - user id mismatch")
	void tokenValidationFailsForUserIdMismatch() {

		String token = jwtService.generateToken(userEntity);

		UserEntity differentUser = UserEntity.builder()
				.id(UUID.randomUUID())
				.email(userEntity.getEmail())
				.role(List.of(UserRole.USER))
				.build();

		Boolean isValid = jwtService.isTokenValid(differentUser, token);

		assertFalse(isValid);

	}

	@Test
	@DisplayName("Token validation failure - expired token")
	void tokenValidationFailsForExpiredToken() throws Exception {

		String expiredToken = Jwts.builder()
				.subject(userEntity.getEmail())
				.claim("user_id", userEntity.getId())
				.claim("user_email", userEntity.getEmail())
				.claim("user_role", userEntity.getRole().getFirst())
				.issuedAt(Date.from(Instant.now().minusSeconds(120)))
				.expiration(Date.from(Instant.now().minusSeconds(60)))
				.signWith(Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes()))
				.compact();

		Boolean isValid = jwtService.isTokenValid(userEntity, expiredToken);

		assertFalse(isValid);

	}

	@Test
	@DisplayName("Token validation failure - malformed token")
	void tokenValidationFailsForMalformedToken() {

		Boolean isValid = jwtService.isTokenValid(userEntity, "not-a-jwt");

		assertFalse(isValid);

	}

	@Test
	@DisplayName("Token validation failure - token signed with different secret")
	void tokenValidationFailsForInvalidSignature() {

		String tokenWithDifferentSecret = Jwts.builder()
				.subject(userEntity.getEmail())
				.claim("user_id", userEntity.getId())
				.claim("user_email", userEntity.getEmail())
				.signWith(Keys.hmacShaKeyFor("another-secret-key-another-secret-key".getBytes()))
				.compact();

		Boolean isValid = jwtService.isTokenValid(userEntity, tokenWithDifferentSecret);

		assertFalse(isValid);

	}

}
