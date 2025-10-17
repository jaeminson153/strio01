package com.example.strio01.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthInfo {
    private String userId;
    private String passwd;
    private String userName;
    private String email;
    private String roleCd;
    
    public AuthInfo(String userId, String passwd, String userName) {
    	this.userId = userId;
    	this.passwd = passwd;
    	this.userName = userName;
    }
}
