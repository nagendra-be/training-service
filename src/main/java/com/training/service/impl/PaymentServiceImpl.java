package com.training.service.impl;

import com.training.model.ApiResponse;
import com.training.model.PaymentRequest;
import com.training.service.PaymentService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import okhttp3.*;

@Service
public class PaymentServiceImpl implements PaymentService {

	private static final String PHONEPE_API_URL = "https://api.phonepe.com/apis/hermes/pg/v1/pay";
	private static final String SALT_KEY = "04264086-93dd-41b6-85b6-2d7e8deae0b4";
	private static final String MERCHANT_ID = "M1VHM1S7GVTQ";
	private static final int SALT_INDEX = 1;

	private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

	private static final OkHttpClient httpClient = new OkHttpClient();

	@Override
	public String initiatePayment(PaymentRequest paymentRequest) {
		try {
			String payload = createBase64EncodedPayload(paymentRequest);
			String checksum = calculateChecksum(payload);
			ApiResponse response = makePaymentApiRequest(payload, checksum);
			return response.getData().getInstrumentResponse().getRedirectInfo().getUrl();
		} catch (Exception e) {
			// Handle exception appropriately
			e.printStackTrace();
			return null;
		}
	}

	private String createBase64EncodedPayload(PaymentRequest paymentRequest) throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("merchantId", MERCHANT_ID);
		data.put("merchantTransactionId", paymentRequest.getMerchantTransactionId());
		data.put("merchantUserId", paymentRequest.getMerchantUserId());
		data.put("name", paymentRequest.getName());
		data.put("amount", paymentRequest.getAmount() * 100);
		data.put("redirectMode", "POST");
		data.put("mobileNumber", paymentRequest.getMobileNumber());

		Map<String, Object> paymentInstrument = new HashMap<>();
		paymentInstrument.put("type", "PAY_PAGE");

		data.put("paymentInstrument", paymentInstrument);

		String payload = new ObjectMapper().writeValueAsString(data);
		return Base64.getEncoder().encodeToString(payload.getBytes());
	}

	private String calculateChecksum(String payload) throws Exception {
		String dataToHash = payload + "/pg/v1/pay" + SALT_KEY + "###" + SALT_INDEX;
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(hash).toLowerCase() + "###" + SALT_INDEX;
	}

	private ApiResponse makePaymentApiRequest(String payload, String checksum) throws Exception {
		RequestBody requestBody = RequestBody.create(MediaType.get("application/json"),
				"{\"request\":\"" + payload + "\"}");

		Request request = new Request.Builder().url(PHONEPE_API_URL).post(requestBody)
				.addHeader("accept", "application/json").addHeader("Content-Type", "application/json")
				.addHeader("X-VERIFY", checksum).build();

		Response response = httpClient.newCall(request).execute();

		if (response.isSuccessful()) {
			logger.info("API Response: {}", response);
			String responseBody = response.body().string();
			return new ObjectMapper().readValue(responseBody, ApiResponse.class);
		} else {
			logger.error("Failed to make a new payment. HTTP Status: {}, Response Body: {}", response.code(),
					response.body().string());
			throw new RuntimeException("Failed to make a new payment");
		}
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
