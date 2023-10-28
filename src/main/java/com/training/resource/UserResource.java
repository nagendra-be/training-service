package com.training.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
	
	@PostMapping("/createuser")
	public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
		System.out.println("Inside create user......");
		return this.userService.createUser(request);
	}


	@GetMapping("/getusers")
	public ResponseEntity<?> getUsers(@RequestParam(required = false) String searchInput) {
		return this.userService.getUsers(searchInput);
	}

	@CrossOrigin(value = "http://localhost:3000")
	@GetMapping("/getuser")
	public ResponseEntity<?> getUser(@RequestParam String customerId) {
		return this.userService.getUser(customerId);
	}

	@CrossOrigin(value = "http://localhost:3000")
	@PutMapping("/updateuser")
	public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
		return this.userService.updateUser(request);
	}

	@CrossOrigin(value = "http://localhost:3000")
	@DeleteMapping("/deleteuser")
	public ResponseEntity<?> deleteUser(@RequestParam String customerId) {
		return this.userService.deleteUser(customerId);
	}
}
