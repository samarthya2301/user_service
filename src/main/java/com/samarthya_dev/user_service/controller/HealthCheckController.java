package com.samarthya_dev.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@GetMapping(path = "/healthCheck")
	public String healthCheck() {
		System.out.println("Invoked Health Check");
		return "user-service API is up and running!";
	}
	
}
