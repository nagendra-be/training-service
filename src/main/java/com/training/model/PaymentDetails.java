package com.training.model;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.training.constants.CollectionConstants;

@Document(collection = CollectionConstants.PAYMENTS)
public class PaymentDetails {
	@Id
	private String id;
	private String userId;
	private int amount;
	private int creditsUsed;
	private Date paymentDate;
	private String courseId;
	private boolean membershipTransaction;
	@CreatedDate
	private Date createdAt;
	private String transactionId;
	private String paymentMode;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public Date getPaymentDate() {
		return this.paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getCourseId() {
		return this.courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
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
