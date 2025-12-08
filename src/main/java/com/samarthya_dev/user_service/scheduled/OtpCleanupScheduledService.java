package com.samarthya_dev.user_service.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.repository.OtpRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OtpCleanupScheduledService {

	@Autowired
	private final OtpRepository otpRepository;

	public OtpCleanupScheduledService(OtpRepository otpRepository) {
		this.otpRepository = otpRepository;
	}

	@Scheduled(fixedRate = 1_800_000L)
	public void cleanupConsumedOrExpiredOtps() {

		Integer otpsCleanedUpCount = otpRepository.deleteConsumedOrExpiredOtps();
		log.info("Consumed and Expired OTPs Deleted: {}", otpsCleanedUpCount);

	}
	
}
