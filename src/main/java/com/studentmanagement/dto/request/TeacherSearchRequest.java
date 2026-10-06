package com.studentmanagement.dto.request;

import java.util.List;

public class TeacherSearchRequest {
    
    private List<String>names;
    private  List<String>usernames;
    public List<String> getNames() {
        return names;
    }
    public void setNames(List<String> names) {
        this.names = names;
    }
    public List<String> getUsernames() {
        return usernames;
    }
    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
    
}
