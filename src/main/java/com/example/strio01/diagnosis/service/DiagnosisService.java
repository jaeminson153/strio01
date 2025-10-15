package com.example.strio01.diagnosis.service;

import java.util.List;

import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.board.dto.PageDTO;

public interface DiagnosisService {
	public long countProcess(); 
	public List<DiagnosisDTO> listProcess(PageDTO pv);
	public void insertProcess(DiagnosisDTO dto);
	public DiagnosisDTO contentProcess(long diagnosisId);
	public void updateProcess(DiagnosisDTO dto);
	public void deleteProcess(long noticeId);
}
