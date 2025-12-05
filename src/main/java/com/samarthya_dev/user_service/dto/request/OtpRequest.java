package com.samarthya_dev.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samarthya_dev.user_service.controller.flow_group.FlowRequestOtp;
import com.samarthya_dev.user_service.controller.flow_group.FlowVerifyOtp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpRequest {

	@Email(message = "Invalid E-Mail Format")
	@NotNull(message = "E-Mail cannot be NULL", groups = {FlowRequestOtp.class, FlowVerifyOtp.class})
	@JsonProperty("email")
	private String email;
	
	@Min(value = 100000, message = "OTP should be of size 6")
	@Max(value = 999999, message = "OTP should be of size 6")
	@NotNull(message = "OTP cannot be NULL.", groups = {FlowVerifyOtp.class})
	@JsonProperty("otp_code")
	private Integer otpCode;

}
