package com.example.strio01.admin.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.admin.entity.AdminEntity;
import com.example.strio01.admin.repository.AdminRefreshTokenRepository;
import com.example.strio01.admin.repository.AdminRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private AdminRefreshTokenRepository refreshTokenRepository; 
	
	public AdminServiceImpl() {
	
	}

	@Override
	public Optional<AdminEntity> findByAdminId(String adminId) {
		
		return adminRepository.findById(adminId);
	}
	
	@Override
	public void deleteAdminProcess(String adminId) {
		refreshTokenRepository.deleteByAdminId(adminId);
		adminRepository.deleteById(adminId);
	}	
	
	
	
}
