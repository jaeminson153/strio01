package com.example.strio01.code.service;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.code.dto.CodeDTO;
import com.example.strio01.code.entity.CodeEntity;
import com.example.strio01.code.repository.CodeRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CodeServiceImp implements CodeService {
	@Autowired
	private CodeRepository codeRepository;

	public CodeServiceImp() {

	}

	@Transactional
	@Override
	public long countProcess() {
		long cnt = codeRepository.count();
		return cnt;
	}

	
	@Transactional
	@Override
	public CodeDTO contentProcess(String codeId) {

		CodeEntity codeEntity = codeRepository.findCodeByCodeId(codeId);
		return CodeDTO.toDTO(codeEntity);
	}



}





