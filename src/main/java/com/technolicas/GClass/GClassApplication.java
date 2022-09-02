package com.technolicas.GClass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.ListCoursesResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
@RestController
public class GClassApplication {
	@GetMapping
	public String welcome() {
		return "Welcome to demo App";
	}
	@GetMapping("/user")
	public Principal user(Principal principal) {
		System.out.println("username:/t"+principal.getName());
		return principal;
	}
	@GetMapping("/createCourse")
	public static Course createCourse() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
		        .createScoped(Collections.singleton(ClassroomScopes.CLASSROOM_COURSES));
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
		        credentials);
		Classroom service = new Classroom.Builder(new NetHttpTransport(),
		        GsonFactory.getDefaultInstance(),
		        requestInitializer)
		        .setApplicationName("classroomspringbootproject")
		        .build();

		    Course course = null;
		    try {
		      course = new Course()
		          .setName("10th Grade Biology")
		          .setSection("Period 2")
		          .setDescriptionHeading("Welcome to 10th Grade Biology")
		          .setDescription("We'll be learning about about the structure of living creatures "
		              + "from a combination of textbooks, guest lectures, and lab work. Expect "
		              + "to be excited!")
		          .setRoom("301")
		          .setOwnerId("me")
		          .setCourseState("PROVISIONED");
		      course = service.courses().create(course).execute();
		      System.out.printf("Course created: %s (%s)\n", course.getName(), course.getId());
		    } catch (GoogleJsonResponseException e) {
		      GoogleJsonError error = e.getDetails();
		      if (error.getCode() == 400) {
		        System.err.println("Unable to create course, ownerId not specified.\n");
		      } else {
		        throw e;
		      }
		    }
		    return course;		
	}
	@GetMapping("/listCourse")
	public static List<Course> listCourse() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
		        .createScoped(Collections.singleton(ClassroomScopes.CLASSROOM_COURSES));
	    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
	        credentials);
	    Classroom service = new Classroom.Builder(new NetHttpTransport(),
	        GsonFactory.getDefaultInstance(),
	        requestInitializer)
	        .setApplicationName("classroomspringbootproject")
	        .build();

	    String pageToken = null;
	    List<Course> courses = new ArrayList<>();

	    try {
	      do {
	        ListCoursesResponse response = service.courses().list()
	            .setPageSize(100)
	            .setPageToken(pageToken)
	            .execute();
	        courses.addAll(response.getCourses());
	        pageToken = response.getNextPageToken();
	      } while (pageToken != null);

	      if (courses.isEmpty()) {
	        System.out.println("No courses found.");
	      } else {
	        System.out.println("Courses:");
	        for (Course course : courses) {
	          System.out.printf("%s (%s)\n", course.getName(), course.getId());
	        }
	      }
	    } catch (NullPointerException ne) {
	      System.err.println("No courses found.\n");
	    }
	    return courses;
		
	}
	public static void main(String[] args) {
		SpringApplication.run(GClassApplication.class, args);
	}

}
