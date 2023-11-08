package com.training.resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.training.model.CreateUserRequest;
import com.training.model.UpdateUserRequest;
import com.training.service.UserService;

@RestController
@RequestMapping("/api/v1/training/users")
public class UserResource {

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

	@PostMapping("/createuser")
	public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
		logger.info("Creating new user......");
		return this.userService.createUser(request);
	}

	@GetMapping("/getusers")
	public ResponseEntity<?> getUsers(@RequestParam(required = false) String searchInput) {
		return this.userService.getUsers(searchInput);
	}

	@GetMapping("/getuser")
	public ResponseEntity<?> getUser(@RequestParam String userId) {
		return this.userService.getUser(userId);
	}

	@PutMapping("/updateuser")
	public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
		logger.info("updating account details for user- {}", request.getEmail());
		String email = request.getEmail();
		if (StringUtils.isEmpty(email)) {
			return new ResponseEntity<>("Email Id cannot be empty", HttpStatus.BAD_REQUEST);
		}
		return this.userService.updateUser(request);
	}

	@DeleteMapping("/deleteuser")
	public ResponseEntity<?> deleteUser(@RequestParam String userId) {
		logger.info("Deleting account for user- {}", userId);
		return this.userService.deleteUser(userId);
	}
}
