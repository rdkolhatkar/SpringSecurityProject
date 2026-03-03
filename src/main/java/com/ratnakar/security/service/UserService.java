package com.ratnakar.security.service;

import com.ratnakar.security.model.Users;
import com.ratnakar.security.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    AuthenticationRepository authenticationRepository;
    public Users saveUserIntoDB(Users user){
        return authenticationRepository.save(user);
    }
}
