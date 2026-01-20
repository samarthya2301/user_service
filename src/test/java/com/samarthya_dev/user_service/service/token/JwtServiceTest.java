package com.samarthya_dev.user_service.service.token;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.mockito.Mock;
import com.samarthya_dev.user_service.config.properties.common.SecretsJwtConfig;
import com.samarthya_dev.user_service.config.properties.common.ServiceConfig;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock
	private SecretsJwtConfig secretsJwtConfig;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ServiceConfig serviceConfig;

	@InjectMocks
	private JwtServiceImpl jwtService;

	private static final String TEST_JWT_SECRET = "this-is-a-very-secure-jwt-secret-key-256-bit-minimum";
	private static final String USER_ID_CLAIM = "user_id";
	private static final String USER_EMAIL_CLAIM = "user_email";
	private static final String USER_ROLE_CLAIM = "user_role";
	private static final Duration EXPIRY_SECONDS = Duration.ofDays(1);

	private UserEntity userEntity;

	@BeforeEach
	void setUp() {

		when(secretsJwtConfig.getKey())
				.thenReturn(TEST_JWT_SECRET);

		when(serviceConfig.getToken().getJwt().getExpireAfter())
				.thenReturn(EXPIRY_SECONDS);

		userEntity = UserEntity.builder()
				.id(UUID.randomUUID())
				.email("user@test.com")
				.role(List.of(UserRole.USER))
				.build();

	}

	@Test
	@DisplayName("Generate token - token is generated successfully")
	void generateTokenSuccess() {

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId())
				.thenReturn(USER_ID_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail())
				.thenReturn(USER_EMAIL_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserRole())
				.thenReturn(USER_ROLE_CLAIM);

		String token = jwtService.generateToken(userEntity);

		assertNotNull(token);
		assertFalse(token.isBlank());

	}

	@Test
	@DisplayName("Extract user email from valid token")
	void extractUserEmailSuccess() {

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId())
				.thenReturn(USER_ID_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail())
				.thenReturn(USER_EMAIL_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserRole())
				.thenReturn(USER_ROLE_CLAIM);

		String token = jwtService.generateToken(userEntity);

		String extractedEmail = jwtService.extractUserEmail(token);

		assertEquals(userEntity.getEmail(), extractedEmail);

	}

	@Test
	@DisplayName("Token validation success - UUID and email match")
	void tokenValidationSuccess() {

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId())
				.thenReturn(USER_ID_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail())
				.thenReturn(USER_EMAIL_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserRole())
				.thenReturn(USER_ROLE_CLAIM);

		String token = jwtService.generateToken(userEntity);

		Boolean isValid = jwtService.isTokenValid(userEntity, token);

		assertTrue(isValid);

	}

	@Test
	@DisplayName("Token validation failure - email mismatch")
	void tokenValidationFailsForEmailMismatch() {

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId())
				.thenReturn(USER_ID_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail())
				.thenReturn(USER_EMAIL_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserRole())
				.thenReturn(USER_ROLE_CLAIM);

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

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserId())
				.thenReturn(USER_ID_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserEmail())
				.thenReturn(USER_EMAIL_CLAIM);

		when(serviceConfig.getToken().getJwt().getClaim().getKey().getUserRole())
				.thenReturn(USER_ROLE_CLAIM);

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
	void tokenValidationFailsForExpiredToken() {

		String expiredToken = Jwts.builder()
				.subject(userEntity.getEmail())
				.claim(USER_ID_CLAIM, userEntity.getId().toString())
				.claim(USER_EMAIL_CLAIM, userEntity.getEmail())
				.claim(USER_ROLE_CLAIM, userEntity.getRole().getFirst())
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
	@DisplayName("Token validation failure - invalid signature")
	void tokenValidationFailsForInvalidSignature() {

		String tokenWithDifferentSecret = Jwts.builder()
				.subject(userEntity.getEmail())
				.claim(USER_ID_CLAIM, userEntity.getId().toString())
				.claim(USER_EMAIL_CLAIM, userEntity.getEmail())
				.signWith(Keys.hmacShaKeyFor("another-secret-key-another-secret-key".getBytes()))
				.compact();

		Boolean isValid = jwtService.isTokenValid(userEntity, tokenWithDifferentSecret);

		assertFalse(isValid);

	}

}
