package com.ratnakar.security.service;

import com.ratnakar.security.model.UserPrincipal;
import com.ratnakar.security.model.Users;
import com.ratnakar.security.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 ============================================================================
 WHAT IS THIS CLASS?
 ============================================================================

 This class connects:

 Spring Security  ↔  Database

 It is responsible for:
 - Fetching user from DB
 - Converting Users → UserPrincipal
 - Returning UserDetails object

 Without this class:
 Database authentication will NOT work.
*/

@Service
public class UserAuthenticationService implements UserDetailsService {

    /*
     Inject repository to fetch user from DB.
    */
    @Autowired
    private AuthenticationRepository authenticationRepository;

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
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        /*
         Step 1:
         Fetch user from database.

         You must have a method like:
         findByUsername(String username)
        */
        Users user = authenticationRepository.findByUsername(username);

        /*
         Step 2:
         If user not found → throw exception

         This exception is mandatory.
         If not thrown → Spring thinks authentication succeeded.
        */
        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found with username: " + username
            );
        }

        /*
         Step 3:
         Convert Users entity into UserPrincipal

         Because Spring Security understands only UserDetails.
        */
        return new UserPrincipal(user);
    }
}