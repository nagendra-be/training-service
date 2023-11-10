package com.training.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.training.constants.CollectionConstants;

@Document(collection = CollectionConstants.USERS)
public class User {
	@Id
	private String id;
	private String firstName;
	private String lastName;
	@Indexed(unique = true)
	@NotNull
	private String email;
	private String phone;
	private String address;
	private String city;
	private String state;
	private String zipCode;
	private String referralId;
	private String country;
	@CreatedDate
	private Date createdAt;
	@LastModifiedDate
	private Date lastModifiedDate;
	@JsonIgnore
	private String password;
	private String role;
	private String referredBy;
	private String status;
	private String token;
	@JsonIgnore
	private Date tokenExpiry;
	private String uniqueId;
	private boolean member;
	private boolean emailVerified;
	private boolean phoneVerified;
	@JsonIgnore
	private Date emailExpiry;
	private int credits;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return this.zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getReferralId() {
		return this.referralId;
	}

	public void setReferralId(String referralId) {
		this.referralId = referralId;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getLastModifiedDate() {
		return this.lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getReferredBy() {
		return this.referredBy;
	}

	public void setReferredBy(String referredBy) {
		this.referredBy = referredBy;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getTokenExpiry() {
		return this.tokenExpiry;
	}

	public void setTokenExpiry(Date tokenExpiry) {
		this.tokenExpiry = tokenExpiry;
	}

	public String getUniqueId() {
		return this.uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public boolean isMember() {
		return this.member;
	}

	public void setMember(boolean member) {
		this.member = member;
	}

	public boolean isEmailVerified() {
		return this.emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public boolean isPhoneVerified() {
		return this.phoneVerified;
	}

	public void setPhoneVerified(boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	public Date getEmailExpiry() {
		return this.emailExpiry;
	}

	public void setEmailExpiry(Date emailExpiry) {
		this.emailExpiry = emailExpiry;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}

}
