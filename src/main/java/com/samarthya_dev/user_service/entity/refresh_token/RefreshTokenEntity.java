package com.samarthya_dev.user_service.entity.refresh_token;

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
@Table(name = "refresh_token")
public class RefreshTokenEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private UserEntity userId;

	@Column(name = "token", length = 512, nullable = false)
	private String token;

	@Column(name = "created_timestamp", nullable = false)
	private Instant createdTimestamp;

	@Column(name = "expires_timestamp", nullable = false)
	private Instant expiresTimestamp;

	@Column(name = "revoked")
	private Boolean revoked;

}
