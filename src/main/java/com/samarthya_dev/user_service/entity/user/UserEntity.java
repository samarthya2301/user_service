package com.samarthya_dev.user_service.entity.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "email_verified", nullable = false)
	private Boolean emailVerified;

	@Column(name = "hashed_password", nullable = false)
	private String hashedPassword;

	@Column(name = "role", nullable = false)
	private List<UserRole> role;

	@Column(name = "status", nullable = false)
	private UserStatus status;

	@Column(name = "created_timestamp", nullable = false)
	private Instant createdTimestamp;

	@Column(name = "updated_timestamp")
	private Instant updatedTimestamp;

}
