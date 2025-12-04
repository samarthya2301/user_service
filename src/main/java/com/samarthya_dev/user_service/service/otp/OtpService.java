package com.samarthya_dev.user_service.service.otp;

import com.samarthya_dev.user_service.dto.request.OtpRequest;
import com.samarthya_dev.user_service.dto.response.OtpResponse;

public interface OtpService {

	OtpResponse otpRequest(OtpRequest sendOtpRequest);
	OtpResponse otpVerify(OtpRequest verifyOtpRequest);

}
