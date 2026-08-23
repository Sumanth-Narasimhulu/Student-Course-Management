package com.studentmanagement.dto.response;

public class StudentLoginResponse {
    private String jwtToken;
    public StudentLoginResponse(String jwtToken){
        this.jwtToken = jwtToken;
    }
    public String getJwtToken() {
        return jwtToken;
    }
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
}
