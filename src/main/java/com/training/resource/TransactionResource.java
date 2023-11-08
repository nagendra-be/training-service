package com.training.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.training.model.CreatePaymentRequest;
import com.training.service.TransactionService;

@RestController
@RequestMapping("/api/v1/training/payments")
public class TransactionResource {

	@Autowired
	private TransactionService transactionService;

	private static final Logger logger = LoggerFactory.getLogger(TransactionResource.class);

	@PostMapping("/createpayment")
	public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request) {
		logger.info("Creating new payment......");
		return this.transactionService.createUserCourseTransaction(request);
	}

	@GetMapping("/getpayments")
	public ResponseEntity<?> getPayments(@RequestParam(required = false) String searchInput) {
		return this.transactionService.getTransactions(searchInput);
	}

	@GetMapping("/byuser")
	public ResponseEntity<?> getCourse(@RequestParam String userId) {
		return this.transactionService.getTransactionsByUser(userId);
	}
}