package com.samarthya_dev.user_service.service.token;

import com.samarthya_dev.user_service.entity.user.UserEntity;

public interface RefreshService {

	String generateToken(UserEntity userEntity);
	
}
