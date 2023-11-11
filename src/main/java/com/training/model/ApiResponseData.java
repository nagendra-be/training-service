package com.training.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseData {

	private String merchantId;
	private String merchantTransactionId;
	private InstrumentResponse instrumentResponse;

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantTransactionId() {
		return merchantTransactionId;
	}

	public void setMerchantTransactionId(String merchantTransactionId) {
		this.merchantTransactionId = merchantTransactionId;
	}

	public InstrumentResponse getInstrumentResponse() {
		return instrumentResponse;
	}

	public void setInstrumentResponse(InstrumentResponse instrumentResponse) {
		this.instrumentResponse = instrumentResponse;
	}
}
