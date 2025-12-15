package com.samarthya_dev.user_service.controller;

import org.springframework.web.bind.annotation.RestController;

import com.samarthya_dev.user_service.controller.flow_group.otp.FlowRequestOtp;
import com.samarthya_dev.user_service.controller.flow_group.otp.FlowVerifyOtp;
import com.samarthya_dev.user_service.dto.request.OtpRequest;
import com.samarthya_dev.user_service.dto.response.OtpResponse;
import com.samarthya_dev.user_service.service.otp.OtpService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
public class OtpController {

	@Autowired
	private final OtpService otpService;

	/**
	 * Bean initialization constructor
	 * @param otpService
	 */
	OtpController(
		OtpService otpService
	) {
		this.otpService = otpService;
	}

	@PostMapping(path = "/otp/request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OtpResponse> otpRequest(@RequestBody @Validated(FlowRequestOtp.class) OtpRequest sendOtpRequest) {

		log.info("Request Invoked on Endpoint: /otp/request");
		log.debug(sendOtpRequest.toString());

		OtpResponse sendOtpResponse = otpService.otpRequest(sendOtpRequest);
		log.info("Request Processed for Endpont: /otp/request");
		log.debug(sendOtpResponse.toString());

		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(MediaType.APPLICATION_JSON)
			.body(sendOtpResponse);

	}

	@PostMapping(path = "/otp/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OtpResponse> otpVerify(@RequestBody @Validated(FlowVerifyOtp.class) OtpRequest verifyOtpRequest) {

		log.info("Request Invoked on Endpoint: /otp/verify");
		log.debug(verifyOtpRequest.toString());

		OtpResponse verifyOtpResponse = otpService.otpVerify(verifyOtpRequest);
		log.info("Request Processed for Endpont: /otp/verify");
		log.debug(verifyOtpResponse.toString());

		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(MediaType.APPLICATION_JSON)
			.body(verifyOtpResponse);

	}

}
