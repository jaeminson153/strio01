package com.example.strio01.admin.service;

public interface AuthService {
	public void saveRefreshToken(String adminId, String refreshToken, String ip, String userAgent);
	 public boolean validateRefreshToken(String adminId, String token);
	 public void deleteRefreshToken(String adminId);
}
