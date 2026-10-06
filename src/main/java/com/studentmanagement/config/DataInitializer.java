package com.studentmanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import com.studentmanagement.service.DataInitializerService;

@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initData(DataInitializerService dataInitializerService){
        return args -> dataInitializerService.initializeData();
    }
}
