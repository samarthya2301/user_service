package com.samarthya_dev.user_service.config.properties.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Value;

@Value
@ConfigurationProperties(prefix = "controller.paths")
public class ControllerPathsConfig {

	CheckAndTest checkAndTest;
	Login login;
	Otp otp;
	Register register;

	@Value
	public static class CheckAndTest {
		String healthCheck;
		String testAuth;
	}

	@Value
	public static class Login {
		String login;
		String refreshToken;
	}

	@Value
	public static class Otp {
		String otpRequest;
		String otpVerify;
	}

	@Value
	public static class Register {
		String register;
	}

}
