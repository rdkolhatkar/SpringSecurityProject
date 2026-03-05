package com.ratnakar.test.service;

import com.ratnakar.test.entity.UsersData;
import com.ratnakar.test.repository.AuthorizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public abstract class UserDetailsService {
    @Autowired
    AuthorizationRepository authenticationRepository;
    public UsersData saveUserIntoDB(UsersData user){
        return authenticationRepository.save(user);
    }

    /*
         =========================================================================
         CORE METHOD – loadUserByUsername
         =========================================================================

         Spring Security automatically calls this method
         when user tries to login.

         Example:
         User enters:
         Username: admin_user
         Password: admin123

         Spring calls:
         loadUserByUsername("admin_user")
        */
    public abstract UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException;
}
