package com.example.strio01.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.strio01.admin.entity.AdminRefreshTokenEntity;

@Repository
public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshTokenEntity, Long> {
	   Optional<AdminRefreshTokenEntity> findByAdminId(String adminId);
	   void deleteByAdminId(String adminId);
}
