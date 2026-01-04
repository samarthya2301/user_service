package com.samarthya_dev.user_service.service.login;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.dto.request.LoginRequest;
import com.samarthya_dev.user_service.dto.response.LoginResponse;
import com.samarthya_dev.user_service.entity.user.UserEntity;
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

	@Override
	public LoginResponse login(LoginRequest loginRequest) {

		log.info("Login Service Invoked");

		log.info("Searching user by E-Mail in Database");
		UserEntity userEntity = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

		if (userEntity == null || !passwordEncoder.matches(loginRequest.getPassword(), userEntity.getHashedPassword())) {

			log.info("Queried User not found in Database or the Password was Incorrect");

			return LoginResponse
				.builder()
				.message("E-Mail or Password is Incorrect. Please Try Again")
				.build();

		}

		if ( !userEntity.getEmailVerified() || userEntity.getStatus() != UserStatus.ACTIVE) {

			log.info("Queried User's E-Mail is not Verified and is DISABLED");

			return LoginResponse
				.builder()
				.message("E-Mail is not Verified. Please Verify E-Mail before proceeding")
				.build();

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

	}

	@Override
	public LoginResponse refresh(LoginRequest refreshRequest) {

		log.info("Refresh Service Invoked");

		log.info("Searching user by E-Mail in Database");

		LoginResponse loginResponse = userRepository.findByEmail(refreshRequest.getEmail())
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

				log.info("Valid User or Refresh Token was not found in the Database");
				return LoginResponse
					.builder()
					.message("Invalid E-Mail or Refresh Token received")
					.build();

			});

		return loginResponse;

	}
	
}
