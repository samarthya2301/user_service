package com.samarthya_dev.user_service.service.tegister;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.samarthya_dev.user_service.dto.request.RegisterRequest;
import com.samarthya_dev.user_service.dto.response.RegisterUserStatus;
import com.samarthya_dev.user_service.dto.response.ResgisterRespose;
import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderEntity;
import com.samarthya_dev.user_service.entity.auth_provider.AuthProviderType;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.repository.AuthProviderRepository;
import com.samarthya_dev.user_service.repository.UserRepository;
import com.samarthya_dev.user_service.service.register.AuthProviderForUserEntityCreator;
import com.samarthya_dev.user_service.service.register.RegisterRequestToUserEntityTransformation;
import com.samarthya_dev.user_service.service.register.RegisterServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RegisterServiceTest {

	@Mock
    private UserRepository userRepository;

    @Mock
    private AuthProviderRepository authProviderRepository;

    @Mock
    private RegisterRequestToUserEntityTransformation registerRequestToUserEntityTransformation;

    @Mock
    private AuthProviderForUserEntityCreator authProviderForUserEntityCreator;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private static final String TEST_EMAIL = "abc@xyz.com";
    private static final String TEST_HASHED_PASSWORD = "test.hashed.password";
	private RegisterRequest registerRequest;
	private UserEntity userEntity;

	@BeforeEach
	private void initializeData() {

        registerRequest = RegisterRequest
			.builder()
			.email(RegisterServiceTest.TEST_EMAIL)
			.password(RegisterServiceTest.TEST_HASHED_PASSWORD)
			.build();

        userEntity = UserEntity
			.builder()
			.email(RegisterServiceTest.TEST_EMAIL)
			.build();

	}

	@Test
	@DisplayName("User Register Success")
    public void testRegisterSuccess() {

        AuthProviderEntity authProviderEntity = AuthProviderEntity.builder().build();

        when(registerRequestToUserEntityTransformation.transform(registerRequest))
			.thenReturn(userEntity);

        when(userRepository.save(userEntity))
			.thenReturn(userEntity);

        when(authProviderForUserEntityCreator.create(userEntity, AuthProviderType.PASSWORD))
            .thenReturn(authProviderEntity);

        ResgisterRespose response = registerService.register(registerRequest);

        assertEquals(RegisterUserStatus.CREATED, response.getUserStatus());
        assertEquals("User Registered Successfully", response.getMessage());

        verify(registerRequestToUserEntityTransformation, times(1)).transform(registerRequest);
        verify(userRepository, times(1)).save(userEntity);
        verify(authProviderRepository, times(1)).save(authProviderEntity);

    }

    @Test
	@DisplayName("User Register Failure - E-Mail already exists")
    public void testRegisterEmailAlreadyExists() {

        when(registerRequestToUserEntityTransformation.transform(registerRequest))
            .thenReturn(userEntity);

        ConstraintViolationException constraintViolation = new ConstraintViolationException("E-Mail already exists", null, "email_unique_constraint");

        when(userRepository.save(userEntity))
			.thenThrow(new DataIntegrityViolationException("E-Mail already exists", constraintViolation));

        ResgisterRespose response = registerService.register(registerRequest);

        assertEquals(RegisterUserStatus.EXISTS, response.getUserStatus());
        assertEquals("E-Mail is already in use by another User.", response.getMessage());

        verify(authProviderRepository, never()).save(any());

    }

    @Test
	@DisplayName("User Register Failure - Other constraints violation")
    public void testRegisterOtherConstraintsViolation() {

        when(registerRequestToUserEntityTransformation.transform(registerRequest))
            .thenReturn(userEntity);

        ConstraintViolationException constraintViolation = new ConstraintViolationException("Other constraint violation", null, "unique_constraint");

        when(userRepository.save(userEntity))
            .thenThrow(new DataIntegrityViolationException("Constraint violation", constraintViolation));

        ResgisterRespose response = registerService.register(registerRequest);

        assertEquals(RegisterUserStatus.EXISTS, response.getUserStatus());
        assertEquals("User cannot be saved due to some Error.", response.getMessage());

        verify(authProviderRepository, never()).save(any());

    }

	@Test
	@DisplayName("User Register Failure - Auth Provider not created when save fails")
	public void testRegisterAuthProviderNotCreatedWhenUserSaveFails() {

	    when(registerRequestToUserEntityTransformation.transform(registerRequest))
	        .thenReturn(userEntity);

	    when(userRepository.save(userEntity))
	        .thenThrow(new DataIntegrityViolationException("exception"));

	    ResgisterRespose response = registerService.register(registerRequest);

	    assertEquals(RegisterUserStatus.EXISTS, response.getUserStatus());

	    verify(authProviderRepository, never()).save(any());

	}
	
}
