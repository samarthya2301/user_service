package com.samarthya_dev.user_service.service.login;

import com.samarthya_dev.user_service.dto.request.LoginRequest;
import com.samarthya_dev.user_service.dto.response.LoginResponse;

public interface LoginService {

	LoginResponse login(LoginRequest loginRequest);
	LoginResponse refresh(LoginRequest refreshRequest);
	
}
