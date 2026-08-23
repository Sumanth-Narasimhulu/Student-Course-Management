package com.studentmanagement.service;

import com.studentmanagement.Security.JwtService;
import com.studentmanagement.dto.request.StudentLoginRequest;
import com.studentmanagement.dto.request.StudentRegisterRequest;
import com.studentmanagement.dto.response.StudentLoginResponse;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.repository.RoleRepository;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    public AuthService(UserRepository userRepository,StudentRepository studentRepository,PasswordEncoder passwordEncoder,RoleRepository roleRepository,AuthenticationManager authenticationManager,JwtService jwtService){
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }
    @Transactional
    public String register(StudentRegisterRequest studentRegisterRequest){
       if(userRepository.findByUserName(studentRegisterRequest.getUserName()).isPresent()){
           throw  new IllegalArgumentException("username already exists");
       }
       User user = new User();
       user.setUserName(studentRegisterRequest.getUserName());
       user.setPassword(passwordEncoder.encode(studentRegisterRequest.getPassword()));
       
      
       Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(()-> new IllegalArgumentException("Role not found"));
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
        return new StudentLoginResponse(jwtService.generateToken(authentication.getName()));

    }

}
