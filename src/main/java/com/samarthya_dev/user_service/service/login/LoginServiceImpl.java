package com.samarthya_dev.user_service.service.login;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.dto.request.LoginRequest;
import com.samarthya_dev.user_service.dto.response.LoginResponse;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.repository.UserRepository;
import com.samarthya_dev.user_service.service.token.JwtService;
import com.samarthya_dev.user_service.service.token.RefreshService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private final PasswordEncoder passwordEncoder;

	@Autowired
	private final JwtService jwtService;

	@Autowired
	private final RefreshService refreshService;

	LoginServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshService refreshService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshService = refreshService;
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {

		log.info("Login Service Invoked");

		log.info("Searching user by E-Mail in Database");
		Optional<UserEntity> userEntityOptional = userRepository.findByEmail(loginRequest.getEmail());

		final AtomicBoolean isEmailVerifiedAtm = new AtomicBoolean(Boolean.FALSE);
		final AtomicBoolean isPasswordCorrectAtm = new AtomicBoolean(Boolean.FALSE);
		final AtomicBoolean isUserAccountActiveAtm = new AtomicBoolean(Boolean.FALSE);
		Boolean isUserRegistered = userEntityOptional
			.filter(userEntity -> {
				if (userEntity.getEmailVerified()) {
					isEmailVerifiedAtm.set(Boolean.TRUE);
				}
				return isEmailVerifiedAtm.get();
			})
			.filter(userEntity -> {
				if (passwordEncoder.matches(loginRequest.getPassword(), userEntity.getHashedPassword())) {
					isPasswordCorrectAtm.set(Boolean.TRUE);
				}
				return isPasswordCorrectAtm.get();
			})
			.filter(userEntity -> {
				if (userEntity.getStatus() == UserStatus.ACTIVE) {
					isUserAccountActiveAtm.set(Boolean.TRUE);
				}
				return isUserAccountActiveAtm.get();
			})
			.isPresent();

		if ( !isUserRegistered || !isPasswordCorrectAtm.get()) {

			log.info("Queried User not found in Database or the Password was Incorrect");

			return LoginResponse
				.builder()
				.message("E-Mail or Password is Incorrect. Please Try Again")
				.build();

		}

		if ( !isEmailVerifiedAtm.get() || !isUserAccountActiveAtm.get()) {

			log.info("Queried User's E-Mail is not Verified and is DISABLED");

			return LoginResponse
				.builder()
				.message("E-Mail is not Verified. Please Verify E-Mail before proceeding")
				.build();

		}

		log.info("Valid User with Correct Password and Verified E-Mail found in Database");

		log.info("Generating Refresh Token for User");
		String refreshToken = refreshService.generateToken(userEntityOptional.get());
		log.info("Refresh Token Created for User");

		log.info("Generating JWT for User");
		String jwtToken = jwtService.generateToken(userEntityOptional.get());
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
	public LoginResponse refresh(LoginRequest loginRequest) {
		// 1. validate refresh token
		// 2. issue new jwt access
		return LoginResponse.builder().message("helloooo").build();
	}
	
}
