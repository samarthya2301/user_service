package com.samarthya_dev.user_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samarthya_dev.user_service.entity.refresh_token.RefreshTokenEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

	@Query("""
		SELECT refreshToken FROM RefreshTokenEntity refreshToken
		JOIN refreshToken.user user
		WHERE user.email = :email
		AND refreshToken.token = :refreshToken
		AND refreshToken.expiresTimestamp > CURRENT_TIMESTAMP
		AND refreshToken.revoked = false
	""")
	Optional<RefreshTokenEntity> findValidRefreshTokenForUser(String email, String refreshToken);
	
}
