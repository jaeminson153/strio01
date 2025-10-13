package com.example.strio01.admin.service;

import java.util.Optional;

import com.example.strio01.admin.entity.AdminEntity;


public interface AdminService {

	public Optional<AdminEntity>  findByAdminId(String adminId);
	public void deleteAdminProcess(String adminId); 
}
