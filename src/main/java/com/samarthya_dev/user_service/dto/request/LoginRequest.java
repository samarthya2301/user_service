package com.samarthya_dev.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@ToString(exclude = {"password"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {

	@Email(message = "Invalid E-Mail Format")
	@NotNull(message = "E-Mail cannot be NULL")
	@JsonProperty("email")
	private String email;

	@NotNull(message = "Password cannot be NULL")
	@JsonProperty("password")
	private String password;
	
}
