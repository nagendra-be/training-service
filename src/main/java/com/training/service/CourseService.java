package com.training.service;

import org.springframework.http.ResponseEntity;

import com.training.model.CreateCourseRequest;
import com.training.model.UpdateCourseRequest;

public interface CourseService {

	ResponseEntity<?> createCourse(CreateCourseRequest request);

	ResponseEntity<?> getCourses(String searchInput);
	
	ResponseEntity<?> getCourse(String courseId);
	
	ResponseEntity<?> getMyCourses(String userId);
	
	ResponseEntity<?> getRemainingCourses(String userId);

	ResponseEntity<?> updateCourse(UpdateCourseRequest request);

	ResponseEntity<?> deleteCourse(String courseId);

}
