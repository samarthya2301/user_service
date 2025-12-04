package com.samarthya_dev.user_service.entity.otp;

import java.time.Instant;
import java.util.UUID;

import com.samarthya_dev.user_service.entity.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "otp")
public class OtpEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private UserEntity userId;

	@Column(name = "otp_code", length = 6, nullable = false)
	private Integer otpCode;

	@Column(name = "created_timestamp", nullable = false)
	private Instant createdTimestamp;

	@Column(name = "expires_timestamp", nullable = false)
	private Instant expiresTimestamp;

	@Column(name = "consumed", nullable = false)
	private Boolean consumed;
	
}
