package com.ratnakar.security.service;

import com.ratnakar.security.repository.JobCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ratnakar.security.entity.JobPostData;

import java.util.List;

@Service
public class JobCodeApplicationService {
    @Autowired
    JobCodeRepository jobAppRepository;
    public void addJob(JobPostData jobPostData){
        jobAppRepository.addJob(jobPostData);
    }
    public List<JobPostData> getAllJobs(){
        return jobAppRepository.getAllJobs();
    }
    public JobPostData getJob(int postId){
        return jobAppRepository.getJob(postId);
    }
    public void updateJob(JobPostData jobPostData){
        jobAppRepository.updateJob(jobPostData);
    }
    public void deleteJob(int postId){
        jobAppRepository.deleteJob(postId);
    }
}
