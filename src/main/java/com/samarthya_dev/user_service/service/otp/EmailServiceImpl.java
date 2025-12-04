package com.samarthya_dev.user_service.service.otp;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Slf4j
@Component
public class EmailServiceImpl implements EmailService {

	@Override
	public void sendEMail(String emailTo, String subjectData, String bodyData) {

		log.info("E-Mail Service Invoked");

		// TODO: Hardcoding email for aws sandbox environment
		emailTo = "samarthya2301+aws1+receiver@gmail.com";

		Destination destination = Destination.builder()
			.toAddresses(emailTo)
			.build();

		Content subject = Content.builder()
			.data(subjectData)
			.build();

		Content htmlBody = Content.builder()
			.data(bodyData)
			.build();

		Message message = Message.builder()
			.subject(subject)
			.body(Body.builder().html(htmlBody).build())
			.build();

		SendEmailRequest emailRequest = SendEmailRequest.builder()
			.destination(destination)
			.message(message)
			.source("expenseintelligence@samarthya2301.in")
			.build();

		log.info("Sending E-Mail from {} to {}", emailRequest.source(), emailRequest.destination().toAddresses());
		
		SesClient
			.builder()
			.region(Region.US_EAST_1)
			.build()
			.sendEmail(emailRequest);

	}
	
}
