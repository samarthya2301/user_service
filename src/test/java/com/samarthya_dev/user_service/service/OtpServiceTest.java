package com.samarthya_dev.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.samarthya_dev.user_service.dto.request.OtpRequest;
import com.samarthya_dev.user_service.dto.response.OtpResponse;
import com.samarthya_dev.user_service.entity.otp.OtpEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.repository.OtpRepository;
import com.samarthya_dev.user_service.repository.UserRepository;
import com.samarthya_dev.user_service.service.otp.EmailService;
import com.samarthya_dev.user_service.service.otp.OtpServiceImpl;
import com.samarthya_dev.user_service.service.otp.RandomOtpEntityCreator;

@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {

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
	private static OtpRequest otpRequestWithEmail = null;
	private static OtpRequest otpRequestWithEmailAndOtp = null;
    private static UserEntity userWithEmail = null;
    private static OtpEntity otpEntityWithOtpCode = null;
    private static OtpEntity otpEntityWithOtherDetails = null;

	@BeforeAll
	private static void initializeStaticData() {

		OtpServiceTest.otpRequestWithEmail = OtpRequest.builder().email(OtpServiceTest.TEST_EMAIL).build();
        OtpServiceTest.otpRequestWithEmailAndOtp = OtpRequest.builder().email(OtpServiceTest.TEST_EMAIL).otpCode(OtpServiceTest.TEST_OTP_CODE).build();
        OtpServiceTest.userWithEmail = UserEntity.builder().email(OtpServiceTest.TEST_EMAIL).build();
        OtpServiceTest.otpEntityWithOtpCode = OtpEntity.builder().otpCode(OtpServiceTest.TEST_OTP_CODE).build();
        OtpServiceTest.otpEntityWithOtherDetails = OtpEntity
			.builder()
			.otpCode(OtpServiceTest.TEST_OTP_CODE)
			.consumed(Boolean.FALSE)
			.userId(OtpServiceTest.userWithEmail)
			.expiresTimestamp(Instant.now().plusSeconds(300))
			.build();

	}

    @Test
    public void testOtpRequestUserDoesNotExist() {

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
            .thenReturn(Optional.empty());

        OtpResponse response = otpService.otpRequest(OtpServiceTest.otpRequestWithEmail);

        assertEquals("User does not exist. Please register", response.getMessage());
        verify(otpRepository, never()).save(any());
        verify(emailService, never()).sendEMail(any(), any(), any());

    }

    @Test
    public void testOtpRequestSuccess() {

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
            .thenReturn(Optional.of(userWithEmail));

        when(randomOtpEntityCreator.create(userWithEmail))
			.thenReturn(otpEntityWithOtpCode);

        when(templateEngine.process(eq("otp-code-on-email-template"), any(Context.class)))
            .thenReturn("<html>OTP Email Template</html>");

        doNothing().when(emailService).sendEMail(any(), any(), any());

        OtpResponse response = otpService.otpRequest(OtpServiceTest.otpRequestWithEmail);

        assertEquals("Otp Sent Successfully", response.getMessage());
        verify(otpRepository, times(1)).save(otpEntityWithOtpCode);
        verify(emailService, times(1)).sendEMail(any(), any(), any());

    }

    @Test
    public void testOtpRequestEmailSendFailure() {

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
			.thenReturn(Optional.of(userWithEmail));

        when(randomOtpEntityCreator.create(userWithEmail))
			.thenReturn(otpEntityWithOtpCode);

		when(templateEngine.process(
			eq("otp-code-on-email-template"),
			any(Context.class)
		))
		.thenReturn("<html>template</html>");

        doThrow(new RuntimeException("SES error"))
			.when(emailService).sendEMail(any(), any(), any());

        OtpResponse response = otpService.otpRequest(OtpServiceTest.otpRequestWithEmail);

        assertEquals("Cannot send Otp, an error occurred. Please Try Again", response.getMessage());
        verify(otpRepository, times(1)).save(otpEntityWithOtpCode);

    }

    @Test
    public void testOtpVerifySuccess() {

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
                .thenReturn(Optional.of(otpEntityWithOtherDetails));
        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
                .thenReturn(Optional.of(userWithEmail));

        OtpResponse response = otpService.otpVerify(otpRequestWithEmailAndOtp);

        assertEquals("User's E-Mail verified successfully.", response.getMessage());

        assertTrue(otpEntityWithOtherDetails.getConsumed());
        assertTrue(userWithEmail.getEmailVerified());
        assertEquals(UserStatus.ACTIVE, userWithEmail.getStatus());

        verify(otpRepository, times(1)).save(otpEntityWithOtherDetails);
        verify(userRepository, times(1)).save(userWithEmail);

    }

    @Test
    public void testOtpVerifyFailedExpiredOtp() {

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
            .thenReturn(Optional.of(otpEntityWithOtherDetails));

        OtpResponse response = otpService.otpVerify(otpRequestWithEmailAndOtp);

        assertEquals("User's E-Mail cannot be verified. Please request Otp again.", response.getMessage());

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).save(any());

    }

    @Test
    public void testOtpVerifyNoOtpFound() {

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
			.thenReturn(Optional.empty());

        OtpResponse response = otpService.otpVerify(otpRequestWithEmailAndOtp);

        assertEquals("User's E-Mail cannot be verified. Please request Otp again.", response.getMessage());

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).save(any());

    }

}
