package com.jonmax.activiti;


import org.springframework.stereotype.Component;

@Component
public class UserBean {

    public String getUsername(int id) {
        if(id == 1) {
            return "jon";
        }
        if(id == 2) {
            return "max";
        }
        return "admin";
    }
}