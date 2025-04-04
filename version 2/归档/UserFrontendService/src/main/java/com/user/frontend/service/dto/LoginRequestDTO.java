package com.user.frontend.service.dto;

import java.io.Serializable;

public class LoginRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
