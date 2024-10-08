package com.example.myprogress.app.SpringSecurity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.myprogress.app.Entites.appUser;
import com.example.myprogress.app.Repositories.AppUserRepository;

@Service
public class ValidationLogin implements UserDetailsService {

    private AppUserRepository userRepository;

    public ValidationLogin(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // In this method I validate if the user exist in the database and after I
    // return a UserDatails, this userDatails has the information of the user and
    // will be used in the successful login(validateLogin)
    @Override
    public UserDetails loadUserByUsername(String user) throws UsernameNotFoundException {
            Optional <appUser> userOptional = userRepository.findByIdUser(user);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException(user);
        }
        appUser userGot = userOptional.get();
        /*
         * List<SimpleGrantedAuthority> authorities = userGot.getRoles().stream()
         * .map(role -> new
         * SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
         */

        // INformation about this mean of each value,true,true,true,true
        return new org.springframework.security.core.userdetails.User(userGot.getUser(), userGot.getPassWord(),
                true, true, true, true, Collections.emptyList() );
    }

}
