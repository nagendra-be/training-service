package com.training.model;

public class CreateCourseRequest {
	private String name;
	private String courseId;
	private String description;
	private String status;
	private int price;
	private int memberPrice;
	private int nonMemberPrice;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCourseId() {
		return this.courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getPrice() {
		return this.price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getMemberPrice() {
		return this.memberPrice;
	}

	public void setMemberPrice(int memberPrice) {
		this.memberPrice = memberPrice;
	}

	public int getNonMemberPrice() {
		return this.nonMemberPrice;
	}

	public void setNonMemberPrice(int nonMemberPrice) {
		this.nonMemberPrice = nonMemberPrice;
	}

}
