package com.training.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.model.LoginRequest;
import com.training.service.UserService;

@RestController
@RequestMapping("/api/v1/training")
public class TrainingResource {

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(TrainingResource.class);

	@PostMapping("/signin")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		logger.info("Trying to login method...");
		// byte[] decodedBytes = Base64Utils.decodeFromString(request.getPassword());
		// String decodedPassword = new String(decodedBytes);
		return this.userService.loginAuthentication(request.getUsername(), request.getPassword());
	}

}
