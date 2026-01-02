package com.samarthya_dev.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.samarthya_dev.user_service.dto.request.RegisterRequest;
import com.samarthya_dev.user_service.dto.response.RegisterUserStatus;
import com.samarthya_dev.user_service.dto.response.ResgisterRespose;
import com.samarthya_dev.user_service.service.register.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RegisterController {

	private final RegisterService registerService;

	/**
	 * Registers a user with E-Mail and Password. Phone Number is optional
	 * @param request E-Mail (Required), Password (Required), Phone Number (Optional)
	 * @return Response Message
	 */
	@PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResgisterRespose> register(@RequestBody @Valid RegisterRequest request) {

		log.info("Request Invoked on Endpoint: /register");
		log.debug(request.toString());

		ResgisterRespose resgisterRespose = registerService.register(request);
		log.info("Request Processed for Endpont: /register");
		log.debug(resgisterRespose.toString());

		return ResponseEntity
			.status(resgisterRespose.getUserStatus().equals(RegisterUserStatus.CREATED) ? HttpStatus.CREATED : HttpStatus.OK)
			.contentType(MediaType.APPLICATION_JSON)
			.body(resgisterRespose);

	}
	
}
