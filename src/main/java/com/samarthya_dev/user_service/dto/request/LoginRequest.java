package com.samarthya_dev.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samarthya_dev.user_service.controller.flow_group.login.FlowLogin;
import com.samarthya_dev.user_service.controller.flow_group.login.FlowRefresh;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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
	@NotNull(message = "E-Mail cannot be NULL", groups = {FlowLogin.class, FlowRefresh.class})
	@NotEmpty(message = "E-Mail cannot be Empty")
	@JsonProperty("email")
	private String email;

	@NotNull(message = "Password cannot be NULL", groups = {FlowLogin.class})
	@JsonProperty("password")
	private String password;

	@NotNull(message = "Refresh Token cannot be Null", groups = {FlowRefresh.class})
	@NotEmpty(message = "Refresh Token cannot be Empty", groups = {FlowRefresh.class})
	@JsonProperty("refreshToken")
	private String refreshToken;
	
}
