package com.training.service;

import org.springframework.http.ResponseEntity;

import com.training.model.CreateUserRequest;
import com.training.model.UpdateUserRequest;

public interface UserService {

	ResponseEntity<?> createUser(CreateUserRequest request);

	ResponseEntity<?> getUsers(String searchInput);

	ResponseEntity<?> getUser(String customerId);

	ResponseEntity<?> updateUser(UpdateUserRequest request);

	ResponseEntity<?> deleteUser(String customerId);

	ResponseEntity<?> loginAuthentication(String username, String password);

	ResponseEntity<?> verifyEmail(String uniqueId);

	ResponseEntity<?> sendEmail(String userId, String operation);

	void calculateRemainingCredits(String userId);
}
