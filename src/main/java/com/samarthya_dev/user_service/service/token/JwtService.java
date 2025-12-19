package com.samarthya_dev.user_service.service.token;

import com.samarthya_dev.user_service.entity.user.UserEntity;

public interface JwtService {

	String generateToken(UserEntity userEntity);
	String extractUserEmail(String token);
	Boolean isTokenValid(UserEntity userEntity, String token);
	
}
