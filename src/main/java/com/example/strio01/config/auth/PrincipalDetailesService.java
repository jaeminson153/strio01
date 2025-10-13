package com.example.strio01.config.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//import com.example.strio01.members.dto.AuthInfo;
//import com.example.strio01.members.entity.MembersEntity;
//import com.example.strio01.members.repository.MembersRepository;
import com.example.strio01.admin.dto.AuthInfo;
import com.example.strio01.admin.entity.AdminEntity;
import com.example.strio01.admin.repository.AdminRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PrincipalDetailesService implements UserDetailsService {

	@Autowired
	private AdminRepository adminRepository;

	// 1. AuthenticationProvider에서 loadUserByUsername(String memberEmail)을 호출한다.
	// 2. loadUserByUsername(String adminId)에서는 DB에서 adminId에 해당하는 데이터를 검색해서
	// UserDetails에 담아서 리턴해준다.
	// 3. AuthenticationProvider에서 UserDetailes받아서 Authentication에 저장을 함으로써 결국
	// Security Session에 저장을 한다.

	@Override
	public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
		log.info("PrincipalDetailesService => loadUserByUsername() => adminId:{}", adminId);
		
		Optional<AdminEntity> optMembersEntity = adminRepository.findById(adminId);
		
		if(optMembersEntity.isEmpty()) {
			throw new UsernameNotFoundException(adminId + "사용자명이 존재하지 않습니다.");
		}
		
		AdminEntity adminEntity = optMembersEntity.get();
		log.info("adminId:{} pwd:{} name:{} ",  adminEntity.getAdminId(), adminEntity.getPwd(),adminEntity.getName());
		
		AuthInfo authInfo = AuthInfo.builder()
				.adminId(adminEntity.getAdminId())
				.pwd(adminEntity.getPwd())
				.name(adminEntity.getName())				
				.build();
		return new PrincipalDetails(authInfo);
	}

}
