package com.samarthya_dev.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig {

	@Bean
	public TaskScheduler taskScheduler() {

		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(3);
		scheduler.setThreadNamePrefix("spring-scheduled-");
		scheduler.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
		scheduler.setAwaitTerminationMillis(30_000L);
		return scheduler;

	}
	
}
