package com.ratnakar.test.service;


import com.ratnakar.test.entity.UserServiceImpl;
import com.ratnakar.test.entity.UsersData;
import com.ratnakar.test.repository.AuthorizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService implements UserDetailsService {

    @Autowired
    private AuthorizationRepository authenticationRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UsersData user = authenticationRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found with username: " + username
            );
        }

        return new UserServiceImpl(user);
    }
}