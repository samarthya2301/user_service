package com.samarthya_dev.user_service.service.register;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.samarthya_dev.user_service.dto.request.RegisterRequest;
import com.samarthya_dev.user_service.dto.response.RegisterUserStatus;
import com.samarthya_dev.user_service.dto.response.ResgisterRespose;
import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderEntity;
import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderType;
import com.samarthya_dev.user_service.entity.role.RoleEntity;
import com.samarthya_dev.user_service.entity.role.RoleName;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.repository.AuthProviderRepository;
import com.samarthya_dev.user_service.repository.RoleRepository;
import com.samarthya_dev.user_service.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RegisterServiceImpl implements RegisterService {

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private final AuthProviderRepository authProviderRepository;

	@Autowired
    private final RoleRepository roleRepository;

	@Autowired
	private final RegisterRequestToUserEntityTransformation registerRequestToUserEntityTransformation;

	@Autowired
	private final AuthProviderForUserEntityCreator authProviderForUserEntityCreator;

	@Autowired
	private final RoleForUserEntityCreator roleForUserEntityCreator;

	RegisterServiceImpl(
		UserRepository userRepository,
		AuthProviderRepository authProviderRepository,
		RoleRepository roleRepository,
		RegisterRequestToUserEntityTransformation registerRequestToUserEntityTransformation,
		AuthProviderForUserEntityCreator authProviderForUserEntityCreator,
		RoleForUserEntityCreator roleForUserEntityCreator
	) {

		this.userRepository = userRepository;
		this.authProviderRepository = authProviderRepository;
		this.roleRepository = roleRepository;
		this.registerRequestToUserEntityTransformation = registerRequestToUserEntityTransformation;
		this.authProviderForUserEntityCreator = authProviderForUserEntityCreator;
		this.roleForUserEntityCreator = roleForUserEntityCreator;

	}

	/**
	 * Transforms RegisterRequest into UserEntity
	 * Saves the created UserEntity with additional fields needed in database
	 * Creates AuthProviderEntity with provider PASSWORD for the UserEntity
	 * Saves the created AuthProviderEntity with additional fields needed in database
	 * Handles already existing email or phone number
	 * @param registerRequest From RegisterController through '/register' endpoint
	 * @return Response Object
	 */
	@Override
	public ResgisterRespose register(RegisterRequest registerRequest) {

		log.info("Register Service Invoked");

		String message = "User Registered Successfully";
		RegisterUserStatus registerUserStatus = RegisterUserStatus.CREATED;

		try {

			UserEntity userEntity = registerRequestToUserEntityTransformation.transform(registerRequest);
			log.info("Saving User Entity into Database");
			userEntity = userRepository.save(userEntity);
			log.info("User Entity saved successfully into Database");

			AuthProviderEntity authProviderEntity = authProviderForUserEntityCreator.create(userEntity, AuthProviderType.PASSWORD);
			log.info("Saving Auth Provider Entity into Database");
			authProviderRepository.save(authProviderEntity);
			log.info("Auth Provider Entity saved successfully into Database");

			RoleEntity roleEntity = roleForUserEntityCreator.create(userEntity, RoleName.USER);
			log.info("Saving Role Entity into Database");
			roleRepository.save(roleEntity);
			log.info("Role Entity saved successfully into Database");

			return ResgisterRespose
				.builder()
				.userStatus(registerUserStatus)
				.message(message)
				.build();

		} catch (DataIntegrityViolationException e) {

			log.info("Failure in saving User Entity into Database");
			log.error("RegisterService#register: " + e.getMessage());

			Throwable cause = e.getCause();
			registerUserStatus = RegisterUserStatus.EXISTS;
			if (cause instanceof ConstraintViolationException cveCause) {
				if (cveCause.getConstraintName().contains("email")) {
					message = "E-Mail is already in use by another User.";
				} else {
					message = "User cannot be saved due to some Error.";
				}
			}

		}

		return ResgisterRespose
			.builder()
			.userStatus(registerUserStatus)
			.message(message)
			.build();

	}
	
}
