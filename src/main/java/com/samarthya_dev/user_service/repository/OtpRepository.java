package com.samarthya_dev.user_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samarthya_dev.user_service.entity.otp.OtpEntity;

import jakarta.transaction.Transactional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {
	
	@Query("""
		SELECT otpEntity FROM OtpEntity otpEntity
		JOIN otpEntity.user user
		WHERE user.email = :email
		AND otpEntity.otpCode = :otpCode
		AND otpEntity.consumed = false
		AND otpEntity.expiresTimestamp > CURRENT_TIMESTAMP
	""")
	Optional<OtpEntity> findValidOtpForUser(String email, Integer otpCode);

	@Transactional
	@Modifying
	@Query("""
		DELETE FROM OtpEntity otpEntity
		WHERE otpEntity.consumed = true
		OR otpEntity.expiresTimestamp < CURRENT_TIMESTAMP
	""")
	Integer deleteConsumedOrExpiredOtps();

}
