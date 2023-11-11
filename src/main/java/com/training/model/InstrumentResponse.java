package com.training.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentResponse {

	private String type;
	private RedirectInfo redirectInfo;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RedirectInfo getRedirectInfo() {
		return redirectInfo;
	}

	public void setRedirectInfo(RedirectInfo redirectInfo) {
		this.redirectInfo = redirectInfo;
	}
}
