package com.example.strio01.code.service;

import java.util.List;

import com.example.strio01.code.dto.CodeDTO;


public interface CodeService {
	public long countProcess(); 
	public CodeDTO contentProcess(String codeId);

}
