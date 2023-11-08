package com.training.service;

import org.springframework.http.ResponseEntity;

import com.training.model.CreatePaymentRequest;

public interface TransactionService {

	ResponseEntity<?> createUserCourseTransaction(CreatePaymentRequest request);

	ResponseEntity<?> getTransactions(String searchInput);
	
	ResponseEntity<?> getTransactionsByUser(String userId);


}
