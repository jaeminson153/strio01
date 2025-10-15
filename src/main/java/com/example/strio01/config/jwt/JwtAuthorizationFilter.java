package com.example.strio01.config.jwt;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.strio01.config.auth.PrincipalDetails;
import com.example.strio01.admin.dto.AuthInfo;
import com.example.strio01.admin.entity.AdminEntity;
import com.example.strio01.admin.repository.AdminRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
	private AdminRepository adminRepository;

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, AdminRepository adminRepository) {
		super(authenticationManager);
		this.adminRepository = adminRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		log.info("인가가 필요한 주소 요청이 실행되는 메소드: doFilterInternal()");
		System.out.println("==============================::7:::1");   

		// 1. 인가가 필요한 요청이 전달된다.
		String accessToken = request.getHeader("Authorization");
		log.info("Authorization: {}", accessToken);
		System.out.println("==============================::7:::2");

		// 2. Header 확인
		// Header가 비어 있거나, 비어있지 않지만 "Bearer" 방식이 아니면 반환한다.
		// JWT 토큰 검증을 해서 정상적인 사용자인 확인 => 정상적인 요청이 아닌 경우
		if (accessToken == null || !accessToken.startsWith("Bearer")) {
			chain.doFilter(request, response);
			return;
		}

		System.out.println("==============================::7:::3");
		// 3. JWT 토큰을 검증해서 정상적인 사용자인지, 권한이 맞는지 확인
		// JWT 토큰 검증을 해서 정상적인 사용자인 확인 => 정상적인 요청인 경우
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		System.out.println("==============================::7:::4");
		try {
			//만료 여부 포함 거믕
		String username = JWT.require(Algorithm.HMAC512("mySecurityCos")).build().verify(jwtToken)
				.getClaim("adminId").asString();
		log.info("username=>{}", username);

		System.out.println("==============================::7:::5");
		// 서명이 정상적으로 처리되었으면
		if (username != null) {
			// spring security가 수행해주는 권한 처리를 위해 아래와 같이 토큰을 만들어
			// Authentication객체를 강제로 만들고 세션에 넣어준다.
			Optional<AdminEntity> optMembersEntity = adminRepository.findById(username);
			AdminEntity adminEntity = optMembersEntity.get();
			log.info("************{}", adminEntity.getUserId());
			AuthInfo authInfo = new AuthInfo(adminEntity.getUserId(), adminEntity.getPasswd(), adminEntity.getUserName());
			PrincipalDetails principalDetails = new PrincipalDetails(authInfo);


			Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
					principalDetails.getAuthorities());


			log.info("authentication.getName() = {}", authentication.getName());
			PrincipalDetails prin = (PrincipalDetails) (authentication.getPrincipal());
			log.info("authentication.principal.getUsername()={}", prin.getUsername());
			// 강제로 시큐리티의 세션에 접근하여 값 저장한다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}


		chain.doFilter(request, response);
		} catch (TokenExpiredException e) {
            log.warn("⚠️ AccessToken 만료됨: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"accessToken expired\"}");


        } catch (Exception e) {
        	
            log.error("❌ JWT 처리 중 예외 발생", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid token\"}");
        }


	}


}// end class

