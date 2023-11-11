package com.training.service;

import org.springframework.http.ResponseEntity;

import com.training.model.PaymentRequest;

public interface PaymentService {

	ResponseEntity<?> initiatePayment(PaymentRequest paymentRequest);

}
