package com.training.service.impl;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.training.model.User;
import com.training.service.ReferralService;

@Service
public class ReferralServiceImpl implements ReferralService {

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int ID_LENGTH = 7;
	private static SecureRandom random = new SecureRandom();

	@Autowired
	private MongoTemplate mongoTemplate;

	// Generate a unique referral ID
	public String generateUniqueReferralId() {
		String referralId;
		do {
			referralId = generateRandomId();
		} while (!isReferralIdUnique(referralId));
		return referralId;
	}

	// Generate a random referral ID
	private String generateRandomId() {
		StringBuilder sb = new StringBuilder(ID_LENGTH);
		for (int i = 0; i < ID_LENGTH; i++) {
			int randomIndex = random.nextInt(CHARACTERS.length());
			sb.append(CHARACTERS.charAt(randomIndex));
		}
		return sb.toString();
	}

	// Check if a referral ID is unique
	private boolean isReferralIdUnique(String referralId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("referralId").is(referralId));
		long count = this.mongoTemplate.count(query, User.class);
		if (count > 0) {
			return false;
		}
		return true;
	}
}
