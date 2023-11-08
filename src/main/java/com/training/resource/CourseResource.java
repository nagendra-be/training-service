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

import com.training.model.CreateCourseRequest;
import com.training.model.UpdateCourseRequest;
import com.training.service.CourseService;

@RestController
@RequestMapping("/api/v1/training/courses")
public class CourseResource {

	@Autowired
	private CourseService courseService;

	private static final Logger logger = LoggerFactory.getLogger(CourseResource.class);

	@PostMapping("/createcourse")
	public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request) {
		logger.info("Creating new course......");
		return this.courseService.createCourse(request);
	}

	@GetMapping("/getcourses")
	public ResponseEntity<?> getUsers(@RequestParam(required = false) String searchInput) {
		return this.courseService.getCourses(searchInput);
	}

	@GetMapping("/getcourse")
	public ResponseEntity<?> getCourse(@RequestParam String courseId) {
		return this.courseService.getCourse(courseId);
	}

	@GetMapping("/mycourses")
	public ResponseEntity<?> getMyCourses(@RequestParam String userId) {
		return this.courseService.getMyCourses(userId);
	}

	@GetMapping("/recommendedcourses")
	public ResponseEntity<?> getOtherCourses(@RequestParam String userId) {
		return this.courseService.getRemainingCourses(userId);
	}

	@PutMapping("/updatecourse")
	public ResponseEntity<?> updateCourse(@RequestBody UpdateCourseRequest request) {
		logger.info("updating course details for id- {}", request.getCourseId());
		String courseId = request.getCourseId();
		if (StringUtils.isEmpty(courseId)) {
			return new ResponseEntity<>("Course Id cannot be empty", HttpStatus.BAD_REQUEST);
		}
		return this.courseService.updateCourse(request);
	}

	@DeleteMapping("/deletecourse")
	public ResponseEntity<?> deleteCourse(@RequestParam String courseId) {
		logger.info("Deleting course - {}", courseId);
		return this.courseService.deleteCourse(courseId);
	}
}