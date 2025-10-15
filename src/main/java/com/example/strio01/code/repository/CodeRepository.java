package com.example.strio01.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.code.entity.CodeEntity;


@Repository
public interface CodeRepository extends JpaRepository<CodeEntity, Long> {

	//클럽 전체 리스트 보기 	
	@Query(value="SELECT b FROM CodeEntity b ")
	List<CodeEntity> findCodeList();
	
	//클럽 상세보기  	
	@Query(value="SELECT b FROM CodeEntity b where b.codeId =:codeId")
	CodeEntity findCodeByCodeId(@Param("codeId") String codeId);	
	
	
}







