package com.training.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.model.PaymentRequest;
import com.training.service.PaymentService;

@RestController
@RequestMapping("/api/v1/training/payments")
public class PaymentResource {

	@Autowired
	private PaymentService paymentService;

	private static final Logger logger = LoggerFactory.getLogger(PaymentResource.class);

	@PostMapping("/initiatepayment")
	public String createPayment(@RequestBody PaymentRequest request) {
		logger.info("Creating new payment......");
		return this.paymentService.initiatePayment(request);
	}
}
