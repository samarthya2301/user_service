package com.samarthya_dev.user_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderEntity;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProviderEntity, UUID> {
	
}
