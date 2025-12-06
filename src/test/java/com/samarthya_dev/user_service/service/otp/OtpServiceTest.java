package com.samarthya_dev.user_service.service.otp;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
	private OtpRequest requestForOtpRequestFlow;
	private OtpRequest requestForOtpVerifyFlow;
    private UserEntity userEntity;
    private OtpEntity otpEntity;

    @BeforeEach
	private void initializeData() {

        requestForOtpRequestFlow = OtpRequest
            .builder()
            .email(OtpServiceTest.TEST_EMAIL)
            .build();

        requestForOtpVerifyFlow = OtpRequest
            .builder()
            .email(OtpServiceTest.TEST_EMAIL)
            .otpCode(OtpServiceTest.TEST_OTP_CODE)
            .build();

        userEntity = UserEntity
            .builder()
            .email(OtpServiceTest.TEST_EMAIL)
            .build();

        otpEntity = OtpEntity
			.builder()
			.otpCode(OtpServiceTest.TEST_OTP_CODE)
			.consumed(Boolean.FALSE)
			.userId(userEntity)
			.expiresTimestamp(Instant.now().plusSeconds(300))
			.build();

	}

    @Test
    @DisplayName("OTP Request Success")
    public void testOtpRequestSuccess() {

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
            .thenReturn(Optional.of(userEntity));

        when(randomOtpEntityCreator.create(userEntity))
			.thenReturn(otpEntity);

        when(templateEngine.process(eq("otp-code-on-email-template"), any(Context.class)))
            .thenReturn("<html>OTP Email Template</html>");

        doNothing().when(emailService).sendEMail(any(), any(), any());

        OtpResponse response = otpService.otpRequest(requestForOtpRequestFlow);

        assertEquals("Otp Sent Successfully", response.getMessage());
        verify(otpRepository, times(1)).save(otpEntity);
        verify(emailService, times(1)).sendEMail(any(), any(), any());

    }

    @Test
    @DisplayName("OTP Request Failure - User does not exist")
    public void testOtpRequestUserDoesNotExist() {

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
            .thenReturn(Optional.empty());

        OtpResponse response = otpService.otpRequest(requestForOtpRequestFlow);

        assertEquals("User does not exist. Please register", response.getMessage());
        verify(otpRepository, never()).save(any());
        verify(emailService, never()).sendEMail(any(), any(), any());

    }

    @Test
    @DisplayName("OTP Request Failure - Failure in sending E-Mail")
    public void testOtpRequestEmailSendFailure() {

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
			.thenReturn(Optional.of(userEntity));

        when(randomOtpEntityCreator.create(userEntity))
			.thenReturn(otpEntity);

		when(templateEngine.process(
			eq("otp-code-on-email-template"),
			any(Context.class)
		))
		.thenReturn("<html>template</html>");

        doThrow(new RuntimeException("SES Failure"))
			.when(emailService).sendEMail(any(), any(), any());

        OtpResponse response = otpService.otpRequest(requestForOtpRequestFlow);

        assertEquals("Cannot send Otp, an error occurred. Please Try Again", response.getMessage());
        verify(otpRepository, times(1)).save(otpEntity);

    }

    @Test
    @DisplayName("OTP Verify Success")
    public void testOtpVerifySuccess() {

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
                .thenReturn(Optional.of(otpEntity));

        when(userRepository.findByEmail(OtpServiceTest.TEST_EMAIL))
                .thenReturn(Optional.of(userEntity));

        OtpResponse response = otpService.otpVerify(requestForOtpVerifyFlow);

        assertEquals("User's E-Mail verified successfully.", response.getMessage());

        assertTrue(otpEntity.getConsumed());
        assertTrue(userEntity.getEmailVerified());
        assertEquals(UserStatus.ACTIVE, userEntity.getStatus());

        verify(otpRepository, times(1)).save(otpEntity);
        verify(userRepository, times(1)).save(userEntity);

    }

    @Test
    @DisplayName("OTP Verify Failure - OTP is present but expired")
    public void testOtpVerifyExpiredOtp() {

        otpEntity.setExpiresTimestamp(Instant.now().minusSeconds(5L));

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
            .thenReturn(Optional.of(otpEntity));

        OtpResponse response = otpService.otpVerify(requestForOtpVerifyFlow);

        assertEquals("User's E-Mail cannot be verified. Please request Otp again.", response.getMessage());

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).save(any());

    }

    @Test
    @DisplayName("OTP Verify Failure - OTP is present but already consumed")
    public void testOtpVerifyConsumedOtp() {

        otpEntity.setConsumed(Boolean.TRUE);

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
            .thenReturn(Optional.of(otpEntity));

        OtpResponse response = otpService.otpVerify(requestForOtpVerifyFlow);

        assertEquals("User's E-Mail cannot be verified. Please request Otp again.", response.getMessage());

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).save(any());

    }

    @Test
    @DisplayName("OTP Verify Failure - OTP is not present because invalid")
    public void testOtpVerifyNoOtpFound() {

        when(otpRepository.findValidOtpForUser(OtpServiceTest.TEST_EMAIL, OtpServiceTest.TEST_OTP_CODE))
			.thenReturn(Optional.empty());

        OtpResponse response = otpService.otpVerify(requestForOtpVerifyFlow);

        assertEquals("User's E-Mail cannot be verified. Please request Otp again.", response.getMessage());

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).save(any());

    }

}
