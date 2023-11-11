package com.training.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.training.constants.TrainingConstants;
import com.training.model.Course;
import com.training.model.CreateCourseRequest;
import com.training.model.UpdateCourseRequest;
import com.training.model.UserCourseMapping;
import com.training.service.CourseService;

@Service
public class CourseServiceImpl implements CourseService {

	@Autowired
	private MongoTemplate mongoTemplate;

	private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

	@Override
	public ResponseEntity<?> createCourse(CreateCourseRequest request) {
		logger.info("Inside create course...");

		Query query = new Query();
		query.addCriteria(Criteria.where("courseId").is(request.getCourseId()));
		if (this.mongoTemplate.count(query, Course.class) > 0) {
			return new ResponseEntity<>("Courses already exists", HttpStatus.BAD_REQUEST);
		}

		Course course = new Course();
		BeanUtils.copyProperties(request, course);
		course.setStatus(TrainingConstants.ACTIVE);
		logger.info("Saving course -{} into db", course.getCourseId());
		this.mongoTemplate.save(course);

		return new ResponseEntity<>("Course successfully created", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getCourses(String searchInput) {
		logger.info("Querying get all active course in db....");
		Query query = new Query();
		if (StringUtils.isNotEmpty(searchInput)) {
			query = this.getSearchQuery(searchInput);
		}
		query.addCriteria(Criteria.where("status").is(TrainingConstants.ACTIVE));
		List<Course> courses = this.mongoTemplate.find(query, Course.class);
		if (!CollectionUtils.isEmpty(courses)) {
			return new ResponseEntity<>(courses, HttpStatus.OK);
		} else {
			logger.info("No active courses found in courses collection...");
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getCourse(String courseId) {
		logger.info("Querying info for courseId- {}", courseId);
		Query query = new Query();
		query.addCriteria(Criteria.where("courseId").is(courseId));
		Course course = this.mongoTemplate.findOne(query, Course.class);
		if (course != null) {
			return new ResponseEntity<>(course, HttpStatus.OK);
		} else {
			logger.info("No course found - {}", courseId);
			return new ResponseEntity<>(new Course(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> getMyCourses(String userId) {
		logger.info("Getting all registered courses for user - {}", userId);
		Query query = new Query();
		query.addCriteria(Criteria.where(userId).is(userId));

		List<UserCourseMapping> coursesList = this.mongoTemplate.find(query, UserCourseMapping.class);
		if (CollectionUtils.isEmpty(coursesList)) {
			logger.info("User- {} is yet to register for a course", userId);
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		} else {
			Set<String> courses = coursesList.stream().map(UserCourseMapping::getCourseId).collect(Collectors.toSet());
			Query courseQuery = new Query();
			courseQuery.addCriteria(Criteria.where("courseId").in(courses));

			List<Course> myCourses = this.mongoTemplate.find(courseQuery, Course.class);
			if (CollectionUtils.isEmpty(myCourses)) {
				logger.info("Something might be breaking in code.");
				return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(myCourses, HttpStatus.OK);
			}
		}
	}

	@Override
	public ResponseEntity<?> getRemainingCourses(String userId) {
		logger.info("Getting all non registered courses for user - {}", userId);
		Query query = new Query();
		query.addCriteria(Criteria.where(userId).is(userId));

		List<UserCourseMapping> coursesList = this.mongoTemplate.find(query, UserCourseMapping.class);
		Set<String> myCourses = new LinkedHashSet<>();
		if (CollectionUtils.isEmpty(coursesList)) {
			logger.info("User- {} is yet to register for a course", userId);
		} else {
			myCourses = coursesList.stream().map(UserCourseMapping::getCourseId).collect(Collectors.toSet());
		}
		Query courseQuery = new Query();
		if (!CollectionUtils.isEmpty(myCourses)) {
			courseQuery.addCriteria(Criteria.where("courseId").nin(myCourses));
		}
		List<Course> recommendedCourses = this.mongoTemplate.find(courseQuery, Course.class);
		if (CollectionUtils.isEmpty(recommendedCourses)) {
			logger.info("User registered to all available courses so far");
			return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(recommendedCourses, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<?> updateCourse(UpdateCourseRequest request) {
		logger.info("Updating course info for course - {}", request.getCourseId());
		Query query = new Query();
		query.addCriteria(Criteria.where("courseId").is(request.getCourseId()));
		Course course = this.mongoTemplate.findOne(query, Course.class);
		if (course != null) {
			if (request.getName() != null) {
				course.setName(request.getName());
			}
			if (request.getDescription() != null) {
				course.setDescription(request.getDescription());
			}
			if (request.getPrice() != 0) {
				course.setPrice(request.getPrice());
			}
			if (request.getMemberPrice() != 0) {
				course.setMemberPrice(request.getMemberPrice());
			}
			if (request.getNonMemberPrice() != 0) {
				course.setNonMemberPrice(request.getNonMemberPrice());
			}

			this.mongoTemplate.save(course);
			return new ResponseEntity<>("Course is successfully updated", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No Course found with Id- " + request.getCourseId(), HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<?> deleteCourse(String courseId) {
		logger.info("Deleting course with id -{}", courseId);
		Query query = new Query();
		query.addCriteria(Criteria.where("courseId").is(courseId));
		Course course = this.mongoTemplate.findOne(query, Course.class);
		if (course != null) {
			course.setStatus(TrainingConstants.DELETED);
			this.mongoTemplate.save(course);
			return new ResponseEntity<>("Course-  " + courseId + " is successfully deleted", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No Course found with Id-" + courseId, HttpStatus.NOT_FOUND);
		}
	}

	private Query getSearchQuery(String searchInput) {
		Query query = new Query();
		List<Criteria> criterias = new LinkedList<>();
		Criteria searchCriteria = new Criteria();
		searchCriteria.orOperator(
				Criteria.where("courseId")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("name")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("description")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)),
				Criteria.where("status")
						.regex(Pattern.compile(searchInput, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));
		criterias.add(searchCriteria);
		if (!CollectionUtils.isEmpty(criterias)) {
			Criteria criteria = new Criteria();
			criteria.andOperator(criterias.stream().toArray(Criteria[]::new));
			query.addCriteria(criteria);
		}
		return query;
	}

}
