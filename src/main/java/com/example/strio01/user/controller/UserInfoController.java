package com.example.strio01.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.strio01.config.auth.PrincipalDetails;
import com.example.strio01.config.jwt.JwtTokenProvider;
import com.example.strio01.user.dto.AuthInfo;
import com.example.strio01.user.dto.UserInfoDTO;
import com.example.strio01.user.entity.UserInfoEntity;
import com.example.strio01.user.service.AuthService;
import com.example.strio01.user.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AuthService authService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public UserInfoController() {}

    // ✅ 회원가입
    @PostMapping("/signup")
    public ResponseEntity<AuthInfo> signup(@RequestBody UserInfoDTO dto) {
        dto.setPasswd(passwordEncoder.encode(dto.getPasswd()));
        AuthInfo authInfo = userInfoService.createUserProcess(dto);
        return ResponseEntity.ok(authInfo);
    }

   

    // ✅ 단일 사용자 조회
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable("userId") String userId,
                                               @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("조회 요청 userId={}, principal={}", userId, principal.getUsername());
        UserInfoDTO userDTO = userInfoService.getUser(userId);
        return ResponseEntity.ok(userDTO);
    }

    // ✅ 회원 정보 수정
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AuthInfo> updateUser(@RequestBody UserInfoDTO dto) {
        dto.setPasswd(passwordEncoder.encode(dto.getPasswd()));
        AuthInfo authInfo = userInfoService.updateUserProcess(dto);
        return ResponseEntity.ok(authInfo);
    }

    // ✅ 회원 삭제
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        log.info("회원 삭제 요청: {}", userId);
        userInfoService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    // ✅ 로그아웃
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization-refresh") String refreshToken) {
        String userId = JWT.require(Algorithm.HMAC512("mySecurityCos"))
                .build()
                .verify(refreshToken)
                .getClaim("userId")
                .asString();
        log.info("로그아웃 userId={}", userId);
        authService.deleteRefreshToken(userId);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }

    // ✅ 토큰 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("Authorization-refresh");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리프레시 토큰이 없습니다."));
        }
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            boolean isValid = authService.validateRefreshToken(userId, refreshToken);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "유효하지 않은 리프레시 토큰입니다."));
            }

            UserInfoEntity entity = userInfoService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));

            AuthInfo authInfo = AuthInfo.builder()
                    .userId(entity.getUserId())
                    .userName(entity.getUserName())
                    .roleCd(entity.getRoleCd())
                    .build();

            String newAccessToken = jwtTokenProvider.createAccessToken(authInfo);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            response.setHeader("Authorization", "Bearer " + newAccessToken);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰 검증 실패", "message", e.getMessage()));
        }
    }
}
