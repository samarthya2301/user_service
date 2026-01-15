package com.samarthya_dev.user_service.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.repository.OtpRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpCleanupScheduledService {

	private final OtpRepository otpRepository;

	/**
	 * Scheduled method to cleap up consumed or expired OTPs
	 * Method will run every 15 minutes
	 */
	@Scheduled(cron = "0 0/15 * * * *")
	public void cleanupConsumedOrExpiredOtps() {

		Integer otpsCleanedUpCount = otpRepository.deleteConsumedOrExpiredOtps();
		log.info("Consumed and Expired OTPs Deleted: {}", otpsCleanedUpCount);

	}
	
}
