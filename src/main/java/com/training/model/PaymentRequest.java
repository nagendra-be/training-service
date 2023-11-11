package com.training.model;

public class PaymentRequest {

	private String merchantTransactionId;
	private String merchantUserId;
	private String name;
	private double amount;
	private String mobileNumber;

	// Add getters and setters

	public PaymentRequest(String transactionId, String MUID, String name, double amount, String number) {
		this.merchantTransactionId = transactionId;
		this.merchantUserId = MUID;
		this.name = name;
		this.amount = amount;
		this.mobileNumber = number;
	}

	public String getMerchantTransactionId() {
		return merchantTransactionId;
	}

	public void setMerchantTransactionId(String merchantTransactionId) {
		this.merchantTransactionId = merchantTransactionId;
	}

	public String getMerchantUserId() {
		return merchantUserId;
	}

	public void setMerchantUserId(String merchantUserId) {
		this.merchantUserId = merchantUserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

}
