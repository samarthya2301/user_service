package com.samarthya_dev.user_service.service.login;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.dto.request.LoginRequest;
import com.samarthya_dev.user_service.dto.response.LoginResponse;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.repository.RefreshTokenRepository;
import com.samarthya_dev.user_service.repository.UserRepository;
import com.samarthya_dev.user_service.service.token.JwtService;
import com.samarthya_dev.user_service.service.token.RefreshService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshService refreshService;
	private final RefreshTokenRepository refreshTokenRepository;

	private LoginResponse logMessageAndCreateLoginResponse(String logMessage, String responseMessage) {

		log.info(logMessage);

		return LoginResponse
				.builder()
				.message(responseMessage)
				.build();

	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {

		log.info("Login Service Invoked");

		log.info("Searching user by E-Mail in Database");

		return userRepository
			.findByEmail(loginRequest.getEmail())
			.map(userEntity -> {

				if ( !passwordEncoder.matches(loginRequest.getPassword(), userEntity.getHashedPassword())) {
					return logMessageAndCreateLoginResponse(
						"Queried User not found in Database or the Password was Incorrect",
						"E-Mail or Password is Incorrect. Please Try Again"
					);
				}

				if ( !userEntity.getEmailVerified() || userEntity.getStatus() != UserStatus.ACTIVE) {
					return logMessageAndCreateLoginResponse(
						"Queried User's E-Mail is not Verified and is DISABLED",
						"E-Mail is not Verified. Please Verify E-Mail before proceeding"
					);
				}

				log.info("Valid User with Correct Password and Verified E-Mail found in Database");

				log.info("Generating Refresh Token for User");
				String refreshToken = refreshService.generateToken(userEntity);
				log.info("Refresh Token Created for User");

				log.info("Generating JWT for User");
				String jwtToken = jwtService.generateToken(userEntity);
				log.info("JWT Created for User");

				return LoginResponse
					.builder()
					.accessToken(jwtToken)
					.refreshToken(refreshToken)
					.tokenType("Bearer")
					.message("User Logged In Successfully")
					.build();

			})
			.orElseGet(() -> {
				return logMessageAndCreateLoginResponse(
					"Queried User not found in Database or the Password was Incorrect",
					"E-Mail or Password is Incorrect. Please Try Again"
				);
			});

	}

	@Override
	public LoginResponse refresh(LoginRequest refreshRequest) {

		log.info("Refresh Service Invoked");

		log.info("Searching user by E-Mail in Database");

		return userRepository.findByEmail(refreshRequest.getEmail())
    		.flatMap(userEntity -> {

				log.info("User found in Database");

				log.info("Searching Refresh Token Entity for User in Database");

				return refreshTokenRepository.findValidRefreshTokenForUser(refreshRequest.getEmail(), refreshRequest.getRefreshToken())
					.map(tokenEntity -> {

						log.info("Valid Refresh Token Entity Found for User in Database");

						log.info("Generating JWT for User");
						String jwtToken = jwtService.generateToken(userEntity);
						log.info("JWT Created for User");

						return LoginResponse
							.builder()
							.accessToken(jwtToken)
							.refreshToken(tokenEntity.getToken())
							.tokenType("Bearer")
							.message("Bearer JWT Renewed Successfully")
							.build();

					});

			})
			.orElseGet(() -> {
				return logMessageAndCreateLoginResponse(
					"Valid User or Refresh Token was not found in the Database",
					"Invalid E-Mail or Refresh Token received"
				);
			});

	}
	
}
