package com.example.strio01.admin.dto;


import com.example.strio01.admin.entity.AdminEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class AdminDTO {
	private String userId; 		// 사용자 아이디
	private String passwd; 		// 비밀번호
	private String userName; 	// 이름
	private String email; 		// 전화번호
	
	// DTO -> Entity
	public AdminEntity toEntity() {
		return AdminEntity.builder()
				.userId(userId)
				.passwd(passwd)
				.userName(userName)
				.email(email)
				.build();
	}

	// Entity -> DTO
	public static AdminDTO toDTO(AdminEntity adminEntity) {
		return AdminDTO.builder()
				.userId(adminEntity.getUserId())
				.passwd(adminEntity.getPasswd())
				.userName(adminEntity.getUserName())
				.email(adminEntity.getEmail())
				.build();
	}	
}
