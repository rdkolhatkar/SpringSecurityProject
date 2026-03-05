package com.ratnakar.test;


import com.ratnakar.test.config.SecurityConfiguration;
import com.ratnakar.test.controller.JobRestController;
import com.ratnakar.test.entity.JobPostData;
import com.ratnakar.test.entity.UserServiceImpl;
import com.ratnakar.test.entity.UsersData;
import com.ratnakar.test.repository.AuthorizationRepository;
import com.ratnakar.test.repository.JobCodeRepository;
import com.ratnakar.test.service.JobCodeApplicationService;
import com.ratnakar.test.service.UserAuthService;
import com.ratnakar.test.service.UserDetailsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackageClasses = {
		SecurityConfiguration.class,
		JobRestController.class,
		JobPostData.class,
		UsersData.class,
		UserServiceImpl.class,
		AuthorizationRepository.class,
		JobCodeRepository.class,
		JobCodeApplicationService.class,
		UserAuthService.class,
		UserDetailsService.class
})
class TestSpringSecurityProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(TestSpringSecurityProjectApplication.class, args);
	}

}
