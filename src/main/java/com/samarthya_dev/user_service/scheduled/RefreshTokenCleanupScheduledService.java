package com.samarthya_dev.user_service.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduledService {

	private final RefreshTokenRepository refreshTokenRepository;
	
	/**
	 * Scheduled method to cleap up expired Refresh Tokens
	 * Method will run every midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void cleanupConsumedOrExpiredOtps() {

		Integer refreshTokensCleanedUpCount = refreshTokenRepository.deleteRevokedOrExpiredRefreshTokens();
		log.info("Revoked and Expired Refresh Tokens Deleted: {}", refreshTokensCleanedUpCount);

	}
}
