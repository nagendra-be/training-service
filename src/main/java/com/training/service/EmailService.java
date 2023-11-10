package com.training.service;

public interface EmailService {

	void send(String from, String to, String subject, String text, String cc, String bcc);

}
