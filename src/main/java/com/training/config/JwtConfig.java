package com.training.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.training.constants.CollectionConstants;
import com.training.model.KeyStorage;
import com.training.model.User;
import com.training.utils.EncryptionUtils;
import com.training.utils.JwtUtils;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

@Component
public class JwtConfig extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;

	public JwtConfig(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private EncryptionUtils encryptionUtils;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String requestTokenHeader = request.getHeader("Authorization");

		if (requestTokenHeader != null) {
			if (requestTokenHeader.startsWith("Bearer ")) {
				// Handle Bearer token authentication
				String jwtToken = requestTokenHeader.substring(7);
				String username = jwtUtils.extractUsername(jwtToken);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					if (jwtUtils.validateToken(jwtToken, username)) {
						SecurityContextHolder.getContext()
								.setAuthentication(jwtUtils.getAuthenticationFromToken(jwtToken));
					}
				}
			} else if (requestTokenHeader.startsWith("Basic ")) {
				// Handle Basic Authentication
				String basicCredentials = requestTokenHeader.substring(6);
				String decodedCredentials = new String(Base64.getDecoder().decode(basicCredentials));
				String[] credentials = decodedCredentials.split(":");

				String username = credentials[0];
				String password = credentials[1];

				// Perform authentication based on username and password
				if (authenticateUser(username, password)) {
					// Create an Authentication object and set it in SecurityContext
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username,
							password, Collections.emptyList());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean authenticateUser(String username, String password) {

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return false;
		}

		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(username));
		if (this.mongoTemplate.count(query, User.class) == 0) {
			return false;
		}

		User user = this.mongoTemplate.findOne(query, User.class);
		if (user != null) {
			KeyStorage keyStorage = this.mongoTemplate.findOne(query, KeyStorage.class,
					CollectionConstants.KEY_STORAGE);

			String encodedKey = keyStorage.getSecretKey();
			byte[] secretKeyBytes = Base64Utils.decodeFromString(encodedKey);
			SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");

			String decryptedPassword = this.encryptionUtils.decrypt(user.getPassword(), secretKey);

			if (StringUtils.equalsIgnoreCase(decryptedPassword, password)) {
				return true;
			}
		}
		return false;
	}

}
