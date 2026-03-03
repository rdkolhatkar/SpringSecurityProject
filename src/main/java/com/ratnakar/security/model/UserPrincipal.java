package com.ratnakar.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/*
 ============================================================================
 WHAT IS THIS CLASS?
 ============================================================================

 UserPrincipal acts as a bridge between:

 - Your Users Entity (database model)
 - Spring Security's UserDetails

 Spring Security ONLY understands UserDetails.

 So we convert:
 Users  →  UserPrincipal  →  UserDetails
*/

public class UserPrincipal implements UserDetails {

    /*
     This is your actual database entity object.
     We wrap it inside UserPrincipal.
    */
    private Users user;

    /*
     Constructor
     -----------
     Accepts Users object from database
    */
    public UserPrincipal(Users user) {
        this.user = user;
    }

    /*
     =========================================================================
     AUTHORITIES (ROLES)
     =========================================================================

     Spring Security does NOT use roles directly.
     It uses GrantedAuthority.

     Example:
     If role in DB = "ADMIN"

     It must be converted to:
     "ROLE_ADMIN"

     Because internally Spring Security checks:
     hasRole("ADMIN") → actually checks "ROLE_ADMIN"
    */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        /*
         SimpleGrantedAuthority is an implementation of GrantedAuthority.

         We are converting DB role into Security role.
        */
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );
    }

    /*
     =========================================================================
     PASSWORD
     =========================================================================

     Spring Security will call this method
     during authentication to get encoded password from DB.
    */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /*
     =========================================================================
     USERNAME
     =========================================================================

     Spring Security will call this method
     to get username during login.
    */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /*
     =========================================================================
     ACCOUNT STATUS FLAGS
     =========================================================================

     These methods control:
     - Is account expired?
     - Is account locked?
     - Is credentials expired?
     - Is account enabled?

     Currently returning TRUE means:
     Everything is valid.

     In real projects, you may store these in DB.
    */

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}