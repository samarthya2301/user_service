package com.samarthya_dev.user_service.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samarthya_dev.user_service.dto.request.TestAuthRequest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
public class CheckAndTestController {

	@GetMapping(
		path = "${controller.paths.check-and-test.health-check:/healthCheck}",
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String healthCheck() {
		return "user-service API is up and running!";
	}

	@PostMapping(
		path = "${controller.paths.check-and-test.test-auth:/test-auth}",
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String postMethodName(@RequestBody @Valid TestAuthRequest testAuthRequest) {
		return "User Authentication Successful: " + testAuthRequest.getMessage();
	}
	
}
