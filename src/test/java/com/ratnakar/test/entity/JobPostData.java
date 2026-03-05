package com.ratnakar.test.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Data is a Lombok annotation that automatically generates:
 *  - Getters for all fields
 *  - Setters for all fields
 *  - toString() method
 *  - equals() and hashCode() methods
 *
 * It reduces boilerplate code and keeps the class clean.
 */
@Data

/**
 * @NoArgsConstructor generates a default no-argument constructor.
 * This is required by frameworks like Spring and Hibernate,
 * which often need to create bean instances using reflection.
 */
@NoArgsConstructor

/**
 * @AllArgsConstructor generates a constructor containing ALL fields.
 * This allows creation of objects like:
 * new JobePostData(1, "Java Developer", "Backend role", 3, List.of("Java","Spring"));
 */
@AllArgsConstructor

/**
 * @Component marks this class as a Spring Bean.
 * This allows Spring to detect and create an instance of this class
 * automatically during component scanning.
 */
@Component
public class JobPostData {

    // Unique identifier for the job post
    private int postId;

    // Job role name or profile (e.g., "Java Developer", "QA Engineer")
    private String postProfile;

    // Short job description
    private String postDescription;

    // Required experience for the job position
    private int requiredExperience;

    // List of technologies required for the job (e.g., ["Java", "Spring Boot"])
    private List<String> postTechStack;
}
