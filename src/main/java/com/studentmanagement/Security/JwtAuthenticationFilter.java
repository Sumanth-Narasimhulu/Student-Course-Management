package com.studentmanagement.Security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    public JwtAuthenticationFilter(JwtService jwtService,UserDetailsService userDetailsService){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
       String authHeader = request.getHeader("Authorization");
       if(authHeader == null || !authHeader.startsWith("Bearer ")){
        filterChain.doFilter(request, response);
        return;
       }
       String token = authHeader.substring(7);
       String userName;
       try{
        userName = jwtService.extractUserName(token);
       }catch(Exception e){
        filterChain.doFilter(request, response);
        return;
       }
       if(userName!=null && SecurityContextHolder.getContext().getAuthentication()==null){
        try{
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            boolean valid = jwtService.isValid(token, userName);
            if(valid){
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName, null,userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch(Exception e){
            filterChain.doFilter(request, response);
            return;
        }

       }
        filterChain.doFilter(request, response);
    }
    
}
