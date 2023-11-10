package com.training.model;

import java.util.Date;

public class CreatePaymentRequest {
	private String userId;
	private String courseId;
	private String transactionId;
	private Date transactionDate;
	private int amount;
	private int creditsUsed;
	private String paymentMode;
	private boolean membershipTransaction;

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCourseId() {
		return this.courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Date getTransactionDate() {
		return this.transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getCreditsUsed() {
		return this.creditsUsed;
	}

	public void setCreditsUsed(int creditsUsed) {
		this.creditsUsed = creditsUsed;
	}

	public String getPaymentMode() {
		return this.paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public boolean isMembershipTransaction() {
		return this.membershipTransaction;
	}

	public void setMembershipTransaction(boolean membershipTransaction) {
		this.membershipTransaction = membershipTransaction;
	}

}
