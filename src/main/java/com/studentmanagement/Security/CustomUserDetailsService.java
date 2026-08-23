package com.studentmanagement.Security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.studentmanagement.entity.User;
import com.studentmanagement.repository.UserRepository;
@Component
public class CustomUserDetailsService implements UserDetailsService{
    private UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                    .orElseThrow(()->
                        new UsernameNotFoundException("username not found "+ username)
                    );
        return new CustomUserDetails(user);
    }
    
}
