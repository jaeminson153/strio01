package com.example.strio01.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.strio01.admin.entity.AdminEntity;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, String>{

}
