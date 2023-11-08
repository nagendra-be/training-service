package com.training.service.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import com.training.constants.CollectionConstants;
import com.training.constants.TrainingConstants;
import com.training.model.CreateUserRequest;
import com.training.model.KeyStorage;
import com.training.model.User;
import com.training.model.UpdateUserRequest;
import com.training.service.ReferralService;
import com.training.service.UserService;
import com.training.utils.EncryptionUtils;
import com.training.utils.JwtUtils;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private EncryptionUtils encryptionUtils;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private ReferralService referralService;

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public ResponseEntity<?> createUser(CreateUserRequest request) {

		logger.info("Inside create user...");

		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(request.getEmail()));
		if (this.mongoTemplate.count(query, User.class) > 0) {
			return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
		}

		User user = new User();
		BeanUtils.copyProperties(request, user);
		SecretKey secretKey = this.encryptionUtils.generateSecretKey();
		String encryptedPassword = this.encryptionUtils.encrypt(request.getPassword(), secretKey);
		user.setPassword(encryptedPassword);
		user.setRole(TrainingConstants.USER);
		user.setUniqueId(UUID.randomUUID().toString());
		user.setStatus(TrainingConstants.ACTIVE);
		user.setReferralId(this.referralService.generateUniqueReferralId());

		// storing secret key separately
		KeyStorage keyStorage = new KeyStorage();
		keyStorage.setEmail(request.getEmail());
		byte[] secretKeyBytes = secretKey.getEncoded();
		String encodedSecretKey = Base64Utils.encodeToString(secretKeyBytes);
		keyStorage.setSecretKey(encodedSecretKey);
		logger.info("Generated secret key for the user- {}", user.getEmail());
		this.mongoTemplate.save(keyStorage);

		logger.info("Saving user -{} into db", user.getEmail());
		this.mongoTemplate.save(user);

		return new ResponseEntity<>("User successfully created", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUsers(String searchInput) {
		logger.info("Querying get all active users in db....");
		Query query = new Query();
		if (StringUtils.isNotEmpty(searchInput)) {
			query = this.getSearchQuery(searchInput);
		}
		query.addCriteria(Criteria.where("status").is(TrainingConstants.ACTIVE));
		List<User> customers = this.mongoTemplate.find(query, User.class);
		if (!CollectionUtils.isEmpty(customers)) {
			return new ResponseEntity<>(customers, HttpStatus.OK);
		} else {
			logger.info("No users found in users collection...");
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getUser(String userId) {
		logger.info("Querying info for userId- {}", userId);
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(userId));
		User user = this.mongoTemplate.findOne(query, User.class);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		} else {
			logger.info("No user found - {}", userId);
			return new ResponseEntity<>(new User(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> updateUser(UpdateUserRequest request) {
		logger.info("Updating user info for user - {}", request.getEmail());
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(request.getEmail()));
		User user = this.mongoTemplate.findOne(query, User.class);
		if (user != null) {
			if (request.getFirstName() != null) {
				user.setFirstName(request.getFirstName());
			}
			if (request.getLastName() != null) {
				user.setLastName(request.getLastName());
			}
			if (request.getEmail() != null) {
				user.setEmail(request.getEmail());
			}
			if (request.getPhone() != null) {
				user.setPhone(request.getPhone());
			}
			if (request.getAddress() != null) {
				user.setAddress(request.getAddress());
			}

			if (request.getPassword() != null) {
				logger.info("Updating password for user -{}", user.getEmail());
				KeyStorage keyStorage = this.mongoTemplate.findOne(query, KeyStorage.class,
						CollectionConstants.KEY_STORAGE);
				SecretKey secretKey;
				if (keyStorage != null) {
					String encodedKey = keyStorage.getSecretKey();
					byte[] secretKeyBytes = Base64Utils.decodeFromString(encodedKey);
					secretKey = new SecretKeySpec(secretKeyBytes, "AES");
				} else {
					keyStorage = new KeyStorage();
					secretKey = this.encryptionUtils.generateSecretKey();
					keyStorage.setEmail(request.getEmail());
					byte[] secretKeyBytes = secretKey.getEncoded();
					String encodedSecretKey = Base64Utils.encodeToString(secretKeyBytes);
					keyStorage.setSecretKey(encodedSecretKey);
					this.mongoTemplate.save(keyStorage);
				}
				String encryptedPassword = this.encryptionUtils.encrypt(request.getPassword(), secretKey);
				System.out.println("Encrypted password - " + encryptedPassword);
				user.setPassword(encryptedPassword);
			}

			this.mongoTemplate.save(user);
			return new ResponseEntity<>("User is successfully updated", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No User found with Id- " + request.getEmail(), HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> deleteUser(String email) {
		logger.info("Deleting user with email -{}", email);
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(email));
		User user = this.mongoTemplate.findOne(query, User.class);
		if (user != null) {
			user.setStatus(TrainingConstants.DELETED);
			this.mongoTemplate.save(user);
			// Removing corresponding secretkey
			this.mongoTemplate.remove(query, KeyStorage.class);
			return new ResponseEntity<>("User " + email + " is successfully deleted", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No Customer found with Id-" + email, HttpStatus.NOT_FOUND);
		}
	}

	private Query getSearchQuery(String searchInput) {
		Query query = new Query();
		List<Criteria> criterias = new LinkedList<>();
		Criteria searchCriteria = new Criteria();
		searchCriteria.orOperator(
				Criteria.where("customerId")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("firstName")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("email")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("city")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("state")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("country")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("lastName")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("zipCode")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("phone")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));
		criterias.add(searchCriteria);
		if (!CollectionUtils.isEmpty(criterias)) {
			Criteria criteria = new Criteria();
			criteria.andOperator(criterias.stream().toArray(Criteria[]::new));
			query.addCriteria(criteria);
		}
		return query;
	}

	@Override
	public ResponseEntity<?> loginAuthentication(String username, String password) {
		logger.info("Authenticating user -{}", username);
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return new ResponseEntity<>("Username or Password should not be blank", HttpStatus.BAD_REQUEST);
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(username));
		if (this.mongoTemplate.count(query, User.class) == 0) {
			return new ResponseEntity<>("Email doesn't exist", HttpStatus.BAD_REQUEST);
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
				logger.info("verifying if correct credentials...");
				// Check if the current token has expired
				if (StringUtils.isEmpty(user.getToken()) || jwtUtils.isTokenExpired(user.getToken())) {
					logger.info("Token is either empty or expired");

					// Token has expired or empty, generate a new token
					String jwtToken = jwtUtils.generateToken(user.getEmail());

					logger.info("Generated token : {} ", jwtToken);

					// Update the token and its expiration in the user object
					user.setToken(jwtToken);
					user.setTokenExpiry(jwtUtils.extractExpiration(jwtToken));

					this.mongoTemplate.save(user);
				}

				return new ResponseEntity<>(user, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Invalid credentials", HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>("Email Id doesn't exist", HttpStatus.FORBIDDEN);
		}
	}
}
