package com.training.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.training.constants.TrainingConstants;
import com.training.model.CreatePaymentRequest;
import com.training.model.CreditsDetails;
import com.training.model.PaymentDetails;
import com.training.model.UserCourseMapping;
import com.training.service.TransactionService;
import com.training.service.UserService;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

	@Override
	public ResponseEntity<?> createUserCourseTransaction(CreatePaymentRequest request) {
		logger.info("Received new payment.. create corresponding records in db- {}", request.getTransactionId());

		if (!request.isMembershipTransaction()) {
			logger.info("received payment for course -{} by user- {}", request.getCourseId(), request.getUserId());
			UserCourseMapping userCourseMapping = new UserCourseMapping();
			userCourseMapping.setCourseId(request.getCourseId());
			userCourseMapping.setEnrollmentDate(new Date());
			userCourseMapping.setUserId(request.getUserId());
			userCourseMapping.setExpiryDate(this.getExpiryTime(userCourseMapping.getEnrollmentDate()));
			userCourseMapping.setStatus(TrainingConstants.ACTIVE);
			this.mongoTemplate.save(userCourseMapping);
		} else {
			logger.info("received payment for premium membership for user- {}", request.getUserId());
		}

		PaymentDetails paymentDetails = new PaymentDetails();
		paymentDetails.setMembershipTransaction(request.isMembershipTransaction());
		paymentDetails.setAmount(request.getAmount());
		paymentDetails.setCreditsUsed(request.getCreditsUsed());
		paymentDetails.setUserId(request.getUserId());
		paymentDetails.setPaymentDate(request.getTransactionDate());
		paymentDetails.setPaymentMode(request.getPaymentMode());
		paymentDetails.setTransactionId(request.getTransactionId());
		if (StringUtils.isNotEmpty(request.getCourseId())) {
			paymentDetails.setCourseId(request.getCourseId());
		}

		this.mongoTemplate.save(paymentDetails);

		if (request.getCreditsUsed() > 0) {
			CreditsDetails creditsDetails = new CreditsDetails();
			creditsDetails.setAmount(request.getCreditsUsed());
			creditsDetails.setCourseId(request.getCourseId());
			creditsDetails.setTransactionDate(request.getTransactionDate());
			creditsDetails.setUserId(request.getUserId());
			creditsDetails.setType(TrainingConstants.USE);
			this.mongoTemplate.save(creditsDetails);

			this.userService.calculateRemainingCredits(request.getUserId());

		}

		return new ResponseEntity<>("Payment details successfully recorded", HttpStatus.OK);
	}

	private Date getExpiryTime(Date enrollmentDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(enrollmentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		return expiryDate;
	}

	@Override
	public ResponseEntity<?> getTransactions(String searchInput) {
		logger.info("Querying get all transactions in db....");
		Query query = new Query();
		if (StringUtils.isNotEmpty(searchInput)) {
			query = this.getSearchQuery(searchInput);
		}
		List<PaymentDetails> payments = this.mongoTemplate.find(query, PaymentDetails.class);
		if (!CollectionUtils.isEmpty(payments)) {
			return new ResponseEntity<>(payments, HttpStatus.OK);
		} else {
			logger.info("No records found in payments collection...");
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getTransactionsByUser(String userId) {
		logger.info("Querying get all transactions in db for userId-{}", userId);
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userId));
		List<PaymentDetails> payments = this.mongoTemplate.find(query, PaymentDetails.class);
		if (!CollectionUtils.isEmpty(payments)) {
			return new ResponseEntity<>(payments, HttpStatus.OK);
		} else {
			logger.info("No records found in payments collection for userId- {}", userId);
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	private Query getSearchQuery(String searchInput) {
		Query query = new Query();
		List<Criteria> criterias = new LinkedList<>();
		Criteria searchCriteria = new Criteria();
		searchCriteria.orOperator(
				Criteria.where("transactionId")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("status")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("paymentMode")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("courseId")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("userId")
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
	public ResponseEntity<?> getCreditsByUser(String userId) {
		logger.info("Querying get all credits/rewards transactions in db for userId-{}", userId);
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(userId));
		List<CreditsDetails> creditsDetails = this.mongoTemplate.find(query, CreditsDetails.class);
		if (!CollectionUtils.isEmpty(creditsDetails)) {
			return new ResponseEntity<>(creditsDetails, HttpStatus.OK);
		} else {
			logger.info("No records found in credits collection for userId- {}", userId);
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		}
	}
}
