package com.samarthya_dev.user_service.service.otp;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.samarthya_dev.user_service.dto.request.OtpRequest;
import com.samarthya_dev.user_service.dto.response.OtpResponse;
import com.samarthya_dev.user_service.entity.otp.OtpEntity;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.entity.user.UserStatus;
import com.samarthya_dev.user_service.repository.OtpRepository;
import com.samarthya_dev.user_service.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

	@Autowired
    private final OtpRepository otpRepository;

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private final RandomOtpEntityCreator randomOtpEntityCreator;

	@Autowired
	private final EmailService emailService;

	@Autowired
	private final TemplateEngine templateEngine;

	public OtpServiceImpl(
		UserRepository userRepository,
		RandomOtpEntityCreator randomOtpEntityCreator,
		OtpRepository otpRepository,
		EmailService emailService,
		TemplateEngine templateEngine
	) {
		this.userRepository = userRepository;
		this.randomOtpEntityCreator = randomOtpEntityCreator;
		this.otpRepository = otpRepository;
		this.emailService = emailService;
		this.templateEngine = templateEngine;
	}

	private void sendOtpOnEMail(String email, OtpEntity otpEntity) {

		String emailSubject = "Your OTP Code for E-Mail Verification";
		Context thymeleafContext = new Context();
		thymeleafContext.setVariable("otpCode", otpEntity.getOtpCode());
		String emailHtmlBody = templateEngine.process("otp-code-on-email-template", thymeleafContext);

		emailService.sendEMail(email, emailSubject, emailHtmlBody);

	}

	@Override
	public OtpResponse otpRequest(OtpRequest sendOtpRequest) {

		log.info("Otp Request Service Invoked");
		log.info("Searching user by E-Mail in Database");
		Optional<UserEntity> userEntityOptional = userRepository.findByEmail(sendOtpRequest.getEmail());

		if (userEntityOptional.isEmpty()) {

			log.info("Queried User not found in Database");

			return OtpResponse
				.builder()
				.message("User does not exist. Please register")
				.build();

		} else {
			log.info("User found in Database");
		}

		OtpEntity otpEntity = randomOtpEntityCreator.create(userEntityOptional.get());
		log.info("Saving Otp Entity into Database");
		otpRepository.save(otpEntity);
		log.info("Otp Entity saved successfully into Database");

		try {
			sendOtpOnEMail(sendOtpRequest.getEmail(), otpEntity);
		} catch (Exception e) {

			log.info("Failure in sending Otp E-Mail to User");
			log.error("OtpService#otpRequest: " + e.getMessage());

			return OtpResponse
				.builder()
				.message("Cannot send Otp, an error occurred. Please Try Again")
				.build();

		}

		log.info("Otp E-Mail sent to User Successfully");

		return OtpResponse
			.builder()
			.message("Otp Sent Successfully")
			.build();

	}

	@Override
	public OtpResponse otpVerify(OtpRequest verifyOtpRequest) {

		log.info("Otp Request Service Invoked");
		String message = "User's E-Mail verified successfully.";
		log.info("Validating Otp with User's E-Mail in Database");
		Optional<OtpEntity> optionalOtpEntity = otpRepository.findValidOtpForUser(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtpCode());

		if (optionalOtpEntity.isPresent()) {

			log.info("Valid Otp Entity Found for User's E-Mail in Database");

			OtpEntity otpEntity = optionalOtpEntity.get();
			log.info("Updating Otp Entity: 'consumed' to TRUE in Database");
			otpEntity.setConsumed(Boolean.TRUE);
			otpRepository.save(otpEntity);
			log.info("Otp Entity fields updated");

			UserEntity userEntity = userRepository.findByEmail(verifyOtpRequest.getEmail()).get();
			log.info("Updating User Entity: 'email_verified' to TRUE, 'status' to ACTIVE and 'updated_timestamp' to NOW in Database");
			userEntity.setEmailVerified(Boolean.TRUE);
			userEntity.setStatus(UserStatus.ACTIVE);
			userEntity.setUpdatedTimestamp(Instant.now());
			userRepository.save(userEntity);
			log.info("User Entity fields updated");

			log.info("User's E-Mail Verified Successfully");

		} else {

			log.info("Valid Otp Entity Not Found for User's E-Mail in Database");
			message = "User's E-Mail cannot be verified. Please request Otp again.";
			log.info("User's E-Mail could not be Verified");

		}

		return OtpResponse
			.builder()
			.message(message)
			.build();

	}
	
}
