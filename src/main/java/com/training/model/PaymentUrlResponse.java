package com.training.model;

public class PaymentUrlResponse {
	private String url;
	private String message;

	public PaymentUrlResponse(String url, String message) {
		this.url = url;
		this.message = message;
	}

	public String getUrl() {
		return this.url;
	}

	public String getMessage() {
		return this.message;
	}
}
