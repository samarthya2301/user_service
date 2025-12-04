package com.samarthya_dev.user_service.service.register;

import com.samarthya_dev.user_service.dto.request.RegisterRequest;
import com.samarthya_dev.user_service.dto.response.ResgisterRespose;

public interface RegisterService {
	
	ResgisterRespose register(RegisterRequest registerRequest);

}
