package com.studentmanagement.service;


import java.util.Collection;
import java.util.HashSet;
 
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.studentmanagement.dto.response.ProfileResponse;
import com.studentmanagement.dto.response.UserMeResponse;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserMeResponse me(Authentication authentication) {
        if (!authentication.isAuthenticated()) {
            throw new IllegalArgumentException("user not authenticated");
        }
        UserMeResponse userMeResponse = new UserMeResponse();
        String username = authentication.getName();
        User user = userRepository.findByUserName(username).orElseThrow(
                () -> new ResourceNotFoundException("user not found:" + username));
        Collection<? extends GrantedAuthority> roles_permissions = authentication.getAuthorities();
        Set<String> roles = new HashSet<>();
        Set<String> KNOWN_ROLES = Set.of("STUDENT", "TEACHER", "ADMIN");

        Set<String> permissions = new HashSet<>();
        for (GrantedAuthority ga : roles_permissions) {
            String authority = ga.getAuthority();
            if (KNOWN_ROLES.contains(authority)) {
                roles.add(authority);
            } else {
                permissions.add(authority);
            }
        }
        ProfileResponse profileResponse = new ProfileResponse();
        if (user.getStudent() != null) {
            profileResponse.setType("STUDENT");
            profileResponse.setProfileId(user.getStudent().getId());
            profileResponse.setName(user.getStudent().getName());

        } else if (user.getTeacher() != null) {
            profileResponse.setType("TEACHER");
            profileResponse.setProfileId(user.getTeacher().getId());
            if (user.getTeacher().getDepartment() != null)
                profileResponse.setDepartment(user.getTeacher().getDepartment().getName());
            profileResponse.setName(user.getTeacher().getName());
        } else if (roles.contains("ADMIN")) {
            profileResponse.setType("ADMIN");
            profileResponse.setName(username);
        } else {
            profileResponse.setType("UNKNOWN");
        }
        userMeResponse.setId(user.getId());
        userMeResponse.setPermissions(permissions);
        userMeResponse.setProfileResponse(profileResponse);
        userMeResponse.setRoles(roles);
        userMeResponse.setUsername(username);
        return userMeResponse;

    }
}
