Core Entities
=============
[README - Core Entities](./src/main/java/com/samarthya_dev/user_service/entity/README.md)

---

Registration Flow
=================

User Registration
-----------------
1. Request invoked at '/register' endpoint in RegisterController.java
2. RegisterRequest body contains - email, phone_number (optional), password
3. RegisterRequest is converted into UserEntity in RegisterRequestToUserEntityTransformation
4. UserEntity is saved to repository and send back as a message with CREATED and 201 Http Status
5. Any constraint violations are reported and sent back as a message with EXISTS and 200 Http Status
6. Create an AuthProvider entity because user should be able to use password (only) for auth now.
	* select * from users u inner join auth_provider a on u.id = a.user_id;
	* select * from users u inner join auth_provider a on u.id = a.user_id where u.email = '<emailtosearch>';
7. Returned with 201 Created on success

Verification of E-Mail/Phone Number
----------------------------------
1. Request invoked from frontend at '/otp/request' endpoint in OtpController.java
2. SendOtpRequest body contains - email, phone_number (optional), channel (EMAIL/PHONE)
	* If channel == EMAIL, otp to be sent on email, 'email' field is mandatory
	* If channel == PHONE, otp to be sent on phone number, 'email' and 'phone_number' are mandatory
3. The existence of user is validated with email. If not present, message to register is sent
4. Otp for a valid user is created and saved to the database with expiy of 5 minutes later than its creation.
5. Otp record is linked with user with the help of user_id and saved to the database.
	* select o.otp_code from users u inner join otp o on u.id = o.user_id;
	* select o.otp_code from users u inner join otp o on u.id = o.user_id where u.email = '<emailtosearch>';
6. Otp is sent on the requested channel (EMAIL/PHONE) through SES/SNS respectively.