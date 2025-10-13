package com.example.strio01.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
//로그인 성공 후 인증 상태 정보를 세션에 보관할 때 사용
public class AuthInfo {
	private String adminId;
	private String pwd;
	private String name;
	 
	
	public AuthInfo() {
	}

	
	public AuthInfo(String id,  String pwd) {
		super();
		this.adminId = id;		
		this.pwd = pwd;
	}
	
	public AuthInfo(String id, String pwd, String name) {
		super();
		this.adminId = id;
		this.name = name;
		this.pwd = pwd;		
	}

	
}

