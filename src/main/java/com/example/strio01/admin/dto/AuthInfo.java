package com.example.strio01.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
//로그인 성공 후 인증 상태 정보를 세션에 보관할 때 사용
public class AuthInfo {
	private String userId;
	private String passwd;
	private String userName;
	 
	
	public AuthInfo() {
	}

	
	public AuthInfo(String id,  String pwd) {
		super();
		this.userId = id;		
		this.passwd = pwd;
	}
	
	public AuthInfo(String id, String pwd, String name) {
		super();
		this.userId = id;
		this.userName = name;
		this.passwd = pwd;		
	}

	
}

