package com.samarthya_dev.user_service.entity.auth_provider;

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
@Table(name = "auth_provider")
public class AuthProviderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private UserEntity user;

	@Column(name = "provider", nullable = false)
	private AuthProviderType provider;

	@Column(name = "provider_details_json")
	private String providerDetailsJson;

	@Column(name = "created_timestamp", nullable = false)
	private Instant createdTimestamp;
	
}
