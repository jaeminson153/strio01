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
	private String adminId; // 관리자 아이디
	private String pwd; 	// 비밀번호
	private String name; 	// 이름
	private String phone; 	// 전화번호
	
	// DTO -> Entity
	public AdminEntity toEntity() {
		return AdminEntity.builder()
				.adminId(adminId)
				.pwd(pwd)
				.name(name)
				.phone(phone)
				.build();
	}

	// Entity -> DTO
	public static AdminDTO toDTO(AdminEntity adminEntity) {
		return AdminDTO.builder()
				.adminId(adminEntity.getAdminId())
				.pwd(adminEntity.getPwd())
				.name(adminEntity.getName())
				.phone(adminEntity.getPhone())
				.build();
	}	
}
