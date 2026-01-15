package com.samarthya_dev.user_service.service.otp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import com.samarthya_dev.user_service.dto.request.OtpRequest;
import com.samarthya_dev.user_service.dto.response.OtpResponse;
import com.samarthya_dev.user_service.entity.otp.OtpEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.repository.OtpRepository;
import com.samarthya_dev.user_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private OtpRepository otpRepository;

	@Mock
	private RandomOtpEntityCreator randomOtpEntityCreator;

	@Mock
	private EmailService emailService;

	@Mock
	private TemplateEngine templateEngine;

	@InjectMocks
	private OtpServiceImpl otpService;

	private static final String TEST_EMAIL = "abc@xyz.com";
	private static final Integer TEST_OTP_CODE = 123456;

	private OtpRequest otpRequest;
	private OtpRequest otpVerifyRequest;
	private UserEntity userEntity;
	private OtpEntity otpEntity;

	@BeforeEach
	void setUp() {

		otpRequest = OtpRequest.builder()
				.email(TEST_EMAIL)
				.build();

		otpVerifyRequest = OtpRequest.builder()
				.email(TEST_EMAIL)
				.otpCode(TEST_OTP_CODE)
				.build();

		userEntity = UserEntity.builder()
				.email(TEST_EMAIL)
				.build();

		otpEntity = OtpEntity.builder()
				.otpCode(TEST_OTP_CODE)
				.consumed(false)
				.user(userEntity)
				.expiresTimestamp(Instant.now().plusSeconds(300))
				.build();

	}

	@Test
	@DisplayName("OTP request success - otp saved and email sent")
	void otpRequestSuccess() {

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(userEntity));

		when(randomOtpEntityCreator.create(userEntity))
				.thenReturn(otpEntity);

		when(templateEngine.process(eq("otp-code-on-email-template"), any(Context.class)))
				.thenReturn("<html>otp</html>");

		OtpResponse response = otpService.otpRequest(otpRequest);

		assertEquals("Otp Sent Successfully", response.getMessage());

		verify(otpRepository).save(otpEntity);
		verify(emailService).sendEMail(any(), any(), any());

	}

	@Test
	@DisplayName("OTP request failure - user does not exist")
	void otpRequestUserNotFound() {

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.empty());

		OtpResponse response = otpService.otpRequest(otpRequest);

		assertEquals("User does not exist. Please register", response.getMessage());

		verifyNoInteractions(otpRepository, emailService);

	}

	@Test
	@DisplayName("OTP request failure - email sending fails but otp is still saved")
	void otpRequestEmailSendFailure() {

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(userEntity));

		when(randomOtpEntityCreator.create(userEntity))
				.thenReturn(otpEntity);

		when(templateEngine.process(any(String.class), any(IContext.class)))
				.thenReturn("<html>otp</html>");

		doThrow(new RuntimeException("SES failure"))
				.when(emailService).sendEMail(any(), any(), any());

		OtpResponse response = otpService.otpRequest(otpRequest);

		assertEquals(
				"Cannot send Otp, an error occurred. Please Try Again",
				response.getMessage());

		verify(otpRepository).save(otpEntity);

	}

	@Test
	@DisplayName("OTP verify success - otp consumed and user activated")
	void otpVerifySuccess() {

		when(otpRepository.findValidOtpForUser(TEST_EMAIL, TEST_OTP_CODE))
				.thenReturn(Optional.of(otpEntity));

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.of(userEntity));

		OtpResponse response = otpService.otpVerify(otpVerifyRequest);

		assertEquals("User's E-Mail verified successfully.", response.getMessage());

		assertTrue(otpEntity.getConsumed());
		assertTrue(userEntity.getEmailVerified());
		assertEquals(UserStatus.ACTIVE, userEntity.getStatus());

		verify(otpRepository).save(otpEntity);
		verify(userRepository).save(userEntity);

	}

	@Test
	@DisplayName("OTP verify failure - invalid, expired, or consumed otp")
	void otpVerifyFailureForInvalidOtp() {

		otpEntity.setExpiresTimestamp(Instant.now().minusSeconds(10));

		when(otpRepository.findValidOtpForUser(TEST_EMAIL, TEST_OTP_CODE))
				.thenReturn(Optional.of(otpEntity));

		OtpResponse response = otpService.otpVerify(otpVerifyRequest);

		assertEquals(
				"User's E-Mail cannot be verified. Please request Otp again.",
				response.getMessage());

		verifyNoInteractions(userRepository);
		verify(otpRepository, never()).save(any());

	}

	@Test
	@DisplayName("OTP verify failure - user not found after otp validation (exposes bug)")
	void otpVerifyUserNotFound() {

		when(otpRepository.findValidOtpForUser(TEST_EMAIL, TEST_OTP_CODE))
				.thenReturn(Optional.of(otpEntity));

		when(userRepository.findByEmail(TEST_EMAIL))
				.thenReturn(Optional.empty());

		assertThrows(
				NoSuchElementException.class,
				() -> otpService.otpVerify(otpVerifyRequest));

	}

}
