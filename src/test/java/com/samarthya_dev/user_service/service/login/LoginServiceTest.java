package com.samarthya_dev.user_service.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.samarthya_dev.user_service.dto.request.LoginRequest;
import com.samarthya_dev.user_service.dto.response.LoginResponse;
import com.samarthya_dev.user_service.entity.refresh_token.RefreshTokenEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.repository.RefreshTokenRepository;
import com.samarthya_dev.user_service.repository.UserRepository;
import com.samarthya_dev.user_service.service.token.JwtService;
import com.samarthya_dev.user_service.service.token.RefreshService;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private RefreshService refreshService;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private LoginServiceImpl loginService;

	private static final String TEST_EMAIL = "user@test.com";
	private static final String RAW_PASSWORD = "password";
	private static final String HASHED_PASSWORD = "hashed-password";
	private static final String JWT_TOKEN = "jwt-token";
	private static final String REFRESH_TOKEN = "refresh-token";

	private UserEntity activeVerifiedUser;
	private LoginRequest loginRequest;

	@BeforeEach
	void setUp() {

		activeVerifiedUser = UserEntity.builder()
				.email(TEST_EMAIL)
				.hashedPassword(HASHED_PASSWORD)
				.emailVerified(true)
				.status(UserStatus.ACTIVE)
				.build();

		loginRequest = LoginRequest.builder()
				.email(TEST_EMAIL)
				.password(RAW_PASSWORD)
				.build();

	}

	@Test
	@DisplayName("Login failure - user not found")
	void loginFailsWhenUserNotFound() {

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.empty());

		LoginResponse response = loginService.login(loginRequest);

		assertEquals(
				"E-Mail or Password is Incorrect. Please Try Again",
				response.getMessage());

		verifyNoInteractions(passwordEncoder, jwtService, refreshService);

	}

	@Test
	@DisplayName("Login failure - incorrect password")
	void loginFailsWhenPasswordIncorrect() {

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(activeVerifiedUser));

		when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
				.thenReturn(false);

		LoginResponse response = loginService.login(loginRequest);

		assertEquals(
				"E-Mail or Password is Incorrect. Please Try Again",
				response.getMessage());

		verify(jwtService, never()).generateToken(any());
		verify(refreshService, never()).generateToken(any());

	}

	@Test
	@DisplayName("Login failure - email not verified or user not active")
	void loginFailsWhenUserNotVerifiedOrInactive() {

		activeVerifiedUser.setEmailVerified(false);

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(activeVerifiedUser));

		when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
				.thenReturn(true);

		LoginResponse response = loginService.login(loginRequest);

		assertEquals(
				"E-Mail is not Verified. Please Verify E-Mail before proceeding",
				response.getMessage());

		verify(jwtService, never()).generateToken(any());
		verify(refreshService, never()).generateToken(any());

	}

	@Test
	@DisplayName("Login success - jwt and refresh token generated")
	void loginSuccess() {

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(activeVerifiedUser));

		when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD))
				.thenReturn(true);

		when(refreshService.generateToken(activeVerifiedUser))
				.thenReturn(REFRESH_TOKEN);

		when(jwtService.generateToken(activeVerifiedUser))
				.thenReturn(JWT_TOKEN);

		LoginResponse response = loginService.login(loginRequest);

		assertEquals("User Logged In Successfully", response.getMessage());
		assertEquals(JWT_TOKEN, response.getAccessToken());
		assertEquals(REFRESH_TOKEN, response.getRefreshToken());
		assertEquals("Bearer", response.getTokenType());

		verify(refreshService).generateToken(activeVerifiedUser);
		verify(jwtService).generateToken(activeVerifiedUser);

	}

	@Test
	@DisplayName("Refresh failure - user not found")
	void refreshFailsWhenUserNotFound() {

		LoginRequest refreshRequest = LoginRequest.builder()
				.email(TEST_EMAIL)
				.refreshToken(REFRESH_TOKEN)
				.build();

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.empty());

		LoginResponse response = loginService.refresh(refreshRequest);

		assertEquals(
				"Invalid E-Mail or Refresh Token received",
				response.getMessage());

		verifyNoInteractions(jwtService);

	}

	@Test
	@DisplayName("Refresh failure - refresh token invalid")
	void refreshFailsWhenRefreshTokenInvalid() {

		LoginRequest refreshRequest = LoginRequest.builder()
				.email(TEST_EMAIL)
				.refreshToken(REFRESH_TOKEN)
				.build();

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(activeVerifiedUser));

		when(refreshTokenRepository.findValidRefreshTokenForUser(TEST_EMAIL, REFRESH_TOKEN))
				.thenReturn(Optional.empty());

		LoginResponse response = loginService.refresh(refreshRequest);

		assertEquals(
				"Invalid E-Mail or Refresh Token received",
				response.getMessage());

		verify(jwtService, never()).generateToken(any());

	}

	@Test
	@DisplayName("Refresh success - jwt regenerated")
	void refreshSuccess() {

		LoginRequest refreshRequest = LoginRequest.builder()
				.email(TEST_EMAIL)
				.refreshToken(REFRESH_TOKEN)
				.build();

		RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
				.token(REFRESH_TOKEN)
				.build();

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(activeVerifiedUser));

		when(refreshTokenRepository.findValidRefreshTokenForUser(TEST_EMAIL, REFRESH_TOKEN))
				.thenReturn(Optional.of(refreshTokenEntity));

		when(jwtService.generateToken(activeVerifiedUser))
				.thenReturn(JWT_TOKEN);

		LoginResponse response = loginService.refresh(refreshRequest);

		assertEquals("Bearer JWT Renewed Successfully", response.getMessage());
		assertEquals(JWT_TOKEN, response.getAccessToken());
		assertEquals(REFRESH_TOKEN, response.getRefreshToken());

		verify(jwtService).generateToken(activeVerifiedUser);

	}

}
