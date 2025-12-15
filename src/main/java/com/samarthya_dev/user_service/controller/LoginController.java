package com.samarthya_dev.user_service.controller;

import org.springframework.web.bind.annotation.RestController;

import com.samarthya_dev.user_service.controller.flow_group.login.FlowLogin;
import com.samarthya_dev.user_service.controller.flow_group.login.FlowRefresh;
import com.samarthya_dev.user_service.dto.request.LoginRequest;
import com.samarthya_dev.user_service.dto.response.LoginResponse;
import com.samarthya_dev.user_service.service.login.LoginService;

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
public class LoginController {

	@Autowired
	private final LoginService loginService;

	LoginController(LoginService loginService) {
		this.loginService = loginService;
	}

	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> login(@RequestBody @Validated(FlowLogin.class) LoginRequest loginRequest) {

		log.info("Request Invoked on Endpoint: /login");
		log.debug(loginRequest.toString());

		LoginResponse loginResponse = loginService.login(loginRequest);
		log.info("Request Processed for Endpont: /login");
		log.debug(loginResponse.toString());

		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(MediaType.APPLICATION_JSON)
			.body(loginResponse);

	}

	@PostMapping(path = "/refresh-token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> refresh(@RequestBody @Validated(FlowRefresh.class) LoginRequest refreshRequest) {

		log.info("Request Invoked on Endpoint: /refresh-token");
		log.debug(refreshRequest.toString());

		LoginResponse refreshResponse = loginService.refresh(refreshRequest);
		log.info("Request Processed for Endpont: /refresh-token");
		log.debug(refreshResponse.toString());

		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(MediaType.APPLICATION_JSON)
			.body(refreshResponse);

	}
	
}
