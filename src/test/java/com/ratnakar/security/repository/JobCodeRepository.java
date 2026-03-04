package com.ratnakar.security.repository;


import com.ratnakar.security.entity.JobPostData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
public class JobCodeRepository {
    List<JobPostData> jobsList = new ArrayList<>(Arrays.asList(

            // 1 → singletonList
            new JobPostData(
                    1,
                    "Java Developer",
                    "Must have strong knowledge of Java, OOPs and SDLC",
                    3,
                    Collections.singletonList("Java")
            ),

            // 2 → asList
            new JobPostData(
                    2,
                    "Spring Boot Developer",
                    "Experience in building REST APIs and microservices",
                    4,
                    Arrays.asList("Spring Boot", "REST API", "Maven")
            ),

            // 3 → singletonList
            new JobPostData(
                    3,
                    "Python Developer",
                    "Must have hands-on experience in Python and automation scripting",
                    2,
                    Collections.singletonList("Python")
            ),

            // 4 → asList
            new JobPostData(
                    4,
                    "Frontend Developer",
                    "Good experience in building responsive UI applications",
                    3,
                    Arrays.asList("HTML", "CSS", "JavaScript")
            ),

            // 5 → singletonList
            new JobPostData(
                    5,
                    "React Developer",
                    "Must be able to create reusable UI components",
                    2,
                    Collections.singletonList("ReactJS")
            ),

            // 6 → asList
            new JobPostData(
                    6,
                    "NodeJS Developer",
                    "Hands-on experience in backend development with NodeJS",
                    3,
                    Arrays.asList("NodeJS", "ExpressJS", "MongoDB")
            ),

            // 7 → singletonList
            new JobPostData(
                    7,
                    "DevOps Engineer",
                    "Knowledge of CI/CD pipelines, Docker and deployment pipelines",
                    4,
                    Collections.singletonList("Docker")
            ),

            // 8 → asList
            new JobPostData(
                    8,
                    "Cloud Engineer",
                    "Should have experience working with cloud platforms",
                    5,
                    Arrays.asList("AWS", "S3", "EC2")
            ),

            // 9 → singletonList
            new JobPostData(
                    9,
                    "Database Administrator",
                    "Experience in database performance tuning and backup management",
                    4,
                    Collections.singletonList("MySQL")
            ),

            // 10 → asList
            new JobPostData(
                    10,
                    "QA Automation Engineer",
                    "Should have experience in Selenium automation and test cycles",
                    3,
                    Arrays.asList("Selenium", "Java", "TestNG")
            )
    ));
    public List<JobPostData> getAllJobs(){
        return jobsList;
    }

    public void addJob(JobPostData jobPostData){
        jobsList.add(jobPostData);
        System.out.println(jobsList);
    }

    public JobPostData getJob(int postId){
        for(JobPostData jobPostData : jobsList){
            if(jobPostData.getPostId() == postId){
                return jobPostData;
            }
        }
        return null;
    }

    public void updateJob(JobPostData jobPostData){
        for(JobPostData jobPostDataOne :  jobsList){
            if(jobPostDataOne.getPostId() == jobPostData.getPostId()){
                jobPostDataOne.setPostProfile(jobPostData.getPostProfile());
                jobPostDataOne.setPostDescription(jobPostData.getPostDescription());
                jobPostDataOne.setPostTechStack(jobPostData.getPostTechStack());
                jobPostDataOne.setRequiredExperience(jobPostData.getRequiredExperience());
            }
        }

    }

    public void deleteJob(int postId){
        for(JobPostData jobPostData : jobsList){
            if(jobPostData.getPostId() == postId){
                jobsList.remove(jobPostData);
            }
        }
    }

}
