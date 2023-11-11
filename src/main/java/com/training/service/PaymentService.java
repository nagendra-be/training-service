package com.training.service;

import com.training.model.PaymentRequest;

public interface PaymentService {

	String initiatePayment(PaymentRequest paymentRequest);

}
