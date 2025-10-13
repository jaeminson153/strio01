package com.example.strio01.admin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.strio01.admin.dto.AuthInfo;
import com.example.strio01.admin.entity.AdminEntity;
import com.example.strio01.admin.service.AdminService;
import com.example.strio01.admin.service.AuthService;
import com.example.strio01.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AdminController {

    @Autowired 
	private AdminService adminService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private BCryptPasswordEncoder encodePassword;
    
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	public AdminController() {
	
	}
	
	
    //로그아웃
    //@PreAuthorize("hasAnyRole('ADMIN','USER')")
    //@DeleteMapping(value="/member/logout/{email}")
    //public ResponseEntity<?> logout(@PathVariable("email") String email){
    @DeleteMapping(value="/admin/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization-refresh") String refreshToken){
    	String email = JWT.require(Algorithm.HMAC512("mySecurityCos")).build().verify(refreshToken).getClaim("memberEmail").asString();
    	log.info("============= ::::: email::::::{}",email);
    	authService.deleteRefreshToken(email);
    	return ResponseEntity.ok(Map.of("message","로그아웃완료"));
    }

    
    @PostMapping("/auth/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = request.getHeader("Authorization-refresh");
		if (refreshToken == null || refreshToken.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "리프레시 토큰이 없습니다."));
		}
		try {
			String email = jwtTokenProvider.getEmailFromToken(refreshToken);
			boolean isValid = authService.validateRefreshToken(email, refreshToken);
			if (!isValid) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 리프레시 토큰입니다."));
			}
			AdminEntity adminEntity = adminService.findByAdminId(email).orElseThrow(() -> new RuntimeException("사용자 없음"));
			AuthInfo authInfo = AuthInfo.builder()
					.adminId(adminEntity.getAdminId())
					.pwd(adminEntity.getPwd())
					.name(adminEntity.getName())
					.build();
			String newAccessToken = jwtTokenProvider.createAccessToken(authInfo);
			
			   // ✅ 브라우저가 이 헤더를 읽을 수 있도록 노출 설정
	        response.setHeader("Access-Control-Expose-Headers", "Authorization");
	        // ✅ 응답 헤더로 accessToken 전달
	        response.setHeader("Authorization", "Bearer " + newAccessToken);
			return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "토큰 검증 실패", "message", e.getMessage()));
		}
	}
	
	
}
