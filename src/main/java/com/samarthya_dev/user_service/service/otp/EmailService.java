package com.samarthya_dev.user_service.service.otp;

public interface EmailService {

	void sendEMail(String emailTo, String subjectData, String bodyData);

}
