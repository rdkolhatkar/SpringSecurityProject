package com.ratnakar.security.controller;


import com.ratnakar.security.entity.JobPostData;
import com.ratnakar.security.service.JobCodeApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 @CrossOrigin allows requests from a different origin (domain/port)
 Here, we are explicitly allowing requests coming from
 the frontend running on http://localhost:3000 (Which is our React Application)
 Without this annotation, browsers will block the request
 due to Same-Origin Policy (CORS restriction).
*/
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class JobRestController {
    @Autowired
    JobCodeApplicationService jobApplicationService;
    @GetMapping(path="jobPosts", produces = {"application/json"}) //By writing the "produces" keyword we are specifying that this @GetMapping method will only produce the json response data
    @ResponseBody
    public List<JobPostData> getAllJobs(){
        return jobApplicationService.getAllJobs();
    } // This method will only return the json response
    @GetMapping("jobPost/{postId}")
    public JobPostData getJob(@PathVariable("postId") int postId){
        return jobApplicationService.getJob(postId);
    }

    /*
    consumes defines the media types that a Spring controller method can accept in the request body.
    If the client sends a request with a Content-Type not listed in consumes, Spring returns 415 Unsupported Media Type.
    It helps Spring choose the correct HttpMessageConverter and enforces a strict API contract.
    */
    @PostMapping(path="jobPosts/add", consumes = {"application/xml","application/json"}) // In this case our @PostMapping will accept only XML and JSON request Body
    public JobPostData addJob(@RequestBody JobPostData jobPostData){
        jobApplicationService.addJob(jobPostData);
        return jobApplicationService.getJob(jobPostData.getPostId());
    }
    @PutMapping("jobPosts")
    public JobPostData updateJob(@RequestBody JobPostData jobPostData){
        jobApplicationService.updateJob(jobPostData);
        return jobApplicationService.getJob(jobPostData.getPostId());
    }
    @DeleteMapping("jobPosts/{postId}")
    public String deleteJob(@PathVariable("postId") int postId){
        jobApplicationService.deleteJob(postId);
        return "DELETE";
    }
}
