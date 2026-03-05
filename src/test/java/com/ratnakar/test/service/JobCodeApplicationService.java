package com.ratnakar.test.service;

import com.ratnakar.test.repository.JobCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ratnakar.test.entity.JobPostData;

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
