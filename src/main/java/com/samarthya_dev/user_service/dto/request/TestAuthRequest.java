package com.samarthya_dev.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
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
public class TestAuthRequest {

	@NotNull(message = "Test Message cannot be Null")
	@NotEmpty(message = "Test Message cannot be Empty")
	@JsonProperty("message")
	private String message;
	
}
