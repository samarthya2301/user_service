package com.samarthya_dev.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.samarthya_dev.user_service.entity.otp.OtpEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.service.otp.RandomOtpEntityCreator;

@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {

	@InjectMocks
	private RandomOtpEntityCreator randomOtpEntityCreator;

	private static UserEntity userEntity;

	@BeforeAll
	static void setUserEntity() {
		userEntity = UserEntity
			.builder()
			.email("test.email@example.com")
			.emailVerified(Boolean.FALSE)
			.hashedPassword("test.hashed.password")
			.status(UserStatus.DISABLED)
			.createdTimestamp(Instant.now())
			.updatedTimestamp(null)
			.build();
	}

	@Test
	@DisplayName("Test OTP is Generated Correctly")
	void testOtpIsGeneratedCorrectly() {

		OtpEntity otpEntity = randomOtpEntityCreator.create(OtpServiceTest.userEntity);

		assertNotNull(otpEntity);
		assertEquals(otpEntity.getUserId(), OtpServiceTest.userEntity);
		assertTrue(otpEntity.getOtpCode() >= 100000 && otpEntity.getOtpCode() <= 999999);

	}
	
	@Test
	void testOtpExpiresAfterFiveMinutes() {

	}

	@Test
	void testOtpVerificationFailsIfExpired() {

	}

	@Test
	void testOtpVerificationFailsIfWrongCode() {

	}

	@Test
	void testOtpVerificationSucceeds() {

	}
	
}
