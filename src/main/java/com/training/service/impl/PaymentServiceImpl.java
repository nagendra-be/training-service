package com.training.service.impl;

import com.training.model.ApiResponse;
import com.training.model.PaymentRequest;
import com.training.model.PaymentUrlResponse;
import com.training.service.PaymentService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import okhttp3.*;

@Service
public class PaymentServiceImpl implements PaymentService {

	@Value("${phonepe.api.url}")
	private String phonePeApiUrl;

	@Value("${salt.key}")
	private String saltKey;

	@Value("${merchant.id}")
	private String merchantId;

	@Value("${salt.index}")
	private int saltIndex;

	@Value("${redirect.url}")
	private String redirectUrl;

	@Value("${callback.url}")
	private String callbackUrl;

	private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

	private static final OkHttpClient httpClient = new OkHttpClient();

	@Override
	public ResponseEntity<?> initiatePayment(PaymentRequest paymentRequest) {
		try {
			String payload = createBase64EncodedPayload(paymentRequest);
			logger.info("Base64 payload- {}", payload);
			String checksum = calculateChecksum(payload);
			ApiResponse response = makePaymentApiRequest(payload, checksum);
			String url = response.getData().getInstrumentResponse().getRedirectInfo().getUrl();
			return ResponseEntity.ok(new PaymentUrlResponse(url, "Payment initiated successfully"));
		} catch (Exception e) {
			logger.error("Error occurred while initiating payment.", e);
			String errorMessage = "Failed to initiate payment.";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new PaymentUrlResponse("", errorMessage));
		}
	}

	private String createBase64EncodedPayload(PaymentRequest paymentRequest) throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("merchantId", this.merchantId);
		data.put("merchantTransactionId", paymentRequest.getMerchantTransactionId());
		data.put("merchantUserId", paymentRequest.getMerchantUserId());
		data.put("redirectUrl", this.redirectUrl);
		data.put("callbackUrl", this.callbackUrl);
		data.put("amount", paymentRequest.getAmount() * 100);
		data.put("redirectMode", "POST");
		data.put("mobileNumber", paymentRequest.getMobileNumber());

		Map<String, Object> paymentInstrument = new HashMap<>();
		paymentInstrument.put("type", "PAY_PAGE");

		data.put("paymentInstrument", paymentInstrument);

		String payload = new ObjectMapper().writeValueAsString(data);
		logger.info("payload- {}", payload);
		return Base64.getEncoder().encodeToString(payload.getBytes());
	}

	private String calculateChecksum(String payload) throws Exception {
		String dataToHash = payload + "/pg/v1/pay" + this.saltKey;
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(hash).toLowerCase() + "###" + this.saltIndex;
	}

	private ApiResponse makePaymentApiRequest(String payload, String checksum) throws Exception {
		RequestBody requestBody = RequestBody.create(MediaType.get("application/json"),
				"{\"request\":\"" + payload + "\"}");

		Request request = new Request.Builder().url(this.phonePeApiUrl).post(requestBody)
				.addHeader("accept", "application/json").addHeader("Content-Type", "application/json")
				.addHeader("X-VERIFY", checksum).build();

		logger.info("Request body :{}", request.toString());

		Response response = httpClient.newCall(request).execute();

		if (response.isSuccessful()) {
			logger.info("API Response: {}", response);
			String responseBody = response.body().string();
			return new ObjectMapper().readValue(responseBody, ApiResponse.class);
		} else {
			logger.error("Failed to make a new payment. HTTP Status: {}, Response Body: {}", response.code(),
					response.body().string());
			// throw new RuntimeException("Failed to make a new payment");
			return new ApiResponse();
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
