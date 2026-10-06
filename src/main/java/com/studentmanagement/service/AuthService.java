package com.studentmanagement.service;

import com.studentmanagement.Security.JwtService;
import com.studentmanagement.dto.request.RefreshTokenRequest;
import com.studentmanagement.dto.request.StudentLoginRequest;
import com.studentmanagement.dto.request.StudentRegisterRequest;
import com.studentmanagement.dto.response.StudentLoginResponse;
import com.studentmanagement.entity.RefreshToken;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.RoleRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    public AuthService(UserRepository userRepository,StudentRepository studentRepository,PasswordEncoder passwordEncoder,RoleRepository roleRepository,AuthenticationManager authenticationManager,JwtService jwtService,RefreshTokenService refreshTokenService){
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }
    @Transactional
    public String register(StudentRegisterRequest studentRegisterRequest){
       if(userRepository.findByUserName(studentRegisterRequest.getUserName()).isPresent()){
           throw  new IllegalArgumentException("username already exists");
       }
       User user = new User();
       user.setUserName(studentRegisterRequest.getUserName());
       user.setPassword(passwordEncoder.encode(studentRegisterRequest.getPassword()));
       
      
       Role userRole = roleRepository.findByName("STUDENT")
                    .orElseThrow(()-> new ResourceNotFoundException("Role not found"));
       user.getRoles().add(userRole);
       userRepository.save(user);


       Student student = new Student();
       student.setName(studentRegisterRequest.getName());
       student.setDegree(studentRegisterRequest.getDegree());
       student.setDob(studentRegisterRequest.getDob());
       student.setYear(studentRegisterRequest.getYear());
       student.setUser(user);
       studentRepository.save(student);
       return "Registered successfully";
       
       
       



    }
    public StudentLoginResponse login(StudentLoginRequest studentLoginRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(studentLoginRequest.getUserName(),studentLoginRequest.getPassword()));
        String jwtToken = jwtService.generateToken(authentication.getName());
        User user = userRepository.findByUserName(jwtService.extractUserName(jwtToken))
                    .orElseThrow(()-> new ResourceNotFoundException("username 404"));

        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new StudentLoginResponse(jwtToken, refreshToken);

    }
    public StudentLoginResponse refresh(String refreshTokenRequest) {
       RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(refreshTokenRequest);
       RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenRequest);
       String jwt = jwtService.generateToken(newRefreshToken.getUser().getUserName());
       return new StudentLoginResponse(jwt, newRefreshToken.getRefreshToken());

    }
    public String revoke(String refreshToken){
        return refreshTokenService.revokeRefreshToken(refreshToken);
    }

}
