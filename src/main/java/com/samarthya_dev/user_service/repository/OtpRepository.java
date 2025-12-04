package com.samarthya_dev.user_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samarthya_dev.user_service.entity.otp.OtpEntity;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {
	
	@Query("""
		SELECT otpEntity FROM OtpEntity otpEntity
		JOIN otpEntity.userId user
		WHERE user.email = :email
		AND otpEntity.otpCode = :otpCode
		AND otpEntity.consumed = false
		AND otpEntity.expiresTimestamp > CURRENT_TIMESTAMP
	""")
	Optional<OtpEntity> findValidOtpForUser(String email, String otpCode);

}
