package com.samarthya_dev.user_service.filter;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.samarthya_dev.user_service.entity.user.UserEntity;
import com.samarthya_dev.user_service.repository.UserRepository;
import com.samarthya_dev.user_service.service.token.JwtService;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);
			return;

		}

		try {

			String accessToken = authHeader.substring(7);
			String userEmail = jwtService.extractUserEmail(accessToken);
			Optional<UserEntity> userEntityOptional = userRepository.findByEmail(userEmail);

			userEntityOptional
				.map(userEntity -> {

					if (SecurityContextHolder.getContext().getAuthentication() == null) {
						if (jwtService.isTokenValid(userEntity, accessToken)) {

							UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
								userEntity,
								null,
								userEntity
									.getRole()
									.stream()
									.map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.name()))
									.collect(Collectors.toList())
							);

							authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(authToken);

						}
					}

					return userEntity;

				});

		} catch (NullPointerException e) {

			log.info("A required entity was null");
			log.error("JwtAuthenticationFilter#doFilterInternal: " + e.getMessage());

		} catch (StringIndexOutOfBoundsException e) {

			log.info("Bearer token is Invalid");
			log.error("JwtAuthenticationFilter#doFilterInternal: " + e.getMessage());

		} catch (MalformedJwtException e) {

			log.info("JWT Access Token is Malformed");
			log.error("JwtAuthenticationFilter#doFilterInternal: " + e.getMessage());

		} catch (RuntimeException e) {

			log.info("General Error in JWT Authentication Filter");
			log.error("JwtAuthenticationFilter#doFilterInternal: " + e.getMessage());

		}

		filterChain.doFilter(request, response);

	}
	
}
