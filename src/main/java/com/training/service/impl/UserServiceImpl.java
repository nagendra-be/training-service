package com.training.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import com.training.constants.CollectionConstants;
import com.training.constants.TrainingConstants;
import com.training.model.CreateUserRequest;
import com.training.model.CreditsDetails;
import com.training.model.KeyStorage;
import com.training.model.User;
import com.training.model.UpdateUserRequest;
import com.training.service.EmailService;
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

	@Autowired
	private EmailService emailService;

	@Value("${referral.amount}")
	private int referralAmount;

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

		// Adding credits to the referrer
		if (StringUtils.isNotEmpty(request.getReferredBy())) {
			this.addCredits(request.getReferredBy());
		}

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

	@Override
	public ResponseEntity<?> verifyEmail(String uniqueId) {
		// Query for the user with the unique token
		Query query = new Query();
		query.addCriteria(Criteria.where("uniqueId").is(uniqueId));
		User user = this.mongoTemplate.findOne(query, User.class);

		// Check if the user exists and the token is valid
		if (user != null) {
			if (user.getEmailExpiry().after(new Date())) {
				// Mark the email as verified
				user.setEmailVerified(true);

				// Save the updated user
				this.mongoTemplate.save(user);

				return new ResponseEntity<>("Email verification successful", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Email link expired", HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>("Invalid email verification token", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> sendEmail(String userId, String operation) {

		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(userId));
		User user = mongoTemplate.findOne(query, User.class);

		if (user != null) {
			user.setEmailExpiry(this.calculateEmailTokenExpiry());
			this.mongoTemplate.save(user);

			Map<String, String> emailContent = this.getEmailContent(user, operation);

			// Sending the verification email
			emailService.send("no-reply@suchiit.com", user.getEmail(), null, null, emailContent.get("subject"),
					emailContent.get("text"));

			return new ResponseEntity<>("Verification email sent successfully", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
	}

	private Date calculateEmailTokenExpiry() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, 2);
		return calendar.getTime();
	}

	private Map<String, String> getEmailContent(User user, String operation) {
		// Preparing the verification email content based on operation
		String subject = "";
		String text = "";

		if (TrainingConstants.VERIFY_EMAIL.equalsIgnoreCase(operation)) {
			subject = "Email Verification SITC - Action Required";
			text = "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n"
					+ "Thank you for choosing our platform. To complete your registration, please click the following link to verify your email address:\n"
					+ "http://sitc.com/verify?token=" + user.getUniqueId()
					+ "\n\nNote: This link will expire in 2 hours.\n\n"
					+ "If you did not sign up for our platform, please ignore this email.\n\n"
					+ "Best Regards,\nThe SITC Team";
		} else if (TrainingConstants.FORGOT_PASSWORD.equalsIgnoreCase(operation)) {
			subject = "Password Reset SITC - Action Required";
			text = "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n"
					+ "We received a request to reset your password. Click the following link to reset your password:\n"
					+ "http://sitc.com/reset-password?token=" + user.getUniqueId()
					+ "\n\nNote: This link will expire in 2 hours.\n\n"
					+ "If you did not request a password reset, please ignore this email.\n\n"
					+ "Best Regards,\nThe SITC Team";
		}

		Map<String, String> emailContent = new HashMap<>();
		emailContent.put("subject", subject);
		emailContent.put("text", text);

		return emailContent;
	}

	@Override
	@Async
	public void calculateRemainingCredits(String userId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(userId));

		List<CreditsDetails> creditsDetailsList = mongoTemplate.find(query, CreditsDetails.class);

		int remainingCredits = 0;

		if (CollectionUtils.isEmpty(creditsDetailsList)) {
			logger.info("There are no credits transactions found for the user- {}", userId);
			return;
		} else {
			for (CreditsDetails creditsDetails : creditsDetailsList) {
				if (TrainingConstants.ADD.equals(creditsDetails.getType())) {
					remainingCredits += creditsDetails.getAmount();
				} else if (TrainingConstants.USE.equals(creditsDetails.getType())) {
					remainingCredits -= creditsDetails.getAmount();
				}
			}

			User user = this.mongoTemplate.findOne(query, User.class);
			if (user != null) {
				user.setCredits(remainingCredits);
				this.mongoTemplate.save(user);
				logger.info("Successfully saved latest credits for user - {}", userId);
			} else {
				logger.info("User not found -{}", userId);
				return;
			}

		}

	}

	private void addCredits(String referredBy) {
		Query query = new Query();
		query.addCriteria(Criteria.where("referralId").is(referredBy));

		User user = this.mongoTemplate.findOne(query, User.class);

		if (user != null) {
			CreditsDetails creditsDetails = new CreditsDetails();
			creditsDetails.setAmount(this.referralAmount);
			creditsDetails.setType(TrainingConstants.ADD);
			creditsDetails.setUserId(user.getEmail());
			creditsDetails.setTransactionDate(new Date());
			this.mongoTemplate.save(creditsDetails);

			logger.info("Successfully captured referral credits into db for user- {}", user.getEmail());

			this.calculateRemainingCredits(user.getEmail());
		} else {
			logger.info("Invalid referral code used- {}", referredBy);
		}

	}

}